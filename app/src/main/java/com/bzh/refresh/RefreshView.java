package com.bzh.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by biezhihua on 16-2-28.
 */
class RefreshView extends View {

    public static final float TRANSITION_START_VAL = 0.0f;
    public static final float TRANSITION_END_VAL = 1.0f;
    public static final int TRANSITION_LOADING_ANIM_DURATION = 330;             // 加载动画时间
    public static final int TRANSITION_ANIM_DURATION = 150;                     // 动画时间
    public static final int TRANSITION_RIGHT_START_DELAY = 270;
    public static final int MODE_NONE = 0x1;                                    // 默认状态
    public static final int MODE_SETUP_1 = MODE_NONE << 1;
    public static final int MODE_SETUP_2 = MODE_SETUP_1 << 1;
    public static final int MODE_SETUP_3 = MODE_SETUP_2 << 1;
    public static final int MODE_SETUP_4 = MODE_SETUP_3 << 1;
    public static final int MODE_SETUP_5 = MODE_SETUP_4 << 1;
    public static final int MODE_SETUP_6 = MODE_SETUP_5 << 1;
    public static final float RATIO_LEG_WIDTH = 8.0f;
    public static final float RATIO_MODE_NONE_RECTANGLE_WIDTH = RATIO_LEG_WIDTH / 2;
    public static final float FACTOR_M_LEG = 1.5f;
    public static final float RATIO_MODE_ROOT = 3;
    public static final float RATIO_MODE_SETUP_0 = 1.0f / RATIO_MODE_ROOT * 0;
    public static final float RATIO_MODE_SETUP_1 = 1.0f / RATIO_MODE_ROOT * 1;
    public static final float RATIO_MODE_SETUP_2 = 1.0f / RATIO_MODE_ROOT * 2;
    public static final float RATIO_MODE_SETUP_3 = 1.0f / RATIO_MODE_ROOT * 3;
    public static final int GAP = 4;
    private static final String TAG = "RefreshView";

    private RefreshLayout.OnRefreshListener mListener;

    private float mMLegWidth;                       // M腿的宽度
    private float mRectangleWidth;                  // 初始矩形的宽度
    private float mTransitionProgress;              // 动画进度 0.0 - 1.0
    private float mMArmWidth;                       // M手臂的宽度
    private int mSize;                              // View尺寸
    private int mColor;                             // 默认颜色
    private int mViewCenter;                        // View中心点
    private int mCurrentMode = MODE_NONE;           // 当前绘画进度

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mLeftPath = new Path();
    private Path mRightPath = new Path();
    private Path mLeftMArmPath = new Path();
    private Path mRightMArmPath = new Path();

    private ValueAnimator mResetThreeSquareAnim;    // 摆放三个点

    private ValueAnimator mLeftLoadingAnim;         // 加载动画-左侧点的相关参数
    private float leftSquareX;
    private float leftSquareY;
    private float mLeftSquareProgress;
    private boolean isLeftReverse;

    private ValueAnimator mCenterLoadingAnim;       // 加载动画-中心点的相关参数
    private ValueAnimator mCenterResetAnim;
    private float centerSquareX;
    private float centerSquareY;
    private float mCenterSquareProgress;
    private boolean isCenterReverse;
    private boolean isCenterReversed;

    private ValueAnimator mRightLoadingAnim;        // 加载动画-右侧点的相关参数
    private ValueAnimator mRightResetAnim;
    private float rightSquareX;
    private float rightSquareY;
    private float mRightSquareProgress;
    private boolean isRightReverse;
    private boolean isRightReversed;

    private int mRefreshViewHeight;

    public RefreshView(Context context) {
        this(context, null);
    }

    public RefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout, 0, 0);
            mColor = a.getColor(R.styleable.RefreshLayout_RefreshViewColor, ContextCompat.getColor(context, android.R.color.holo_blue_dark));
            mRefreshViewHeight = (int) a.getDimension(R.styleable.RefreshLayout_RefreshViewHeight, d2x(RefreshLayout.DEFAULT_REFRESH_VIEW_HEIGHT));
            a.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initializeValues();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        resetPaint();

        resetPath();

        drawStateContent(canvas);

