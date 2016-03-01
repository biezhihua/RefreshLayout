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
public class RefreshRlView extends RelativeLayout {

    private int mRefreshViewHeight;
    private int mRefreshViewColor;
    private RefreshView mRefreshView;

    public RefreshRlView(Context context) {
        this(context, null);
    }

    public RefreshRlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshRlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout, 0, 0);
            mRefreshViewColor = a.getColor(R.styleable.RefreshLayout_RefreshViewColor, ContextCompat.getColor(context, android.R.color.holo_blue_dark));
            mRefreshViewHeight = (int) a.getDimension(R.styleable.RefreshLayout_RefreshViewHeight, d2x(RefreshLayout.DEFAULT_REFRESH_VIEW_HEIGHT));
            a.recycle();
        }

        mRefreshView = new RefreshView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.height = mRefreshViewHeight;
        params.width = mRefreshViewHeight;
        mRefreshView.setLayoutParams(params);
        mRefreshView.setColor(mRefreshViewColor);
        addView(mRefreshView);
    }

    private float d2x(float size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getContext().getResources().getDisplayMetrics());
    }
}
