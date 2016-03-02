package com.bzh.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
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
    public static final int TRANSITION_LOADING_ANIM_DURATION = 350;             // 加载动画时间
    public static final int TRANSITION_ANIM_DURATION = 200;                     // 动画时间
    public static final int TRANSITION_CENTER_START_DELAY = 200;
    public static final int TRANSITION_RIGHT_START_DELAY = 500;
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

    private int mSize;                              // View尺寸
    private float mMLegWidth;                       // M腿的宽度
    private float mRectangleWidth;                  // 初始矩形的宽度
    private int mViewCenter;                        // View中心点
    private float mTransitionProgress;              // 动画进度 0.0 - 1.0
    private int mCurrentMode = MODE_NONE;           // 当前绘画进度
    private float mMArmWidth;                       // M手臂的宽度
    private RefreshLayout.OnRefreshListener mListener;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mLeftPath = new Path();
    private Path mRightPath = new Path();
    private Path mLeftMArmPath = new Path();
    private Path mRightMArmPath = new Path();
    private int mColor;                             // 默认颜色
    private ValueAnimator mReduceMLegHeightAnim;
    private ValueAnimator mResetThreeSquareAnim;

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
    private float rightSquareX;
    private float rightSquareY;
    private float mRightSquareProgress;
    private boolean isRightReverse;

    public RefreshView(Context context) {
        this(context, null);
    }

    public RefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
                drawDefaultRectangle(canvas);
                mCurrentMode = MODE_SETUP_1;
            }
            break;
            case MODE_SETUP_1: {

                final float setup1Progress = mTransitionProgress - RATIO_MODE_SETUP_0;
                final float setup1TransitionProgress = setup1Progress * RATIO_MODE_ROOT;

                drawSetup1LeftPath(canvas, setup1TransitionProgress);
                drawSetup1RightPath(canvas, setup1TransitionProgress);

                if (mTransitionProgress >= RATIO_MODE_SETUP_1) {
                    mCurrentMode = MODE_SETUP_2;
                }
            }
            break;
            case MODE_SETUP_2: {
                float setup2Progress = mTransitionProgress - RATIO_MODE_SETUP_1;
                float setup2TransitionProgress = setup2Progress * RATIO_MODE_ROOT;

                float x1 = mViewCenter + mRectangleWidth / 2;
                float y1 = mViewCenter - mRectangleWidth / 2 - d2x(GAP);
                float x2 = mMLegWidth;
                float y2 = mSize;

                float base = getBase(getAngle(y1 - y2, x1 - x2), getHypotenuse(y1 - y2, x1 - x2));
                float opposite = getOpposite(getAngle(y1 - y2, x1 - x2), getHypotenuse(y1 - y2, x1 - x2));

                drawSetup2LeftPath(canvas, setup2TransitionProgress, base, opposite);
                drawSetup2RightPath(canvas, setup2TransitionProgress, base, opposite);

                if (mTransitionProgress >= RATIO_MODE_SETUP_2) {
                    mCurrentMode = MODE_SETUP_3;
                }
            }
            break;
            case MODE_SETUP_3: {

                float setup3Progress = mTransitionProgress - RATIO_MODE_SETUP_2;

                float setup3TransitionProgress = setup3Progress * RATIO_MODE_ROOT;

                drawLeftMLeg(canvas);

                drawRightMLeg(canvas);

                drawLeftMArmPath(canvas, setup3TransitionProgress);

                drawRightMArmPath(canvas, setup3TransitionProgress);

            }
            break;
            case MODE_SETUP_4: {

                float legHeight = mSize - mTransitionProgress * (mSize - mMLegWidth);

                drawLeftMLeg(canvas, legHeight);

                drawRightMLeg(canvas, legHeight);

                drawReduceLeftMArmPath(canvas, mTransitionProgress);

                drawReduceRightMArmPath(canvas, mTransitionProgress);

                if (mTransitionProgress >= 0.9f) {
                    float centerX = mViewCenter;
                    float centerY = mViewCenter + mMLegWidth / 2;
                    drawSquare(canvas, centerX, centerY, mPaint);
                }

//                if (mTransitionProgress >= 1.0f) {
//                    mCurrentMode = MODE_SETUP_5;
//                    startResetThreeSquareAnim();
//                    postInvalidate();
//                }
            }
            break;
            case MODE_SETUP_5: {
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
                    startRightLoadingAnim();
                    mCurrentMode = MODE_SETUP_6;
                }
            }
            break;
            case MODE_SETUP_6: {

                if (isLeftReverse) {
                    drawSquare(canvas, leftSquareX, mSize / 4 + mLeftSquareProgress, mPaint);
                } else {
                    drawSquare(canvas, leftSquareX, mSize - mSize / 4 - mLeftSquareProgress, mPaint);
                }

                if (isCenterReverse) {
                    drawSquare(canvas, centerSquareX, mSize / 4 + mCenterSquareProgress, mPaint);
                } else {
                    if (isCenterReversed) {
                        drawSquare(canvas, centerSquareX, mSize - mSize / 4 - mCenterSquareProgress, mPaint);
                    } else {
                        if (mViewCenter + mMLegWidth / 2 - mCenterSquareProgress >= mSize / 4) {
                            drawSquare(canvas, centerSquareX, mViewCenter + mMLegWidth / 2 - mCenterSquareProgress, mPaint);
                        } else {
                            drawSquare(canvas, centerSquareX, mSize / 4, mPaint);
                        }
                    }
                }

                if (isRightReverse) {
                    drawSquare(canvas, rightSquareX, mSize - mSize / 4 - mRightSquareProgress, mPaint);
                } else {
                    drawSquare(canvas, rightSquareX, mSize / 4 + mRightSquareProgress, mPaint);
                }
            }
            break;
        }
    }

    public void setMode(int mode) {
        switch (mode) {
            case MODE_SETUP_4:
                if (mCurrentMode != MODE_SETUP_4) {
                    mCurrentMode = MODE_SETUP_4;
                }
//                startReduceMLegHeightAnim();
//                postInvalidate();
                break;
            case MODE_SETUP_5:
                if (mCurrentMode != MODE_SETUP_5) {
                    mCurrentMode = MODE_SETUP_5;
                    startResetThreeSquareAnim();
                }
                break;
        }
    }

    private void startReduceMLegHeightAnim() {
        if (mReduceMLegHeightAnim == null) {
            mReduceMLegHeightAnim = ValueAnimator.ofFloat(TRANSITION_END_VAL);
            mReduceMLegHeightAnim.setFloatValues(TRANSITION_START_VAL, TRANSITION_END_VAL);
            mReduceMLegHeightAnim.setDuration(TRANSITION_ANIM_DURATION);
            mReduceMLegHeightAnim.setInterpolator(new DecelerateInterpolator());
            mReduceMLegHeightAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mTransitionProgress = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
        } else if (mReduceMLegHeightAnim.isRunning()) {
            mReduceMLegHeightAnim.cancel();
        }
        mReduceMLegHeightAnim.start();
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
            mCenterResetAnim.setFloatValues(0.f, mViewCenter + mMLegWidth / 2 - mSize / 4);
            mCenterResetAnim.setDuration(TRANSITION_ANIM_DURATION);
            mCenterResetAnim.setStartDelay(TRANSITION_CENTER_START_DELAY);
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

    private void startRightLoadingAnim() {
        if (mRightLoadingAnim == null) {
            mRightLoadingAnim = ValueAnimator.ofFloat(0.f);
            mRightLoadingAnim.setFloatValues(0.f, mSize / 2);
            mRightLoadingAnim.setDuration(TRANSITION_LOADING_ANIM_DURATION);
            mRightLoadingAnim.setRepeatMode(ValueAnimator.RESTART);
            mRightLoadingAnim.setRepeatCount(ValueAnimator.INFINITE);
            mRightLoadingAnim.setStartDelay(TRANSITION_RIGHT_START_DELAY);
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

    private void drawSquare(Canvas canvas, float centerX, float centerY, Paint paint) {
        RectF rectF = new RectF();
        rectF.set(centerX - mMLegWidth / 2, centerY - mMLegWidth / 2, centerX + mMLegWidth / 2, centerY + mMLegWidth / 2);
        canvas.drawRect(rectF, paint);
    }

    private void drawReduceRightMArmPath(Canvas canvas, float transitionProgress) {

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

    private void drawReduceLeftMArmPath(Canvas canvas, float transitionProgress) {

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

    private void drawRightMArmPath(Canvas canvas, float transitionProgress) {

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

    private void drawLeftMArmPath(Canvas canvas, float transitionProgress) {
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

    private void drawSetup2RightPath(Canvas canvas, float transitionProgress, float base, float opposite) {

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

    private void drawSetup2LeftPath(Canvas canvas, float transitionProgress, float base, float opposite) {

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

    private void drawSetup1RightPath(Canvas canvas, float transitionProgress) {

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

    private void drawSetup1LeftPath(Canvas canvas, float transitionProgress) {

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

    private void drawDefaultRectangle(Canvas canvas) {
        RectF noneRectF = new RectF();
        noneRectF.set(mViewCenter - mRectangleWidth / 2, 0, mViewCenter + mRectangleWidth / 2, mSize);
        canvas.drawRect(noneRectF, mPaint);
    }

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

    private void resetPaint() {
        mPaint.reset();
        mPaint.setAntiAlias(true);
    }

    private void resetPath() {
        mLeftPath.reset();
        mRightPath.reset();
        mLeftMArmPath.reset();
        mRightMArmPath.reset();
    }

    private void initializeValues() {
        mSize = Math.min(getMeasuredHeight(), getMeasuredWidth());
        mViewCenter = mSize / 2;
        mRectangleWidth = mSize / RATIO_MODE_NONE_RECTANGLE_WIDTH;
        mMLegWidth = mSize / RATIO_LEG_WIDTH;
        mMArmWidth = mMLegWidth / FACTOR_M_LEG;
    }

    public void resetValues() {

        if (mReduceMLegHeightAnim != null) {
            mReduceMLegHeightAnim.cancel();
        }
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

        initializeValues();
    }

    public void setTransitionProgress(float transitionProgress) {
        mTransitionProgress = transitionProgress;
        postInvalidate();
    }

    public void setColor(int color) {
        this.mColor = color;
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
     * <p/>
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
