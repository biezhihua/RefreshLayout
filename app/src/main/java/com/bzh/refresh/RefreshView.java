package com.bzh.refresh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by biezhihua on 16-2-28.
 */
public class RefreshView extends View {


    public static final int MODE_NONE = 0x1;
    public static final int MODE_SETUP_1 = MODE_NONE << 1;
    public static final int MODE_SETUP_2 = MODE_SETUP_1 << 1;
    public static final int MODE_SETUP_3 = MODE_SETUP_2 << 1;
    public static final int MODE_SETUP_4 = MODE_SETUP_3 << 1;
    public static final int MODE_SETUP_5 = MODE_SETUP_4 << 1;

    public static final float ANGLE_DIAGONAL = 45.0f;

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

    /**
     * View尺寸
     */
    private int mSize;

    /**
     * M腿的宽度
     */
    private float mLegWidth;

    /**
     * 初始矩形的宽度
     */
    private float mRectangleWidth;

    /**
     * View中心点
     */
    private int mViewCenter;

    /**
     * 动画进度 0.0 - 1.0
     */
    private float mTransitionProgress;

    /**
     * 当前模式
     */
    private int mCurrentMode = MODE_NONE;

    /**
     * M腿的宽度
     */
    private float mMLegWidth;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Path mLeftPath = new Path();

    private Path mRightPath = new Path();

    private Path mLeftMArmPath = new Path();

    private Path mRightMArmPath = new Path();

    private OnRefreshListener mListener;

    private int mRectangleColor = Color.rgb(84, 255, 159);

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

