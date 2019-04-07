package com.dlutskov.chart.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart.AppDesign;

import java.util.List;

/**
 * Contains tabs related to each chart in charts data
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartTabsView<X extends ChartCoordinate, Y extends ChartCoordinate> extends LinearLayout implements View.OnClickListener {

    /**
     * Used to notify about ChartLinesData's related tab selected
     */
    public interface Listener<X extends ChartCoordinate, Y extends ChartCoordinate> {
        void onChartTabSelected(ChartLinesData<X, Y> chartData);
    }

    private TextView mSelectedTab;

    private Listener mListener;

    public ChartTabsView(Context context) {
        super(context);
    }

    public void createTabs(List<ChartLinesData<X, Y>> chartData, int selectedTabIndex) {
        // Remove previous data views (consider caching)
        removeAllViews();

        ColorStateList colors = AppDesign.getTabTextColor();
        for (int i = 0; i < chartData.size(); i++) {
            TextView tabView = createTabView(getContext());
            tabView.setText("Chart " + (i + 1));
            tabView.setTag(chartData.get(i));
            tabView.setTextColor(colors);
            tabView.setOnClickListener(this);
            if (i == selectedTabIndex) {
                mSelectedTab = tabView;
                tabView.setSelected(true);
                tabView.setTypeface(null, Typeface.BOLD);
            } else {
                tabView.setSelected(false);
                tabView.setTypeface(null, Typeface.NORMAL);
            }
            addView(tabView);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mSelectedTab) {
            return;
        }
        if (mSelectedTab != null) {
            mSelectedTab.setSelected(false);
            mSelectedTab.setTypeface(null, Typeface.NORMAL);
        }
        mSelectedTab = (TextView) v;
        mSelectedTab.setSelected(true);
        mSelectedTab.setTypeface(null, Typeface.BOLD);
        if (mListener != null) {
            mListener.onChartTabSelected((ChartLinesData) mSelectedTab.getTag());
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private static TextView createTabView(Context context) {
        TextView tabView = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        tabView.setLayoutParams(layoutParams);
        tabView.setGravity(Gravity.CENTER);
        tabView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        return tabView;
    }

}
