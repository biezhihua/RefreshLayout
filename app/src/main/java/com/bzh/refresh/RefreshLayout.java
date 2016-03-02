package com.bzh.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;

/**
 * ========================================================== <br>
 * <b>版权</b>：　　　音悦台 版权所有(c) 2016 <br>
 * <b>作者</b>：　　　别志华 zhihua.bie@yinyuetai.com<br>
 * <b>创建日期</b>：　16-3-1 <br>
 * <b>描述</b>：　　　<br>
 * <b>版本</b>：　    V1.0 <br>
 * <b>修订历史</b>：　<br>
 * ========================================================== <br>
 */
public class RefreshLayout extends FrameLayout {

    public interface OnRefreshListener {
        void onRefresh();
    }

    private OnRefreshListener mListener;

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if (refreshing && !mRefreshing) {
            mRefreshing = true;
            isDragRefresh = false;
            mListView.setTranslationY(mRefreshViewHeight);
            mRefreshInnerLayout.getLayoutParams().height = mRefreshViewHeight;
            requestLayout();
            mRefreshInnerLayout.setMode(RefreshView.MODE_SETUP_5);
        } else if (mRefreshing != refreshing) {
            mRefreshing = false;
            mPreListViewY = 0;
            startRefreshViewBackTopAnim();
        }
    }

    private static final String TAG = "RefreshLayout";

    public static final float DEFAULT_REFRESH_VIEW_MAX_HEIGHT = 300;    // 可拖动的默认最大值
    public static final float DEFAULT_REFRESH_VIEW_HEIGHT = 100;         // 刷新控件的默认高度

    private int mRefreshViewMaxHeight;                  // 可拖动的最大值
    private int mRefreshViewHeight;                     // 刷新动画控件的高度

    private boolean mRefreshing;                        // 是否处于刷新状态
    private boolean isDragRefresh;                      // 是否时手势拖动刷新，非手势拖动刷新不响应回调事件

    private float mTouchStartY;                         // 开始触摸的Y点
    private float mTouchCurrentY;                       // 当前触摸的Y点  mTouchCurrentY - mTouchStartY 相减得 移动的距离
    private float mPreListViewY;                        // 记录的mListView在Y轴的偏移量，如果不为0，说明mListView控件已经下移，那么再次响应DOWN事件时，应该加上该偏移量。
    private View mListView;                             // 可滑动的列表：ListView 或者 RecyclerView
    private RefreshInnerLayout mRefreshInnerLayout;     // 刷新动画的控件
    private ValueAnimator mBackRefreshViewHeightAnim;   // 返回到刷新控件高度的动画
    private ValueAnimator mRefreshViewBackTopAnim;      // 从刷新控件高度返回到顶部的动画
    private ValueAnimator mBackTopAnim;                 // 从刷新控件尚未显示完全的位置返回到顶部的动画


    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (getChildCount() > 1) {
            throw new RuntimeException("you can only attach one child");
        }

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout, 0, 0);
            mRefreshViewMaxHeight = (int) a.getDimension(R.styleable.RefreshLayout_RefreshViewMaxHeight, d2x(DEFAULT_REFRESH_VIEW_MAX_HEIGHT));
            mRefreshViewHeight = (int) a.getDimension(R.styleable.RefreshLayout_RefreshViewHeight, d2x(DEFAULT_REFRESH_VIEW_HEIGHT));
            a.recycle();
        }

        initRefreshView(context, attrs);
        initAnimation();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = null;
        ViewGroup vp = (ViewGroup) getChildAt(1);
        if (vp instanceof AbsListView || vp instanceof RecyclerView) {
            view = vp;
        }
        if (view == null) {
            view = getRefreshView(vp);
        }
        if (view == null) {
            throw new IllegalArgumentException("没有可以滚动的View");
        } else {
            mListView = view;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartY = ev.getY();
                mTouchCurrentY = mTouchStartY;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = ev.getY() - mTouchStartY;
                if (dy >= 0 && !canChildScrollUp()) {
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mTouchCurrentY = event.getY();
                float moveDistanceY = Math.max(0, Math.min(mRefreshViewMaxHeight, mTouchCurrentY - mTouchStartY));
                float newMoveDistanceY = Math.round(Math.min(mRefreshViewMaxHeight, mPreListViewY + moveDistanceY)); // 加上之前的偏移量
                mListView.setTranslationY(newMoveDistanceY);
                mRefreshInnerLayout.getLayoutParams().height = (int) newMoveDistanceY;
                requestLayout();
                if (!mRefreshing) {
                    float percent = newMoveDistanceY / (mRefreshViewHeight * 2);
                    mRefreshInnerLayout.setTransitionProgress(Math.min(1, percent));
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                if (mListView.getTranslationY() >= mRefreshViewHeight) {
                    isDragRefresh = true;
                    startBackRefreshViewHeightAnim();
                } else {
                    startBackTopAnim();
                }

                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void initAnimation() {
    }

    private void initRefreshView(Context context, AttributeSet attrs) {
        mRefreshInnerLayout = new RefreshInnerLayout(context, attrs);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        mRefreshInnerLayout.setLayoutParams(params);
        super.addView(mRefreshInnerLayout);
    }

    private void startRefreshViewBackTopAnim() {
        if (mRefreshViewBackTopAnim == null) {
            mRefreshViewBackTopAnim = ValueAnimator.ofFloat(mRefreshViewHeight, 0);
            mRefreshViewBackTopAnim.setInterpolator(new DecelerateInterpolator());
            mRefreshViewBackTopAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mListView.setTranslationY(value);
                    mRefreshInnerLayout.getLayoutParams().height = (int) value;
                    requestLayout();
//                    mRefreshInnerLayout.setPadding(0, 0, 0, (int) (mRefreshViewHeight - value));
                }
            });
            mRefreshViewBackTopAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mRefreshInnerLayout.resetValues();
                }
            });
            mRefreshViewBackTopAnim.setDuration(300);
        } else if (mRefreshViewBackTopAnim.isRunning()) {
            mRefreshViewBackTopAnim.cancel();
        }
        mRefreshViewBackTopAnim.start();
    }

    private void startBackRefreshViewHeightAnim() {
        if (mBackRefreshViewHeightAnim == null) {
            mBackRefreshViewHeightAnim = ValueAnimator.ofFloat(0);
            mBackRefreshViewHeightAnim.setFloatValues(mRefreshViewMaxHeight, mRefreshViewHeight);
            mBackRefreshViewHeightAnim.setInterpolator(new LinearInterpolator());
            mBackRefreshViewHeightAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mListView.setTranslationY(value);
                    mRefreshInnerLayout.getLayoutParams().height = (int) value;
                    requestLayout();

                    if (!mRefreshing) {
                        float percent = (value - mRefreshViewHeight) / (mRefreshViewMaxHeight - mRefreshViewHeight);
                        mRefreshInnerLayout.setTransitionProgress(1 - percent);
                        mRefreshInnerLayout.setMode(RefreshView.MODE_SETUP_4);
                    }
                }
            });
            mBackRefreshViewHeightAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mPreListViewY = mListView.getTranslationY();
                    if (!mRefreshing) {
                        if (mListener != null && isDragRefresh) {
                            mRefreshInnerLayout.setOnRefreshListener(mListener);
                        } else {
                            mRefreshInnerLayout.setOnRefreshListener(null);
                        }
                        mRefreshInnerLayout.setMode(RefreshView.MODE_SETUP_5);
                        mRefreshing = true;
                    }
                }
            });
            mBackRefreshViewHeightAnim.setDuration(300);
        } else if (mBackRefreshViewHeightAnim.isRunning()) {
            mBackRefreshViewHeightAnim.cancel();
        }
        mBackRefreshViewHeightAnim.setFloatValues(mListView.getTranslationY(), mRefreshViewHeight);
        mBackRefreshViewHeightAnim.start();
    }

    private void startBackTopAnim() {
        if (mBackTopAnim == null) {
            mBackTopAnim = ValueAnimator.ofFloat(mRefreshViewHeight, 0);
            mBackTopAnim.setInterpolator(new DecelerateInterpolator());
            mBackTopAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mListView.setTranslationY(value);
                    mRefreshInnerLayout.getLayoutParams().height = (int) value;
                    requestLayout();
//                    mRefreshInnerLayout.setPadding(0, 0, 0, mRefreshViewHeight - Math.round(value));
                }
            });
            mBackTopAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mPreListViewY = mListView.getTranslationY();
                }
            });
            mBackTopAnim.setDuration(300);
        } else if (mBackTopAnim.isRunning()) {
            mBackTopAnim.cancel();
        }
        mBackTopAnim.start();
    }

    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            return mListView.getScrollY() > 0;
        } else {
            return ViewCompat.canScrollVertically(mListView, -1);
        }
    }

    private View getRefreshView(ViewGroup vp) {
        if (vp == null) {
            return null;
        }
        for (int i = 0; i < vp.getChildCount(); i++) {
            View temp = vp.getChildAt(i);
            if (temp instanceof AbsListView || temp instanceof RecyclerView) {
                return temp;
            } else if (temp instanceof ViewGroup) {
                return getRefreshView((ViewGroup) temp);
            }
        }
        return null;
    }

    private float d2x(float size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getContext().getResources().getDisplayMetrics());
    }

}
