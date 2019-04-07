package com.dlutskov.chart.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Contains ChartCheckBox views related to collection of ChartPointsData
 */
public class ChartCheckBoxesContainer extends LinearLayout implements View.OnClickListener {

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

    private int mCheckBoxTextColor = Color.BLACK;

    public ChartCheckBoxesContainer(Context context) {
        super(context);
    }

    public void createCheckBoxes(ChartLinesData<DateCoordinate, LongCoordinate> chartData) {
        // Clear previous views (consider caching)
        removeAllViews();

        int itemTopMargin = ChartUtils.getPixelForDp(getContext(), 16);
        int height = ChartUtils.getPixelForDp(getContext(), 24);
        for (int i = 0; i < chartData.getYPoints().size(); i++) {
            ChartPointsData<LongCoordinate> lineData = chartData.getYPoints().get(i);
            ChartCheckBox toggleView = createToggleView(getContext());
            LayoutParams params = (LayoutParams) toggleView.getLayoutParams();
            params.height = height;
            params.topMargin = i == 0 ? 0 : itemTopMargin;
            toggleView.setText(lineData.getName());
            toggleView.setColor(lineData.getColor());
            toggleView.getPaint().setColor(mCheckBoxTextColor);
            toggleView.setTag(lineData);
            toggleView.setChecked(true);
            toggleView.setOnClickListener(this);
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

    public void setTextColor(int textColor) {
        mCheckBoxTextColor = textColor;
        for (int i = 0; i < getChildCount(); i++) {
            TextView view = (TextView)getChildAt(i);
            view.getPaint().setColor(textColor);
            view.invalidate();
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private static ChartCheckBox createToggleView(Context context) {
        ChartCheckBox checkBox = new ChartCheckBox(context);
        LinearLayout.LayoutParams params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        checkBox.setLayoutParams(params);
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        return checkBox;
    }
}
