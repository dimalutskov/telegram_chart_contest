package com.dlutskov.chart.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;


/**
 * Contains ChartCheckBox views related to collection of ChartPointsData
 */
public class ChartCheckBoxesContainer extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {

    /**
     * Used to notify about ChartPointsData's related checkbox state changed
     */
    public interface Listener {
        /**
         * @param id id of ChartPointsData related to changed checkbox
         * @param checked - New checkbox state
         */
        void onChartLineCheckBoxStateChanged(String id, boolean checked);

    }

    private Listener mListener;

    private int mCheckBoxTextColor = Color.WHITE;

    private int mMargin = ChartUtils.getPixelForDp(getContext(), 4);

    public ChartCheckBoxesContainer(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        int parentWidth = getMeasuredWidth();

        int height = 0;
        int width = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (height == 0) {
                height = child.getMeasuredHeight();
            }
            width += child.getMeasuredWidth() + mMargin;
            if (width > parentWidth) {
                // Move to new row
                width = child.getMeasuredWidth() + mMargin;
                height += child.getMeasuredHeight() + mMargin;
            }
        }

        setMeasuredDimension(getMeasuredWidth(), height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int parentWidth = getMeasuredWidth();

        int height = 0;
        int width = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (width + childWidth > parentWidth) {
                // Move to new row
                width = 0;
                height += child.getMeasuredHeight() + mMargin;
            }
            int l = width == 0 ? 0 : width + mMargin;
            int r = l + childWidth;
            child.layout(l, height, l + childWidth, height + childHeight);
            width = r;
        }

    }

    public void createCheckBoxes(ChartLinesData<DateCoordinate, LongCoordinate> chartData) {
        // Clear previous views (consider caching)
        removeAllViews();

        int height = ChartUtils.getPixelForDp(getContext(), 30);
        for (int i = 0; i < chartData.getYPoints().size(); i++) {
            ChartPointsData<LongCoordinate> lineData = chartData.getYPoints().get(i);
            ChartCheckBox toggleView = createToggleView(getContext());
            LayoutParams params = (LayoutParams) toggleView.getLayoutParams();
            params.height = height;
            toggleView.setText(lineData.getName());
            toggleView.setColor(lineData.getColor());
            toggleView.setCheckedTextColor(mCheckBoxTextColor);
            toggleView.setTag(lineData);
            toggleView.setChecked(true);
            toggleView.setOnClickListener(this);
            toggleView.setOnLongClickListener(this);
            addView(toggleView);
        }
    }

    @Override
    public void onClick(View v) {
        ChartCheckBox checkBox = (ChartCheckBox) v;
        checkBox.toggle();
        if (mListener != null) {
            ChartPointsData chartPointsData = (ChartPointsData) checkBox.getTag();
            mListener.onChartLineCheckBoxStateChanged(chartPointsData.getId(), checkBox.isChecked());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        for (int i = 0; i < getChildCount(); i++) {
            ChartCheckBox checkBox = (ChartCheckBox) getChildAt(i);
            boolean checked = checkBox == v;
            if (mListener != null && checked != checkBox.isChecked()) {
                ChartPointsData chartPointsData = (ChartPointsData) checkBox.getTag();
                mListener.onChartLineCheckBoxStateChanged(chartPointsData.getId(), checked);
            }
            checkBox.setChecked(checked);
        }
        return true;
    }

    public void setTextColor(int textColor) {
        mCheckBoxTextColor = textColor;
        for (int i = 0; i < getChildCount(); i++) {
            ChartCheckBox view = (ChartCheckBox)getChildAt(i);
            view.setCheckedTextColor(mCheckBoxTextColor);
            view.invalidate();
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private static ChartCheckBox createToggleView(Context context) {
        ChartCheckBox checkBox = new ChartCheckBox(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        checkBox.setLayoutParams(params);
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        return checkBox;
    }
}
