package com.bzh.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
class RefreshInnerLayout extends RelativeLayout {

    private int mRefreshViewHeight;
    private RefreshView mRefreshView;

    public RefreshInnerLayout(Context context) {
        this(context, null);
    }

    public RefreshInnerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshInnerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout, 0, 0);
            mRefreshViewHeight = (int) a.getDimension(R.styleable.RefreshLayout_RefreshViewHeight, d2x(RefreshLayout.DEFAULT_REFRESH_VIEW_HEIGHT));
            a.recycle();
        }

        mRefreshView = new RefreshView(getContext(), attrs);
        LayoutParams params = new LayoutParams(mRefreshViewHeight, mRefreshViewHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRefreshView.setLayoutParams(params);
        addView(mRefreshView);
    }

    public void setTransitionProgress(float transitionProgress) {
        mRefreshView.setTransitionProgress(transitionProgress);

    }

    public void setMode(int mode) {
        mRefreshView.setMode(mode);
    }

    public void setOnRefreshListener(RefreshLayout.OnRefreshListener listener) {
        mRefreshView.setOnRefreshListener(listener);
    }

    public void resetValues() {
        mRefreshView.resetValues();
    }

    private float d2x(float size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getContext().getResources().getDisplayMetrics());
    }
}
