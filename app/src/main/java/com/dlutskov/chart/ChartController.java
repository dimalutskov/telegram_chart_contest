package com.dlutskov.chart;

import android.view.View;

import com.dlutskov.chart.data.ChartDataProvider;
import com.dlutskov.chart.view.ChartCheckBoxesContainer;
import com.dlutskov.chart_lib.ChartFullView;
import com.dlutskov.chart_lib.ChartPreviewView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;
import com.dlutskov.chart_lib.drawers.ChartAxisLabelsDrawer;
import com.dlutskov.chart_lib.drawers.ChartBarsDrawer;
import com.dlutskov.chart_lib.drawers.ChartLinesDrawer;
import com.dlutskov.chart_lib.drawers.ChartPercentagesAreasDrawer;
import com.dlutskov.chart_lib.drawers.ChartPointsDrawer;
import com.dlutskov.chart_lib.drawers.ChartScaledLinesDrawer;
import com.dlutskov.chart_lib.drawers.ChartStackedBarsDrawer;
import com.dlutskov.chart_lib.drawers.ChartYAxisLabelsDrawer;

public class ChartController implements
        ChartPreviewView.Listener,
        ChartCheckBoxesContainer.Listener,
        ChartFullView.Listener<DateCoordinate, LongCoordinate> {

    private static final float CHART_PREVIEW_MIN_SELECTED_WIDTH = 0.2f;

    private final MainActivity mActivity;
    private final ChartLinesData<DateCoordinate, LongCoordinate> mChartData;
    private final String mAssetsDataFolder;

    private final ChartFullView<DateCoordinate, LongCoordinate> mChartView;
    private final ChartPreviewView<DateCoordinate, LongCoordinate> mChartPreview;
    private final ChartCheckBoxesContainer mCheckBoxesContainer;

    // TODO Hotfix. For aplying app theme
    private ChartYAxisLabelsDrawer mYRightLabelsDrawer;

    ChartController(MainActivity activity,
                    ChartLinesData<DateCoordinate, LongCoordinate> chartData,
                    String assetsDataFolder,
                    ChartFullView<DateCoordinate, LongCoordinate> chartView,
                    ChartPreviewView<DateCoordinate, LongCoordinate> chartPreview,
                    ChartCheckBoxesContainer checkBoxesContainer) {
        mActivity = activity;
        mChartData = chartData;
        mAssetsDataFolder = assetsDataFolder;
        mChartView = chartView;
        mChartPreview = chartPreview;
        mCheckBoxesContainer = checkBoxesContainer;

        mChartView.setListener(this);
        mChartPreview.setListener(this);
        mCheckBoxesContainer.setListener(this);
    }

    void showChart() {
        initChartDrawers(mChartData);
        int pointsSize = mChartData.getXPoints().getPoints().size();
        int leftBound = (int) (pointsSize * (1 - CHART_PREVIEW_MIN_SELECTED_WIDTH));
        int rightBound = pointsSize - 1;
        // Update full chart
        mChartView.updateChartData(mChartData, leftBound, rightBound);
        // Update preview chart with default bounds
        mChartPreview.updateChartData(mChartData);
        mChartPreview.updateSelectedAreaBounds(leftBound, rightBound, rightBound - leftBound);
        // Create new checkboxes for new data
        mCheckBoxesContainer.createCheckBoxes(mChartData);
    }

    private void initChartDrawers(ChartLinesData<DateCoordinate, LongCoordinate> chartData) {
        String chartType = chartData.getYPoints().get(0).getType();
        if (chartType.equals(ChartLinesData.CHART_TYPE_BAR) || chartType.equals(ChartLinesData.CHART_TYPE_AREA)) {
            mChartView.setMinYValue(LongCoordinate.valueOf(0));
            mChartPreview.setMinYValue(LongCoordinate.valueOf(0));
            if (chartData.isPercentage()) {
                mChartView.setPointsDrawer(new ChartPercentagesAreasDrawer<>(mChartView));
                mChartPreview.setPointsDrawer(new ChartPercentagesAreasDrawer<>(mChartPreview));
            } else if (chartData.isStacked()) {
                mChartView.setPointsDrawer(new ChartStackedBarsDrawer<>(mChartView));
                mChartPreview.setPointsDrawer(new ChartStackedBarsDrawer<>(mChartPreview));
            } else {
                mChartView.setPointsDrawer(new ChartBarsDrawer<>(mChartView));
                mChartPreview.setPointsDrawer(new ChartStackedBarsDrawer<>(mChartPreview));
            }
        } else if (chartData.isYScaled()) {
            mChartPreview.setPointsDrawer(new ChartScaledLinesDrawer<>(mChartPreview));
            mChartView.setPointsDrawer(new ChartScaledLinesDrawer<>(mChartView));

            // Bind yLabelsDrawer to first graph
            ChartPointsData<LongCoordinate> firstGraph = mChartData.getYPoints().get(0);
            mChartView.getYLabelsDrawer().setScaledPointsId(firstGraph.getId(), firstGraph.getColor());

            // Create yLabelsDrawer for the second graph
            mYRightLabelsDrawer = new ChartYAxisLabelsDrawer(mChartView, ChartAxisLabelsDrawer.SIZE_MATCH_PARENT);
            mYRightLabelsDrawer.setSide(ChartYAxisLabelsDrawer.SIDE_RIGHT);
            ChartPointsData<LongCoordinate> secondGraph = mChartData.getYPoints().get(1);
            mYRightLabelsDrawer.setScaledPointsId(secondGraph.getId(), secondGraph.getColor());
            mChartView.addDrawer(mYRightLabelsDrawer);
        }
        // Lines drawers will be used by default
    }

    void applyCurrentColors(AppDesign.Theme curTheme, boolean animate) {
        int duration = animate ? MainActivity.APP_MODE_ANIM_DURATION : 0;

        AppDesign.Theme prevTheme = curTheme.invertedTheme();

        AppDesign.applyColorWithAnimation(AppDesign.bgActivity(prevTheme),
                AppDesign.bgActivity(curTheme), duration, updatedColor -> {
                    ChartPointsDrawer pointsDrawer = mChartView.getPointsDrawer();
                    if (pointsDrawer instanceof ChartBarsDrawer) {
                        ((ChartBarsDrawer) pointsDrawer).setCoverColor(updatedColor);
                    } else if (pointsDrawer instanceof ChartLinesDrawer) {
                        ((ChartLinesDrawer) pointsDrawer).setSelectedPointCircleBackground(updatedColor);
                    }
                    mChartView.getXLabelsDrawer().setBackgroundColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.chartGridColor(prevTheme),
                AppDesign.chartGridColor(curTheme), duration, updatedColor -> {
                    mChartView.getYLabelsDrawer().setGridColor(updatedColor);
                    mChartView.getPointsDetailsDrawer().setBackgroundBorderColor(updatedColor);
                    ChartPointsDrawer pointsDrawer = mChartView.getPointsDrawer();
                    if (pointsDrawer instanceof ChartLinesDrawer) {
                        ((ChartLinesDrawer) pointsDrawer).setSelectedPointsDividerColor(updatedColor);
                    }
                    if (mYRightLabelsDrawer != null) {
                        mYRightLabelsDrawer.setGridColor(updatedColor);
                    }
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

    @Override
    public void onExpandChartClicked(ChartFullView<DateCoordinate, LongCoordinate> view, int pointsIndex) {
        long timeStamp = mChartData.getXPoints().getPoints().get(pointsIndex).getValue();
        mActivity.setProgressVisibility(View.VISIBLE);
        new Thread(() -> {
            ChartLinesData<DateCoordinate, LongCoordinate> expandedData = null;
            try {
                expandedData = ChartDataProvider.getExpandedChartData(mChartView.getContext(), mAssetsDataFolder, timeStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final ChartLinesData<DateCoordinate, LongCoordinate> finalData = expandedData;
            mChartView.post(() -> {
                mActivity.setProgressVisibility(View.GONE);
                // Hide progress
                if (finalData != null) {
                    // TODO TBD
                }
            });
        }).start();

    }
}
