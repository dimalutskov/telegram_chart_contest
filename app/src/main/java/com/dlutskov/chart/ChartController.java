package com.dlutskov.chart;

import com.dlutskov.chart.data.ChartData;
import com.dlutskov.chart.view.ChartCheckBoxesContainer;
import com.dlutskov.chart_lib.ChartFullView;
import com.dlutskov.chart_lib.ChartPreviewView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;
import com.dlutskov.chart_lib.drawers.ChartBarsDrawer;
import com.dlutskov.chart_lib.drawers.ChartStackedBarsDrawer;

public class ChartController implements ChartPreviewView.Listener, ChartCheckBoxesContainer.Listener  {

    private static final float CHART_PREVIEW_MIN_SELECTED_WIDTH = 0.2f;

    private final ChartData mChartData;

    private final ChartFullView<DateCoordinate, LongCoordinate> mChartView;
    private final ChartPreviewView<DateCoordinate, LongCoordinate> mChartPreview;
    private final ChartCheckBoxesContainer mCheckBoxesContainer;

    ChartController(ChartData chartData, ChartFullView<DateCoordinate, LongCoordinate> chartView, ChartPreviewView<DateCoordinate,
            LongCoordinate> chartPreview, ChartCheckBoxesContainer checkBoxesContainer) {
        mChartData = chartData;
        mChartView = chartView;
        mChartPreview = chartPreview;
        mCheckBoxesContainer = checkBoxesContainer;

        mChartPreview.setListener(this);
        mCheckBoxesContainer.setListener(this);
    }

    void showChart() {
        ChartLinesData<DateCoordinate, LongCoordinate> chartData = mChartData.getOverviewData();
        initChartDrawers(chartData);
        int pointsSize = chartData.getXPoints().getPoints().size();
        int leftBound = (int) (pointsSize * (1 - CHART_PREVIEW_MIN_SELECTED_WIDTH));
        int rightBound = pointsSize - 1;
        // Update full chart
        mChartView.updateChartData(chartData, leftBound, rightBound);
        // Update preview chart with default bounds
        mChartPreview.updateChartData(chartData);
        mChartPreview.updateSelectedAreaBounds(leftBound, rightBound, rightBound - leftBound);
        // Create new checkboxes for new data
        mCheckBoxesContainer.createCheckBoxes(chartData);
    }

    private void initChartDrawers(ChartLinesData<DateCoordinate, LongCoordinate> chartData) {
        if (chartData.isStacked()) {
            mChartView.setMinYValue(LongCoordinate.valueOf(0));
            mChartPreview.setMinYValue(LongCoordinate.valueOf(0));
        }

        if (chartData.getYPoints().get(0).getType().equals(ChartData.CHART_TYPE_BAR)) {
            if (chartData.isStacked()) {
                mChartView.setPointsDrawer(new ChartStackedBarsDrawer<>(mChartView));
                mChartPreview.setPointsDrawer(new ChartStackedBarsDrawer<>(mChartPreview));
            } else {
                mChartView.setPointsDrawer(new ChartBarsDrawer<>(mChartView));
                mChartPreview.setPointsDrawer(new ChartStackedBarsDrawer<>(mChartPreview));
            }
        }
    }

    void applyCurrentColors(AppDesign.Theme curTheme, boolean animate) {
        int duration = animate ? MainActivity.APP_MODE_ANIM_DURATION : 0;

        AppDesign.Theme prevTheme = curTheme.invertedTheme();

        AppDesign.applyColorWithAnimation(AppDesign.bgActivity(prevTheme),
                AppDesign.bgActivity(curTheme), duration, updatedColor -> {
                    mChartView.getPointsDetailsDrawer().setPointCircleBackground(updatedColor);
                    mChartView.getXLabelsDrawer().setBackgroundColor(updatedColor);
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
                    mCheckBoxesContainer.setTextColor(updatedColor);
                });
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