        drawOuterSquare(canvas);
    }

    private void drawStateContent(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mRectangleColor);

        switch (mCurrentMode) {
            case MODE_NONE: {
                Log.d(TAG, "drawStateContent() called with: " + "mCurrentMode = [" + MODE_NONE + "]");


                RectF noneRectF = new RectF();
                noneRectF.set(mViewCenter - mRectangleWidth / 2, 0, mViewCenter + mRectangleWidth / 2, mSize);
                canvas.drawRect(noneRectF, mPaint);
                mCurrentMode = MODE_SETUP_1;
            }
            break;
            case MODE_SETUP_1: {
                Log.d(TAG, "drawStateContent() called with: " + "mCurrentMode = [" + MODE_SETUP_1 + "]");

                float setup1Progress = mTransitionProgress - RATIO_MODE_SETUP_0;

                float setup1TransitionProgress = setup1Progress * RATIO_MODE_ROOT;

                Log.d(TAG, "drawStateContent() called with: " + "setup1Progress = [" + setup1Progress + "]");

                Log.d(TAG, "drawStateContent() called with: " + "setup1TransitionProgress = [" + setup1TransitionProgress + "]");

                drawSetup1LeftPath(canvas, setup1TransitionProgress);

                drawSetup1RightPath(canvas, setup1TransitionProgress);

                if (mTransitionProgress >= RATIO_MODE_SETUP_1) {
                    mCurrentMode = MODE_SETUP_2;
                }
            }
            break;
            case MODE_SETUP_2: {
                Log.d(TAG, "drawStateContent() called with: " + "mCurrentMode = [" + MODE_SETUP_2 + "]");

                float setup2Progress = mTransitionProgress - RATIO_MODE_SETUP_1;

                float setup2TransitionProgress = setup2Progress * RATIO_MODE_ROOT;

                Log.d(TAG, "drawStateContent() called with: " + "setup2Progress = [" + (setup2Progress) + "]");

                Log.d(TAG, "drawStateContent() called with: " + "setup2TransitionProgress = [" + setup2TransitionProgress + "]");

                float x1 = mViewCenter + mRectangleWidth / 2;
                float y1 = mViewCenter - mRectangleWidth / 2 - d2x(GAP);
                float x2 = mLegWidth;
                float y2 = mSize;
                float y = Math.abs(y1 - y2);
                float x = Math.abs(x1 - x2);
                float angle = (float) Math.atan2(y1 - y2, x1 - x2);                // 角度
                float hypotenuse = (float) Math.sqrt(y * y + x * x);   // 斜边
                float base = (float) (Math.cos(angle) * hypotenuse);     // 底边
                float opposite = (float) (Math.sin(angle) * hypotenuse); // 对边

                drawSetup2LeftPath(canvas, setup2TransitionProgress, base, opposite);
                drawSetup2RightPath(canvas, setup2TransitionProgress, base, opposite);

                if (mTransitionProgress >= RATIO_MODE_SETUP_2) {
                    mCurrentMode = MODE_SETUP_3;
                }
            }
            break;
            case MODE_SETUP_3: {
                Log.d(TAG, "drawStateContent() called with: " + "mCurrentMode = [" + MODE_SETUP_3 + "]");

                float setup3Progress = mTransitionProgress - RATIO_MODE_SETUP_2;

                float setup3TransitionProgress = setup3Progress * RATIO_MODE_ROOT;

                Log.d(TAG, "drawStateContent() called with: " + "setup3Progress = [" + (setup3Progress) + "]");

                Log.d(TAG, "drawStateContent() called with: " + "setup3TransitionProgress = [" + setup3TransitionProgress + "]");

                drawLeftMLeg(canvas);

                drawRightMLeg(canvas);

                drawLeftMLegPath(canvas, setup3TransitionProgress);

                drawRightMLegPath(canvas, setup3TransitionProgress);

                if (mTransitionProgress >= RATIO_MODE_SETUP_3) {
                    mCurrentMode = MODE_SETUP_4;
                    postInvalidate();
                }
            }
            break;
            case MODE_SETUP_4: {
                Log.d(TAG, "drawStateContent() called with: " + "mCurrentMode = [" + MODE_SETUP_4 + "]");

//                mCurrentMode = MODE_SETUP_5;
//                postInvalidate();
            }
            break;
            case MODE_SETUP_5: {
                
            }
            break;
        }
    }

    private void drawRightMLegPath(Canvas canvas, float setup3TransitionProgress) {

        // 计算右平行线的斜率/斜边/底边/对边/角度
        float rightX1 = mViewCenter + mMLegWidth;
        float rightY1 = mViewCenter + mMLegWidth;
        float rightX2 = mSize;
        float rightY2 = 0;
        float rightY = Math.abs(rightY1 - rightY2);
        float rightX = Math.abs(rightX1 - rightX2);
        float rightAngle = (float) Math.atan2(rightY2 - rightY1, rightX2 - rightX1);                // 角度
        float rightHypotenuse = (float) Math.sqrt(rightY * rightY + rightX * rightX);   // 斜边
        float rightBase = (float) (Math.cos(rightAngle) * rightHypotenuse);     // 底边
        float rightOpposite = (float) (Math.sin(rightAngle) * rightHypotenuse); // 对边

        Log.d(TAG, "drawRightMLegPath() called with: " + "rightAngle = [" + rightAngle + "], " +
                "rightHypotenuse = [" + rightHypotenuse + "]" +
                "rightBase = [" + rightBase + "]" +
                "rightOpposite = [" + rightOpposite + "]");

        // 计算左平行线的斜率/斜边/底边/对边/角度
        float leftX1 = mViewCenter - mMLegWidth;
        float leftY1 = mViewCenter + mMLegWidth;
        float leftX2 = mSize - mLegWidth;
        float leftY2 = 0;
        float leftY = Math.abs(leftY1 - leftY2);
        float leftX = Math.abs(leftX1 - leftX2);
        float leftAngle = (float) Math.atan2(leftY2 - leftY1, leftX2 - leftX1);                // 角度
        float leftHypotenuse = (float) Math.sqrt(leftY * leftY + leftX * leftX);   // 斜边
        float leftBase = (float) (Math.cos(leftAngle) * leftHypotenuse);     // 底边
        float leftOpposite = (float) (Math.sin(leftAngle) * leftHypotenuse); // 对边

        Log.d(TAG, "drawRightMLegPath() called with: " + "leftAngle = [" + leftAngle + "], " +
                "leftHypotenuse = [" + leftHypotenuse + "]" +
                "leftBase = [" + leftBase + "]" +
                "leftOpposite = [" + leftOpposite + "]");

        // 根据左右平行线的结果，绘制四边形
        float leftTopX = mSize - mLegWidth;
        float leftTopY = 0;
        float rightTopX = mSize;
        float rightTopY = 0;
        float leftBottomX = leftTopX - (leftBase * setup3TransitionProgress) + 10;
        float leftBottomY = leftTopY - (leftOpposite * setup3TransitionProgress) - 10;
        float rightBottomX = rightTopX - (rightBase * setup3TransitionProgress) + 10;
        float rightBottomY = rightTopY - (rightOpposite * setup3TransitionProgress) - 10;

        mRightMArmPath.moveTo(rightBottomX, rightBottomY);
        mRightMArmPath.lineTo(leftBottomX, leftBottomY);
        mRightMArmPath.lineTo(leftTopX, leftTopY);
        mRightMArmPath.lineTo(rightTopX, rightTopY);
        mRightMArmPath.close();
        canvas.drawPath(mRightMArmPath, mPaint);
    }

    private void drawLeftMLegPath(Canvas canvas, float setup3TransitionProgress) {
        // 计算左平行线的斜率/斜边/底边/对边/角度
        float leftX1 = mViewCenter - mMLegWidth;
        float leftY1 = mViewCenter + mMLegWidth;
        float leftX2 = 0;
        float leftY2 = 0;
        float leftY = Math.abs(leftY1 - leftY2);
        float leftX = Math.abs(leftX1 - leftX2);
        float leftAngle = (float) Math.atan2(leftY1 - leftY2, leftX1 - leftX2);                // 角度
        float leftHypotenuse = (float) Math.sqrt(leftY * leftY + leftX * leftX);   // 斜边
        float leftBase = (float) (Math.cos(leftAngle) * leftHypotenuse);     // 底边
        float leftOpposite = (float) (Math.sin(leftAngle) * leftHypotenuse); // 对边

        // 计算右平行线的斜率/斜边/底边/对边/角度
        float rightX1 = mViewCenter + mMLegWidth;
        float rightY1 = mViewCenter + mMLegWidth;
        float rightX2 = mLegWidth;
        float rightY2 = 0;
        float rightY = Math.abs(rightY1 - rightY2);
        float rightX = Math.abs(rightX1 - rightX2);
        float rightAngle = (float) Math.atan2(rightY1 - rightY2, rightX1 - rightX2);                // 角度
        float rightHypotenuse = (float) Math.sqrt(rightY * rightY + rightX * rightX);   // 斜边
        float rightBase = (float) (Math.cos(rightAngle) * rightHypotenuse);     // 底边
        float rightOpposite = (float) (Math.sin(rightAngle) * rightHypotenuse); // 对边

        // 根据左右平行线的结果，绘制四边形

        float leftBottomX = mViewCenter - mMLegWidth;
        float leftBottomY = mViewCenter + mMLegWidth;

        float rightBottomX = mViewCenter + mMLegWidth;
        float rightBottomY = mViewCenter + mMLegWidth;

        float leftTopX = leftBottomX - (leftBase * setup3TransitionProgress);
        float leftTopY = leftBottomY - (leftOpposite * setup3TransitionProgress);
        float rightTopX = rightBottomX - (rightBase * setup3TransitionProgress);
        float rightTopY = rightBottomY - (rightOpposite * setup3TransitionProgress);

        mLeftMArmPath.moveTo(rightBottomX, rightBottomY);
        mLeftMArmPath.lineTo(leftBottomX, leftBottomY);
        mLeftMArmPath.lineTo(leftTopX, leftTopY);
        mLeftMArmPath.lineTo(rightTopX, rightTopY);
        mLeftMArmPath.close();
        canvas.drawPath(mLeftMArmPath, mPaint);
    }

    private void drawRightMLeg(Canvas canvas) {
        mRightPath.moveTo(mSize, 0);
        mRightPath.lineTo(mSize, mSize);
        mRightPath.lineTo(mSize - mLegWidth, mSize);
        mRightPath.lineTo(mSize - mLegWidth, 0);
        mRightPath.close();
        canvas.drawPath(mRightPath, mPaint);
    }

    private void drawLeftMLeg(Canvas canvas) {
        mLeftPath.moveTo(0, 0);
        mLeftPath.lineTo(mLegWidth, 0);
        mLeftPath.lineTo(mLegWidth, mSize);
        mLeftPath.lineTo(0, mSize);
        mLeftPath.close();
        canvas.drawPath(mLeftPath, mPaint);
    }

    private void drawSetup2RightPath(Canvas canvas, float transitionProgress, float base, float opposite) {

        Log.d(TAG, "drawSetup2RightPath() called with: " + "canvas = [" + canvas + "], transitionProgress = [" + transitionProgress + "], base = [" + base + "], opposite = [" + opposite + "]");

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

        Log.d(TAG, "drawSetup2LeftPath() called with: " + "canvas = [" + canvas + "], transitionProgress = [" + transitionProgress + "], base = [" + base + "], opposite = [" + opposite + "]");

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
        mRectangleWidth = mSize / RATIO_MODE_NONE_RECTANGLE_WIDTH;
        mViewCenter = mSize / 2;
        mLegWidth = mSize / RATIO_LEG_WIDTH;
        mMLegWidth = mLegWidth / FACTOR_M_LEG;

        Log.d(TAG, "initializeValues() called with:" + "mSize = [" + mSize + "] mLegWidth = [" + mLegWidth + "] mRectangleWidth  = [" + mRectangleWidth + "] mViewCenter = [" + mViewCenter + "]");
    }

    public void resetValues() {
        mSize = 0;
        mLegWidth = 0;
        mRectangleWidth = 0;
        mViewCenter = 0;
        mCurrentMode = MODE_NONE;
        mTransitionProgress = 0.0f;
        mMLegWidth = 0.0f;

        initializeValues();
    }

    public void setTransitionProgress(float transitionProgress) {
        mTransitionProgress = transitionProgress;
        Log.d(TAG, "setTransitionProgress() called with: " + "transitionProgress = [" + transitionProgress + "]");
        postInvalidate();
    }

    private float d2x(float size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getContext().getResources().getDisplayMetrics());
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}