//        drawOuterSquare(canvas);
    }

    private void drawStateContent(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColor);

        switch (mCurrentMode) {
            case MODE_NONE: {
                drawNone(canvas);
            }
            break;
            case MODE_SETUP_1: {
                drawSetup1(canvas);
            }
            break;
            case MODE_SETUP_2: {
                drawSetup2(canvas);
            }
            break;
            case MODE_SETUP_3: {
                drawSetup3(canvas);
            }
            break;
            case MODE_SETUP_4: {
                drawSetup4(canvas);
            }
            break;
            case MODE_SETUP_5: {
                drawSetup5(canvas);
            }
            break;
            case MODE_SETUP_6: {
                drawSetup6(canvas);
            }
            break;
        }
    }

    private void drawNone(Canvas canvas) {
        drawDefaultRectangle(canvas);

        mCurrentMode = MODE_SETUP_1;
    }

    private void drawSetup1(Canvas canvas) {// 将 0 ~ 0.33 内的值变换为 0 ~ 1.0的值
        final float setup1Progress = mTransitionProgress - RATIO_MODE_SETUP_0;
        final float setup1TransitionProgress = setup1Progress * RATIO_MODE_ROOT;

        drawLeftIncreaseTrapezoidPath(canvas, setup1TransitionProgress);

        drawRightIncreaseTrapezoidPath(canvas, setup1TransitionProgress);

        if (mTransitionProgress >= RATIO_MODE_SETUP_1) {
            mCurrentMode = MODE_SETUP_2;
        }
    }

    private void drawSetup2(Canvas canvas) {// 低于临界值则返回到上一步
        if (mTransitionProgress < RATIO_MODE_SETUP_1) {
            mCurrentMode = MODE_SETUP_1;
            return;
        }

        // 将 0.33 ~ 0.66 内的值变换为 0 ~ 1.0的值
        float setup2Progress = mTransitionProgress - RATIO_MODE_SETUP_1;
        float setup2TransitionProgress = setup2Progress * RATIO_MODE_ROOT;

        // 根据两点得道底边和对边
        float x1 = mViewCenter + mRectangleWidth / 2;
        float y1 = mViewCenter - mRectangleWidth / 2 - d2x(GAP);
        float x2 = mMLegWidth;
        float y2 = mSize;

        float base = getBase(getAngle(y1 - y2, x1 - x2), getHypotenuse(y1 - y2, x1 - x2));
        float opposite = getOpposite(getAngle(y1 - y2, x1 - x2), getHypotenuse(y1 - y2, x1 - x2));

        drawLeftReduceTrapezoidPath(canvas, setup2TransitionProgress, base, opposite);

        drawRightReduceTrapezoidPath(canvas, setup2TransitionProgress, base, opposite);

        // 超出临界值则进入下一步
        if (mTransitionProgress >= RATIO_MODE_SETUP_2) {
            mCurrentMode = MODE_SETUP_3;
        }
    }

    private void drawSetup3(Canvas canvas) {// 低于临界值则返回到上一步
        if (mTransitionProgress < RATIO_MODE_SETUP_2) {
            mCurrentMode = MODE_SETUP_2;
            return;
        }

        // 将 0.66 ~ 0.99 内的值变换为 0 ~ 1.0的值
        float setup3Progress = mTransitionProgress - RATIO_MODE_SETUP_2;
        float setup3TransitionProgress = setup3Progress * RATIO_MODE_ROOT;

        drawLeftMLeg(canvas);

        drawRightMLeg(canvas);

        drawLeftIncreaseMArmPath(canvas, setup3TransitionProgress);

        drawRightIncreaseMArmPath(canvas, setup3TransitionProgress);
    }

    private void drawSetup4(Canvas canvas) {// 根据进度得到M“腿”的高度
        float legHeight = mSize - mTransitionProgress * (mSize - mMLegWidth);

        drawLeftMLeg(canvas, legHeight);

        drawRightMLeg(canvas, legHeight);

        drawLeftReduceMArmPath(canvas, mTransitionProgress);

        drawRightReduceMArmPath(canvas, mTransitionProgress);

        if (mTransitionProgress >= 0.9f) {
            float centerX = mViewCenter;
            float centerY = mViewCenter + mMLegWidth / 2;
            drawSquare(canvas, centerX, centerY, mPaint);
        }
    }

    private void drawSetup5(Canvas canvas) {
        float endY = 0;

        // 画中心的正方形点
        centerSquareX = mViewCenter;
        centerSquareY = mViewCenter + mMLegWidth / 2;
        drawSquare(canvas, centerSquareX, centerSquareY, mPaint);

        // 画左边的正方形点
        endY = mSize - mSize / 4 - mMLegWidth / 2;
        leftSquareX = mMLegWidth / 2;
        leftSquareY = mMLegWidth / 2 + endY * mTransitionProgress;
        drawSquare(canvas, leftSquareX, leftSquareY, mPaint);

        // 画右边的正方形点
        endY = mSize / 4 - mMLegWidth / 2;
        rightSquareX = mSize - mMLegWidth / 2;
        rightSquareY = mMLegWidth / 2 + endY * mTransitionProgress;
        drawSquare(canvas, rightSquareX, rightSquareY, mPaint);

        if (mTransitionProgress >= 1.0f) {
            startLeftLoadingAnim();
            startCenterResetAnim();
            startRightResetAnim();
            mCurrentMode = MODE_SETUP_6;
        }
    }

    private void drawSetup6(Canvas canvas) {
        if (isLeftReverse) {
            drawSquare(canvas, leftSquareX, mSize / 4 + mLeftSquareProgress, mPaint);
        } else {
            drawSquare(canvas, leftSquareX, mSize - mSize / 4 - mLeftSquareProgress, mPaint);
        }

        if (!isCenterReverse) {
            if (isCenterReversed) {
                drawSquare(canvas, centerSquareX, mSize / 4 + mCenterSquareProgress, mPaint);
            } else {
                if (mViewCenter + mMLegWidth / 2 + mCenterSquareProgress <= mSize - mSize / 4) {
                    drawSquare(canvas, centerSquareX, mViewCenter + mMLegWidth / 2 + mCenterSquareProgress, mPaint);
                } else {
                    drawSquare(canvas, centerSquareX, mSize - mSize / 4, mPaint);
                }
            }
        } else {
            drawSquare(canvas, centerSquareX, mSize - mSize / 4 - mCenterSquareProgress, mPaint);
        }

        if (isRightReverse) {
            drawSquare(canvas, rightSquareX, mSize - mSize / 4 - mRightSquareProgress, mPaint);
        } else {
            if (isRightReversed) {
                drawSquare(canvas, rightSquareX, mSize / 4 + mRightSquareProgress, mPaint);
            } else {
                if (mSize / 4 + mRightSquareProgress <= mSize - mSize / 4) {
                    drawSquare(canvas, rightSquareX, mSize / 4 + mRightSquareProgress, mPaint);
                } else {
                    drawSquare(canvas, rightSquareX, mSize - mSize / 4, mPaint);
                }
            }
        }
    }

    public void setMode(int mode) {
        switch (mode) {
            case MODE_SETUP_4:
                if (mCurrentMode != MODE_SETUP_4) {
                    mCurrentMode = MODE_SETUP_4;
                }
                break;
            case MODE_SETUP_5:
                if (mCurrentMode != MODE_SETUP_5) {
                    mCurrentMode = MODE_SETUP_5;
                    startResetThreeSquareAnim();
                }
                break;
        }
    }

    private void startResetThreeSquareAnim() {
        if (mResetThreeSquareAnim == null) {
            mResetThreeSquareAnim = ValueAnimator.ofFloat(TRANSITION_END_VAL);
            mResetThreeSquareAnim.setFloatValues(TRANSITION_START_VAL, TRANSITION_END_VAL);
            mResetThreeSquareAnim.setDuration(TRANSITION_ANIM_DURATION);
            mResetThreeSquareAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mResetThreeSquareAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mTransitionProgress = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
        } else if (mResetThreeSquareAnim.isRunning()) {
            mResetThreeSquareAnim.cancel();
        }
        mResetThreeSquareAnim.start();
    }

    private void startLeftLoadingAnim() {
        if (mLeftLoadingAnim == null) {
            mLeftLoadingAnim = ValueAnimator.ofFloat(0.f);
            mLeftLoadingAnim.setFloatValues(0.f, mSize / 2);
            mLeftLoadingAnim.setDuration(TRANSITION_LOADING_ANIM_DURATION);
            mLeftLoadingAnim.setRepeatMode(ValueAnimator.RESTART);
            mLeftLoadingAnim.setRepeatCount(ValueAnimator.INFINITE);
            mLeftLoadingAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mLeftLoadingAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mLeftSquareProgress = (float) animation.getAnimatedValue();

                    postInvalidate();
                }
            });
            mLeftLoadingAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (mListener != null) {
                        mListener.onRefresh();
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    isLeftReverse = !isLeftReverse;
                }
            });
        } else if (mLeftLoadingAnim.isRunning()) {
            mLeftLoadingAnim.cancel();
        }
        mLeftLoadingAnim.start();
    }

    private void startCenterResetAnim() {
        if (mCenterResetAnim == null) {
            mCenterResetAnim = ValueAnimator.ofFloat(0.f);
            mCenterResetAnim.setFloatValues(0.f, mSize - mSize / 4 - (mViewCenter + mMLegWidth / 2));
            mCenterResetAnim.setDuration(TRANSITION_ANIM_DURATION);
            mCenterResetAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCenterSquareProgress = (float) animation.getAnimatedValue();
                }
            });
            mCenterResetAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isCenterReverse = !isCenterReverse;
                    isCenterReversed = true;
                    startCenterLoadingAnim();
                }
            });
        } else if (mCenterResetAnim.isRunning()) {
            mCenterResetAnim.cancel();
        }
        mCenterResetAnim.start();
    }

    private void startCenterLoadingAnim() {
        if (mCenterLoadingAnim == null) {
            mCenterLoadingAnim = ValueAnimator.ofFloat(0.f);
            mCenterLoadingAnim.setFloatValues(0.f, mSize / 2);
            mCenterLoadingAnim.setDuration(TRANSITION_LOADING_ANIM_DURATION);
            mCenterLoadingAnim.setRepeatMode(ValueAnimator.RESTART);
            mCenterLoadingAnim.setRepeatCount(ValueAnimator.INFINITE);
            mCenterLoadingAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mCenterLoadingAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCenterSquareProgress = (float) animation.getAnimatedValue();
                }
            });
            mCenterLoadingAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    isCenterReverse = !isCenterReverse;
                }
            });
        } else if (mCenterLoadingAnim.isRunning()) {
            mCenterLoadingAnim.cancel();
        }
        mCenterLoadingAnim.start();
    }

    private void startRightResetAnim() {
        if (mRightResetAnim == null) {
            mRightResetAnim = ValueAnimator.ofFloat(0.f);
            mRightResetAnim.setFloatValues(0.f, mSize / 2);
            mRightResetAnim.setDuration(TRANSITION_RIGHT_START_DELAY);
            mRightResetAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRightSquareProgress = (float) animation.getAnimatedValue();
                }
            });
            mRightResetAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isRightReverse = !isRightReverse;
                    isRightReversed = true;
                    startRightLoadingAnim();
                }
            });
        } else if (mRightResetAnim.isRunning()) {
            mRightResetAnim.cancel();
        }
        mRightResetAnim.start();
    }

    private void startRightLoadingAnim() {
        if (mRightLoadingAnim == null) {
            mRightLoadingAnim = ValueAnimator.ofFloat(0.f);
            mRightLoadingAnim.setFloatValues(0.f, mSize / 2);
            mRightLoadingAnim.setDuration(TRANSITION_LOADING_ANIM_DURATION);
            mRightLoadingAnim.setRepeatMode(ValueAnimator.RESTART);
            mRightLoadingAnim.setRepeatCount(ValueAnimator.INFINITE);
            mRightLoadingAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mRightLoadingAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRightSquareProgress = (float) animation.getAnimatedValue();
                }
            });
            mRightLoadingAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    isRightReverse = !isRightReverse;
                }
            });
        } else if (mRightLoadingAnim.isRunning()) {
            mRightLoadingAnim.cancel();
        }
        mRightLoadingAnim.start();
    }

    /**
     * 画一个小正方形
     */
    private void drawSquare(Canvas canvas, float centerX, float centerY, Paint paint) {
        RectF rectF = new RectF();
        rectF.set(centerX - mMLegWidth / 2, centerY - mMLegWidth / 2, centerX + mMLegWidth / 2, centerY + mMLegWidth / 2);
        canvas.drawRect(rectF, paint);
    }

    private void drawRightReduceMArmPath(Canvas canvas, float transitionProgress) {

        // 计算左平行线的斜率/斜边/底边/对边/角度
        float leftX1 = mViewCenter - mMArmWidth;
        float leftY1 = mViewCenter + mMArmWidth;
        float leftX2 = mSize - mMLegWidth;
        float leftY2 = 0;
        float leftAngle = getAngle(leftY2 - leftY1, leftX2 - leftX1);
        float leftHypotenuse = getHypotenuse(leftY2 - leftY1, leftX2 - leftX1);
        float leftBase = getBase(leftAngle, leftHypotenuse);
        float leftOpposite = getOpposite(leftAngle, leftHypotenuse);

        // 计算右平行线的斜率/斜边/底边/对边/角度
        float rightX1 = mViewCenter + mMArmWidth;
        float rightY1 = mViewCenter + mMArmWidth;
        float rightX2 = mSize;
        float rightY2 = 0;
        float rightAngle = getAngle(rightY2 - rightY1, rightX2 - rightX1);
        float rightHypotenuse = getHypotenuse(rightY2 - rightY1, rightX2 - rightX1);
        float rightBase = getBase(rightAngle, rightHypotenuse);
        float rightOpposite = getOpposite(rightAngle, rightHypotenuse);

        // 根据左右平行线的结果，绘制四边形
        float leftTopX = mSize - mMLegWidth - (leftBase * transitionProgress);
        float leftTopY = 0 - (leftOpposite * transitionProgress);
        float rightTopX = mSize - (rightBase * transitionProgress);
        float rightTopY = 0 - (rightOpposite * transitionProgress);

        float leftBottomX = mViewCenter - mMArmWidth;
        float leftBottomY = mViewCenter + mMArmWidth;
        float rightBottomX = mViewCenter + mMArmWidth;
        float rightBottomY = mViewCenter + mMArmWidth;

        mRightMArmPath.moveTo(rightBottomX, rightBottomY);
        mRightMArmPath.lineTo(leftBottomX, leftBottomY);
        mRightMArmPath.lineTo(leftTopX, leftTopY);
        mRightMArmPath.lineTo(rightTopX, rightTopY);
        mRightMArmPath.close();
        canvas.drawPath(mRightMArmPath, mPaint);
    }

    /**
     * 画左侧的M"臂"
     */
    private void drawLeftReduceMArmPath(Canvas canvas, float transitionProgress) {

        // 计算左平行线的斜率/斜边/底边/对边/角度
        float leftX1 = mViewCenter - mMArmWidth;
        float leftY1 = mViewCenter + mMArmWidth;
        float leftX2 = 0;
        float leftY2 = 0;
        float leftAngle = getAngle(leftY1 - leftY2, leftX1 - leftX2);
        float leftHypotenuse = getHypotenuse(leftY1 - leftY2, leftX1 - leftX2);
        float leftBase = getBase(leftAngle, leftHypotenuse);
        float leftOpposite = getOpposite(leftAngle, leftHypotenuse);

        // 计算右平行线的斜率/斜边/底边/对边/角度
        float rightX1 = mViewCenter + mMArmWidth;
        float rightY1 = mViewCenter + mMArmWidth;
        float rightX2 = mMLegWidth;
        float rightY2 = 0;
        float rightAngle = getAngle(rightY1 - rightY2, rightX1 - rightX2);
        float rightHypotenuse = getHypotenuse(rightY1 - rightY2, rightX1 - rightX2);
        float rightBase = getBase(rightAngle, rightHypotenuse);
        float rightOpposite = getOpposite(rightAngle, rightHypotenuse);

        // 根据左右平行线的结果，绘制四边形
        float leftTopX = 0 + (leftBase * transitionProgress);
        float leftTopY = 0 + (leftOpposite * transitionProgress);
        float rightTopX = mMLegWidth + (rightBase * transitionProgress);
        float rightTopY = 0 + (rightOpposite * transitionProgress);

        float leftBottomX = mViewCenter - mMArmWidth;
        float leftBottomY = mViewCenter + mMArmWidth;

        float rightBottomX = mViewCenter + mMArmWidth;
        float rightBottomY = mViewCenter + mMArmWidth;

        mLeftMArmPath.moveTo(rightBottomX, rightBottomY);
        mLeftMArmPath.lineTo(leftBottomX, leftBottomY);
        mLeftMArmPath.lineTo(leftTopX, leftTopY);
        mLeftMArmPath.lineTo(rightTopX, rightTopY);
        mLeftMArmPath.close();
        canvas.drawPath(mLeftMArmPath, mPaint);
    }

    private void drawRightMLeg(Canvas canvas, float legHeight) {
        mRightPath.moveTo(mSize, 0);
        mRightPath.lineTo(mSize, legHeight);
        mRightPath.lineTo(mSize - mMLegWidth, legHeight);
        mRightPath.lineTo(mSize - mMLegWidth, 0);
        mRightPath.close();
        canvas.drawPath(mRightPath, mPaint);
    }

    private void drawLeftMLeg(Canvas canvas, float legHeight) {
        mLeftPath.moveTo(0, 0);
        mLeftPath.lineTo(mMLegWidth, 0);
        mLeftPath.lineTo(mMLegWidth, legHeight);
        mLeftPath.lineTo(0, legHeight);
        mLeftPath.close();
        canvas.drawPath(mLeftPath, mPaint);
    }

    /**
     * 画右侧M“臂”
     */
    private void drawRightIncreaseMArmPath(Canvas canvas, float transitionProgress) {

        // 计算左平行线的斜率/斜边/底边/对边/角度
        float leftX1 = mViewCenter - mMArmWidth;
        float leftY1 = mViewCenter + mMArmWidth;
        float leftX2 = mSize - mMLegWidth;
        float leftY2 = 0;
        float leftAngle = getAngle(leftY2 - leftY1, leftX2 - leftX1);
        float leftHypotenuse = getHypotenuse(leftY2 - leftY1, leftX2 - leftX1);
        float leftBase = getBase(leftAngle, leftHypotenuse);
        float leftOpposite = getOpposite(leftAngle, leftHypotenuse);

        // 计算右平行线的斜率/斜边/底边/对边/角度
        float rightX1 = mViewCenter + mMArmWidth;
        float rightY1 = mViewCenter + mMArmWidth;
        float rightX2 = mSize;
        float rightY2 = 0;
        float rightAngle = getAngle(rightY2 - rightY1, rightX2 - rightX1);
        float rightHypotenuse = getHypotenuse(rightY2 - rightY1, rightX2 - rightX1);
        float rightBase = getBase(rightAngle, rightHypotenuse);
        float rightOpposite = getOpposite(rightAngle, rightHypotenuse);


        // 根据左右平行线的结果，绘制四边形
        float leftTopX = mSize - mMLegWidth;
        float leftTopY = 0;
        float rightTopX = mSize;
        float rightTopY = 0;
        float leftBottomX = leftTopX - (leftBase * transitionProgress);
        float leftBottomY = leftTopY - (leftOpposite * transitionProgress);
        float rightBottomX = rightTopX - (rightBase * transitionProgress);
        float rightBottomY = rightTopY - (rightOpposite * transitionProgress);

        mRightMArmPath.moveTo(rightBottomX, rightBottomY);
        mRightMArmPath.lineTo(leftBottomX, leftBottomY);
        mRightMArmPath.lineTo(leftTopX, leftTopY);
        mRightMArmPath.lineTo(rightTopX, rightTopY);
        mRightMArmPath.close();
        canvas.drawPath(mRightMArmPath, mPaint);
    }

    /**
     * 画左侧M“臂”
     */
    private void drawLeftIncreaseMArmPath(Canvas canvas, float transitionProgress) {
        // 计算左平行线的斜率/斜边/底边/对边/角度
        float leftX1 = mViewCenter - mMArmWidth;
        float leftY1 = mViewCenter + mMArmWidth;
        float leftX2 = 0;
        float leftY2 = 0;
        float leftAngle = getAngle(leftY1 - leftY2, leftX1 - leftX2);
        float leftHypotenuse = getHypotenuse(leftY1 - leftY2, leftX1 - leftX2);
        float leftBase = getBase(leftAngle, leftHypotenuse);
        float leftOpposite = getOpposite(leftAngle, leftHypotenuse);

        // 计算右平行线的斜率/斜边/底边/对边/角度
        float rightX1 = mViewCenter + mMArmWidth;
        float rightY1 = mViewCenter + mMArmWidth;
        float rightX2 = mMLegWidth;
        float rightY2 = 0;
        float rightAngle = getAngle(rightY1 - rightY2, rightX1 - rightX2);
        float rightHypotenuse = getHypotenuse(rightY1 - rightY2, rightX1 - rightX2);
        float rightBase = getBase(rightAngle, rightHypotenuse);
        float rightOpposite = getOpposite(rightAngle, rightHypotenuse);

        // 根据左右平行线的结果，绘制四边形
        float leftBottomX = mViewCenter - mMArmWidth;
        float leftBottomY = mViewCenter + mMArmWidth;
        float rightBottomX = mViewCenter + mMArmWidth;
        float rightBottomY = mViewCenter + mMArmWidth;

        float leftTopX = leftBottomX - (leftBase * transitionProgress);
        float leftTopY = leftBottomY - (leftOpposite * transitionProgress);
        float rightTopX = rightBottomX - (rightBase * transitionProgress);
        float rightTopY = rightBottomY - (rightOpposite * transitionProgress);

        mLeftMArmPath.moveTo(rightBottomX, rightBottomY);
        mLeftMArmPath.lineTo(leftBottomX, leftBottomY);
        mLeftMArmPath.lineTo(leftTopX, leftTopY);
        mLeftMArmPath.lineTo(rightTopX, rightTopY);
        mLeftMArmPath.close();
        canvas.drawPath(mLeftMArmPath, mPaint);
    }

    /**
     * 画M右侧的腿
     */
    private void drawRightMLeg(Canvas canvas) {
        mRightPath.moveTo(mSize, 0);
        mRightPath.lineTo(mSize, mSize);
        mRightPath.lineTo(mSize - mMLegWidth, mSize);
        mRightPath.lineTo(mSize - mMLegWidth, 0);
        mRightPath.close();
        canvas.drawPath(mRightPath, mPaint);
    }

    /**
     * 画M左侧的腿
     */
    private void drawLeftMLeg(Canvas canvas) {
        mLeftPath.moveTo(0, 0);
        mLeftPath.lineTo(mMLegWidth, 0);
        mLeftPath.lineTo(mMLegWidth, mSize);
        mLeftPath.lineTo(0, mSize);
        mLeftPath.close();
        canvas.drawPath(mLeftPath, mPaint);
    }

    /**
     * 画右侧的梯形
     */
    private void drawRightReduceTrapezoidPath(Canvas canvas, float transitionProgress, float base, float opposite) {

        float rightTopX = mSize;
        float rightTopY = 0;

        float rightBottomY = mSize;

        float leftTopX = mViewCenter - mRectangleWidth / 2 + (base * transitionProgress);
        float leftTopY = mViewCenter + mRectangleWidth / 2 + d2x(GAP) + (opposite * transitionProgress);

        float leftBottomY = mSize;

        mRightPath.moveTo(rightTopX, rightTopY);
        mRightPath.lineTo(rightTopX, rightBottomY);
        mRightPath.lineTo(leftTopX, leftBottomY);
        mRightPath.lineTo(leftTopX, leftTopY);
        mRightPath.close();

        canvas.drawPath(mRightPath, mPaint);
    }

    /**
     * 画左侧的梯形
     */
    private void drawLeftReduceTrapezoidPath(Canvas canvas, float transitionProgress, float base, float opposite) {

        float rightBottomX = mViewCenter + mRectangleWidth / 2 - (base * transitionProgress);
        float rightBottomY = mViewCenter - mRectangleWidth / 2 - d2x(GAP) - (opposite * transitionProgress);

        float rightTopY = 0;
        float leftBottomX = 0;
        float leftBottomY = mSize;
        float leftTopY = 0;

        mLeftPath.moveTo(leftBottomX, leftBottomY);
        mLeftPath.lineTo(leftBottomX, leftTopY);
        mLeftPath.lineTo(rightBottomX, rightTopY);
        mLeftPath.lineTo(rightBottomX, rightBottomY);
        mLeftPath.close();

        canvas.drawPath(mLeftPath, mPaint);
    }

    /**
     * 画右侧的梯形
     */
    private void drawRightIncreaseTrapezoidPath(Canvas canvas, float transitionProgress) {

        // 由于整个View是正方形，直角三角形两边相等，所以不需要进行特殊的数学计算
        float leftTopX = mViewCenter - mRectangleWidth / 2;
        float leftTopY = mViewCenter + mRectangleWidth / 2 + d2x(GAP);
        float leftBottomY = mSize;
        float rightTopX = mViewCenter + mRectangleWidth / 2 + (mSize / 2 - mRectangleWidth / 2) * transitionProgress;
        float rightTopY = mViewCenter - mRectangleWidth / 2 - (mSize / 2 - mRectangleWidth / 2) * transitionProgress;
        float rightBottomY = mSize;

        mRightPath.moveTo(leftTopX, leftBottomY);
        mRightPath.lineTo(leftTopX, leftTopY);
        mRightPath.lineTo(rightTopX, rightTopY);
        mRightPath.lineTo(rightTopX, rightBottomY);
        mRightPath.close();

        canvas.drawPath(mRightPath, mPaint);
    }

    /**
     * 画左侧的梯形
     */
    private void drawLeftIncreaseTrapezoidPath(Canvas canvas, float transitionProgress) {

        // 由于整个View是正方形，直角三角形两边相等，所以不需要进行特殊的数学计算
        float rightTopX = mViewCenter + mRectangleWidth / 2;
        float rightTopY = 0;
        float rightBottomY = mViewCenter - mRectangleWidth / 2 - d2x(GAP);
        float leftBottomX = mViewCenter - mRectangleWidth / 2 - (mSize / 2 - mRectangleWidth / 2) * transitionProgress;
        float leftBottomY = mViewCenter + mRectangleWidth / 2 + (mSize / 2 - mRectangleWidth / 2) * transitionProgress;
        float leftTopY = 0;

        mLeftPath.moveTo(rightTopX, rightTopY);
        mLeftPath.lineTo(rightTopX, rightBottomY);
        mLeftPath.lineTo(leftBottomX, leftBottomY);
        mLeftPath.lineTo(leftBottomX, leftTopY);
        mLeftPath.close();

        canvas.drawPath(mLeftPath, mPaint);
    }

    /**
     * 画一个初始矩形
     */
    private void drawDefaultRectangle(Canvas canvas) {
        RectF noneRectF = new RectF();
        noneRectF.set(mViewCenter - mRectangleWidth / 2, 0, mViewCenter + mRectangleWidth / 2, mSize);
        canvas.drawRect(noneRectF, mPaint);
    }

    /**
     * 画一个测试使用的正方形
     */
    private void drawOuterSquare(Canvas canvas) {

        // Draw the outer square for the test
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(d2x(1));
        mPaint.setColor(Color.BLACK);

        RectF square = new RectF();
        square.set(0, 0, mSize, mSize);
        canvas.drawRect(square, mPaint);
        canvas.drawLine(0, 0, mSize, mSize, mPaint);
        canvas.drawLine(0, mSize, mSize, 0, mPaint);
    }

    /**
     * 重置画笔
     */
    private void resetPaint() {
        mPaint.reset();
        mPaint.setAntiAlias(true);
    }

    /**
     * 重置路径
     */
    private void resetPath() {
        mLeftPath.reset();
        mRightPath.reset();
        mLeftMArmPath.reset();
        mRightMArmPath.reset();
    }

    /**
     * 初始化相关参数
     */
    private void initializeValues() {
        mSize = mRefreshViewHeight;                                 // 初始化View大小
        mViewCenter = mSize / 2;                                    // 初始化中心点
        mRectangleWidth = mSize / RATIO_MODE_NONE_RECTANGLE_WIDTH;  // 初始化初始中心矩形的宽度为M腿宽度的一倍
        mMLegWidth = mSize / RATIO_LEG_WIDTH;                       // 初始化M"腿"的宽度
        mMArmWidth = mMLegWidth / FACTOR_M_LEG;                     // 初始化M"臂"的宽度
    }

    /**
     * 重置数据等相关参数，并会重新初始化数据
     */
    public void resetValues() {

        if (mResetThreeSquareAnim != null) {
            mResetThreeSquareAnim.cancel();
        }
        if (mLeftLoadingAnim != null) {
            mLeftLoadingAnim.cancel();
        }
        if (mCenterLoadingAnim != null) {
            mCenterLoadingAnim.cancel();
        }
        if (mRightLoadingAnim != null) {
            mRightLoadingAnim.cancel();
        }
        if (mCenterResetAnim != null) {
            mCenterResetAnim.cancel();
        }
        if (mRightResetAnim != null) {
            mRightResetAnim.cancel();
        }

        mSize = 0;
        mMLegWidth = 0;
        mRectangleWidth = 0;
        mViewCenter = 0;
        mCurrentMode = MODE_NONE;
        mTransitionProgress = 0.0f;
        mMArmWidth = 0.0f;
        centerSquareX = 0.f;
        centerSquareY = 0.f;
        leftSquareX = 0.f;
        leftSquareY = 0.f;
        rightSquareX = 0.f;
        rightSquareY = 0.f;
        mLeftSquareProgress = 0;
        mCenterSquareProgress = 0;
        mRightSquareProgress = 0;
        isLeftReverse = false;
        isCenterReverse = false;
        isCenterReversed = false;
        isRightReverse = false;
        isRightReversed = false;

        initializeValues();
    }

    /**
     * @param transitionProgress 进度，是一个0 ~ 1间的值
     */
    public void setTransitionProgress(float transitionProgress) {
        mTransitionProgress = transitionProgress;
        postInvalidate();
    }

    private float d2x(float size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getContext().getResources().getDisplayMetrics());
    }

    /**
     * 获取对边
     *
     * @param angle      角度
     * @param hypotenuse 斜边
     * @return
     */
    private float getOpposite(float angle, float hypotenuse) {
        return (float) (Math.sin(angle) * hypotenuse);
    }

    /**
     * 获取底边
     *
     * @param angle      角度
     * @param hypotenuse 斜边
     * @return
     */
    private float getBase(float angle, float hypotenuse) {
        return (float) (Math.cos(angle) * hypotenuse);
    }

    /**
     * 获取任意两点间角度
     * <p>
     * y1-y2 与 y2-y1 不同
     *
     * @param x y1-y2
     * @param y x1-x2
     * @return
     */
    private float getAngle(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    /**
     * 获取斜边
     *
     * @param y y1-y2
     * @param x x1-x2
     * @return 斜边
     */
    private float getHypotenuse(float y, float x) {
        return (float) Math.sqrt(Math.abs(y * y) + Math.abs(x * x));
    }

    public void setOnRefreshListener(RefreshLayout.OnRefreshListener listener) {
        mListener = listener;
    }
}
