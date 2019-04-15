package com.dlutskov.chart.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;


/**
 * Contains ChartCheckBox views related to collection of ChartPointsData
 */
public class ChartCheckBoxesContainer extends LinearLayout implements View.OnClickListener, View.OnLongClickListener {

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
        setOrientation(VERTICAL);
    }

    public void createCheckBoxes(ChartLinesData<DateCoordinate, LongCoordinate> chartData) {
        if (getWidth() == 0) {
            post(() -> createCheckBoxes(chartData));
            return;
        }

        // Clear previous views (consider caching)
        removeAllViews();

        float measuredWidth = 0;

        int height = ChartUtils.getPixelForDp(getContext(), 30);
        LinearLayout container = createHorizontalContainer(height);
        for (int i = 0; i < chartData.getYPoints().size(); i++) {
            ChartPointsData<LongCoordinate> lineData = chartData.getYPoints().get(i);
            ChartCheckBox toggleView = createToggleView(getContext(), height, mMargin);
            toggleView.setText(lineData.getName());
            toggleView.setColor(lineData.getColor());
            toggleView.setCheckedTextColor(mCheckBoxTextColor);
            toggleView.setTag(lineData);
            toggleView.setChecked(true);
            toggleView.setOnClickListener(this);
            toggleView.setOnLongClickListener(this);

            measuredWidth += toggleView.measureWidth(height) + mMargin;
            if (measuredWidth > getWidth()) {
                // Move to new row
                addView(container);
                container = createHorizontalContainer(height);
                measuredWidth = toggleView.measureWidth(height) + mMargin;
            }
            container.addView(toggleView);
        }
        addView(container);

        getLayoutParams().height = (height + mMargin) * getChildCount();
        requestLayout();
    }

    private LinearLayout createHorizontalContainer(int height) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        params.bottomMargin = mMargin;
        params.rightMargin = mMargin;
        container.setLayoutParams(params);
        return container;
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
            ViewGroup child = (ViewGroup) getChildAt(i);
            for (int j = 0; j < child.getChildCount(); j++) {
                ChartCheckBox checkBox = (ChartCheckBox) child.getChildAt(j);
                boolean checked = checkBox == v;
                if (mListener != null && checked != checkBox.isChecked()) {
                    ChartPointsData chartPointsData = (ChartPointsData) checkBox.getTag();
                    mListener.onChartLineCheckBoxStateChanged(chartPointsData.getId(), checked);
                }
                checkBox.setChecked(checked);
            }
        }
        return true;
    }

    public void setTextColor(int textColor) {
        mCheckBoxTextColor = textColor;
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup child = (ViewGroup) getChildAt(i);
            for (int j = 0; j < child.getChildCount(); j++) {
                ChartCheckBox view = (ChartCheckBox)child.getChildAt(j);
                view.setCheckedTextColor(mCheckBoxTextColor);
                view.invalidate();
            }
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private static ChartCheckBox createToggleView(Context context, int height, int rightMargin) {
        ChartCheckBox checkBox = new ChartCheckBox(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height);
        params.rightMargin = rightMargin;
        checkBox.setLayoutParams(params);
        return checkBox;
    }
}
