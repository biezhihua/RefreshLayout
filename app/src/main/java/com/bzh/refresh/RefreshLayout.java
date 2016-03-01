package com.bzh.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
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

    public static final float DEFAULT_REFRESH_VIEW_MAX_HEIGHT = 100;
    public static final float DEFAULT_REFRESH_VIEW_HEIGHT = 40;
    private float mRefreshViewMaxHeight;
    private float mRefreshViewHeight;
    private int mRefreshViewColor;
    private int mTouchSlop;
    private View mListView;
    private RefreshRlView mRefreshRlView;
    private float mTouchStartY;
    private float mTouchCurrentY;
    private boolean yRefreshing = false;

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
            mRefreshViewColor = a.getColor(R.styleable.RefreshLayout_RefreshViewColor, ContextCompat.getColor(context, android.R.color.holo_blue_dark));
            mRefreshViewMaxHeight = a.getDimension(R.styleable.RefreshLayout_RefreshViewMaxHeight, d2x(DEFAULT_REFRESH_VIEW_MAX_HEIGHT));
            mRefreshViewHeight = a.getDimension(R.styleable.RefreshLayout_RefreshViewHeight, d2x(DEFAULT_REFRESH_VIEW_HEIGHT));
            a.recycle();
        }

        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));

        mRefreshRlView = new RefreshRlView(context, attrs);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.height = (int) mRefreshViewHeight;
        mRefreshRlView.setLayoutParams(params);
        mRefreshRlView.setPadding(0, 0, 0, (int) mRefreshViewHeight);
        super.addView(mRefreshRlView);
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
