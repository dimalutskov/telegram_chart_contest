package com.dlutskov.chart.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChartHeaderView extends LinearLayout {

    private TextView mTitleView;
    private TextView mSelectedAreaTitleView;

    public ChartHeaderView(Context context) {
        super(context);
        init();
    }

    public ChartHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ChartHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);

        mTitleView = new TextView(getContext());
        LayoutParams titleParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.gravity = Gravity.CENTER_VERTICAL;
        titleParams.weight = 1;
        mTitleView.setLayoutParams(titleParams);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        mTitleView.setTextColor(Color.BLACK);
        mTitleView.setTypeface(null, Typeface.BOLD);

        mSelectedAreaTitleView = new TextView(getContext());
        LayoutParams areaTitleAprams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        areaTitleAprams.gravity = Gravity.CENTER_VERTICAL;
        mSelectedAreaTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        mSelectedAreaTitleView.setTextColor(Color.BLACK);
        mSelectedAreaTitleView.setLayoutParams(areaTitleAprams);
        mSelectedAreaTitleView.setTypeface(null, Typeface.BOLD);

        addView(mTitleView);
        addView(mSelectedAreaTitleView);
    }

    public void setTitleText(String text) {
        mTitleView.setText(text);
    }

    public void setAreaTitleText(String text) {
        mSelectedAreaTitleView.setText(text);
    }

    public void setAreaTitleColor(int color) {
        mSelectedAreaTitleView.setTextColor(color);
    }

    public void setTitleColor(int color) {
        mTitleView.setTextColor(color);
    }

    public void setTitleClickListener(View.OnClickListener clickListener) {
        mTitleView.setOnClickListener(clickListener);
    }

}
