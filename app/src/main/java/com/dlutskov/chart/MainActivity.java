package com.dlutskov.chart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dlutskov.chart.data.ChartDataParser;
import com.dlutskov.chart.view.ChartCheckBoxesContainer;
import com.dlutskov.chart.view.ChartTabsView;
import com.dlutskov.chart.view.MoonIconView;
import com.dlutskov.chart_lib.ChartFullView;
import com.dlutskov.chart_lib.ChartPreviewView;
import com.dlutskov.chart_lib.ChartUtils;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Example of using custom created {@link ChartView} for Telegram Contests
 * Activity's layout and all app resources are created programmatically to reduce apk size
 */
public class MainActivity extends Activity implements
        ChartTabsView.Listener<DateCoordinate, LongCoordinate>,
        ChartPreviewView.Listener,
        ChartCheckBoxesContainer.Listener {

    /**
     * General padding in DP which is used by most of the activity's views
     */
    private static final int PADDING_GENERAL = 16;

    private static final float CHART_PREVIEW_MIN_SELECTED_WIDTH = 0.2f;

    private static final int APP_MODE_ANIM_DURATION = 250;

    private LinearLayout mRootView;

    private ViewGroup mHeaderLayout;
    private MoonIconView mIconMoon;

    private ChartTabsView<DateCoordinate, LongCoordinate> mChartTabs;
    private ChartCheckBoxesContainer mChartCheckBoxesContainer;

    private ChartFullView<DateCoordinate, LongCoordinate> mChartView;
    private ChartPreviewView<DateCoordinate, LongCoordinate> mChartPreview;

    private List<ChartLinesData<DateCoordinate, LongCoordinate>> mChartData;
    private ChartLinesData<DateCoordinate, LongCoordinate> mCurrentChart;

    private AppDesign.Theme mTheme = AppDesign.Theme.DAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Root Container
        mRootView = new LinearLayout(this);
        mRootView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        mRootView.setOrientation(LinearLayout.VERTICAL);

        // Header Container
        mHeaderLayout = createHeaderView(this);
        mIconMoon = (MoonIconView) mHeaderLayout.getChildAt(1);
        mIconMoon.setOnClickListener(v -> {
            mTheme = mTheme.invertedTheme();
            applyCurrentColors(true);
        });
        mRootView.addView(mHeaderLayout);

        // Tabs Container
        mChartTabs = createChartTabsView(this);
        mChartTabs.setListener(this);
        mRootView.addView(mChartTabs);

        // Chart Full View
        mChartView = createChartFullView(this);
        mRootView.addView(mChartView);

        // Chart Preview
        mChartPreview = createChartPreviewView(this);
        mChartPreview.setListener(this);
        mRootView.addView(mChartPreview);

        // Chart CheckBoxes
        mChartCheckBoxesContainer = createCheckboxesContainer(this);
        mChartCheckBoxesContainer.setListener(this);
        mRootView.addView(mChartCheckBoxesContainer);

        setContentView(mRootView);

        applyCurrentColors(false);

        showChartsData();
    }

    private void showChartsData() {
        mChartData = ChartDataParser.parse(this);
        // Display first chart by default
        mCurrentChart = mChartData.get(0);
        mChartTabs.createTabs(mChartData, 0);

        showChart(mCurrentChart);
    }

    private static ViewGroup createHeaderView(Context ctx) {
        // Container
        LinearLayout containerView = new LinearLayout(ctx);
        int headerHeight = ChartUtils.getPixelForDp(ctx, 56);
        containerView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, headerHeight));
        containerView.setOrientation(LinearLayout.HORIZONTAL);
        int padding = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        containerView.setPadding(padding, 0, padding, 0);

        TextView titleView = new TextView(ctx);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT);
        textParams.weight = 1;
        textParams.gravity = Gravity.CENTER_VERTICAL;
        titleView.setLayoutParams(textParams);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        titleView.setTextColor(Color.WHITE);
        titleView.setText("Telegram Chart Contest");
        containerView.addView(titleView);

        MoonIconView moonIconView = new MoonIconView(ctx);
        int moonIconSize = ChartUtils.getPixelForDp(ctx, 24);
        LinearLayout.LayoutParams moonIconParams = new LinearLayout.LayoutParams(moonIconSize, moonIconSize);
        moonIconParams.gravity = Gravity.CENTER_VERTICAL;
        moonIconView.setLayoutParams(moonIconParams);
        containerView.addView(moonIconView);

        return containerView;
    }

    private static ChartTabsView createChartTabsView(Context ctx) {
        ChartTabsView view = new ChartTabsView(ctx);
        view.setOrientation(LinearLayout.HORIZONTAL);
        int height = ChartUtils.getPixelForDp(ctx, 50);
        int margin = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL / 2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, height);
        params.leftMargin = margin;
        params.rightMargin = margin;
        view.setLayoutParams(params);
        return view;
    }

    private static ChartFullView<DateCoordinate, LongCoordinate> createChartFullView(Context ctx) {
        ChartFullView<DateCoordinate, LongCoordinate> view = new ChartFullView<>(ctx);
        int height = ChartUtils.getPixelForDp(ctx, 220);
        int margin = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, height);
        params.leftMargin = margin;
        params.rightMargin = margin;
        view.setLayoutParams(params);
        // Styling
        view.setMinYValue(LongCoordinate.valueOf(0));
        view.setLabelsTextSize(ChartUtils.getPixelForDp(ctx, 12));
        return view;
    }

    private static ChartPreviewView<DateCoordinate, LongCoordinate> createChartPreviewView(Context ctx) {
        ChartPreviewView<DateCoordinate, LongCoordinate> view = new ChartPreviewView<>(ctx);
        int height = ChartUtils.getPixelForDp(ctx, 40);
        int margin = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, height);
        params.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(params);
        // Styling
        view.setMinYValue(LongCoordinate.valueOf(0));
        return view;
    }

    private static ChartCheckBoxesContainer createCheckboxesContainer(Context ctx) {
        ChartCheckBoxesContainer view = new ChartCheckBoxesContainer(ctx);
        view.setOrientation(LinearLayout.VERTICAL);
        int margin = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.topMargin = ChartUtils.getPixelForDp(ctx, 4);
        params.leftMargin = margin;
        params.rightMargin = margin;
        view.setLayoutParams(params);
        return view;
    }

    private void applyCurrentColors(boolean animate) {
        int duration = animate ? APP_MODE_ANIM_DURATION : 0;

        AppDesign.Theme prevTheme = mTheme.invertedTheme();
        AppDesign.Theme curTheme = mTheme;

        AppDesign.applyColorWithAnimation(AppDesign.bgActivity(prevTheme),
                AppDesign.bgActivity(curTheme), duration, updatedColor -> {
                    mRootView.setBackgroundColor(updatedColor);
                    mChartView.getPointsDetailsDrawer().setPointCircleBackground(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.bgHeader(prevTheme),
                AppDesign.bgHeader(curTheme), duration, updatedColor -> {
                    mHeaderLayout.setBackgroundColor(updatedColor);
                    mIconMoon.setBackgroundColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.chartGridColor(prevTheme),
                AppDesign.chartGridColor(curTheme), duration, updatedColor -> {
                    mChartView.getYLabelsDrawer().setGridColor(updatedColor);
                    mChartView.getPointsDetailsDrawer().setBackgroundBorderColor(updatedColor);
                    mChartView.getPointsDetailsDrawer().setDividerColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.textColorChartLabels(prevTheme),
                AppDesign.textColorChartLabels(curTheme), duration, updatedColor -> {
                    mChartView.setLabelsTextColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.bgChartPointsDetails(prevTheme),
                AppDesign.bgChartPointsDetails(curTheme), duration, updatedColor -> {
                    mChartView.getPointsDetailsDrawer().setBackgroundColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.chartPointsDetailsXLabel(prevTheme),
                AppDesign.chartPointsDetailsXLabel(curTheme), duration, updatedColor -> {
                    mChartView.getPointsDetailsDrawer().setXLabelColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.bgChartPreview(prevTheme),
                AppDesign.bgChartPreview(curTheme), duration, updatedColor -> {
                    mChartPreview.setBackgroundColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.bgChartPreviewUnselectedArea(prevTheme),
                AppDesign.bgChartPreviewUnselectedArea(curTheme), duration, updatedColor -> {
                    mChartPreview.setUnselectedBackgroundColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.bgChartPreviewSelectedArea(prevTheme),
                AppDesign.bgChartPreviewSelectedArea(curTheme), duration, updatedColor -> {
                    mChartPreview.setSelectedBackgroundColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.chartPreviewBordersColor(prevTheme),
                AppDesign.chartPreviewBordersColor(curTheme), duration, updatedColor -> {
                    mChartPreview.setAreaBordersColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.textCheckBox(prevTheme),
                AppDesign.textCheckBox(curTheme), duration, updatedColor -> {
                    mChartCheckBoxesContainer.setTextColor(updatedColor);
                });
    }

    private void showChart(ChartLinesData<DateCoordinate, LongCoordinate> chartData) {
        int pointsSize = chartData.getXPoints().getPoints().size();
        int leftBound = (int) (pointsSize * (1 - CHART_PREVIEW_MIN_SELECTED_WIDTH));
        int rightBound = pointsSize - 1;
        // Update full chart
        mChartView.updateChartData(chartData, leftBound, rightBound);
        // Update preview chart with default bounds
        mChartPreview.updateChartData(chartData);
        mChartPreview.updateSelectedAreaBounds(leftBound, rightBound, rightBound - leftBound);
        // Create new checkboxes for new data
        mChartCheckBoxesContainer.createCheckBoxes(chartData);
    }

    ////////////////// CALLBACKS //////////////////
    @Override
    public void onChartTabSelected(ChartLinesData<DateCoordinate, LongCoordinate> chartData) {
        showChart(chartData);
    }

    @Override
    public void onChartPreviewAreaChanged(int minXIndex, int maxXIndex) {
        mChartView.updateHorizontalBounds(minXIndex, maxXIndex);
    }

    @Override
    public void onChartLineCheckBoxStateChanged(String id, boolean checked) {
        mChartView.updatePointsVisibility(id, checked);
        mChartPreview.updatePointsVisibility(id, checked);
    }

}