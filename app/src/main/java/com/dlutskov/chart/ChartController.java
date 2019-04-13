package com.dlutskov.chart;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dlutskov.chart.data.ChartDataProvider;
import com.dlutskov.chart.view.ChartCheckBoxesContainer;
import com.dlutskov.chart.view.ChartHeaderView;
import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartFullView;
import com.dlutskov.chart_lib.ChartPreviewView;
import com.dlutskov.chart_lib.ChartView;
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
import com.dlutskov.chart_lib.utils.ChartUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.dlutskov.chart.MainActivity.PADDING_GENERAL;

public class ChartController implements
        ChartPreviewView.Listener,
        ChartCheckBoxesContainer.Listener,
        ChartFullView.Listener<DateCoordinate, LongCoordinate> {

    private static final float CHART_PREVIEW_MIN_SELECTED_WIDTH = 0.15f;

    private final MainActivity mActivity;
    private final ChartData mChartData;

    private ChartLinesData<DateCoordinate, LongCoordinate> mCurrentChartLinesData;

    private View mTopSpaceView;
    private ChartHeaderView mHeaderView;
    private ChartFullView<DateCoordinate, LongCoordinate> mChartView;
    private ChartPreviewView<DateCoordinate, LongCoordinate> mChartPreview;
    private ChartCheckBoxesContainer mCheckBoxesContainer;

    private ChartBounds<DateCoordinate, LongCoordinate> mCollapsedChartBounds;

    private boolean isExpanded;

    // TODO Hotfix. For aplying app theme
    private ChartYAxisLabelsDrawer mYRightLabelsDrawer;

    ChartController(MainActivity activity, ChartData chartData) {
        mActivity = activity;
        mChartData = chartData;
        mCurrentChartLinesData = chartData.linesData;
    }

    void attachChart(ViewGroup container) {
        mTopSpaceView = new View(mActivity);
        mTopSpaceView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, ChartUtils.getPixelForDp(mActivity, 16)));
        container.addView(mTopSpaceView);

        // Chart header
        mHeaderView = createChartHeaderView(mActivity);
        mHeaderView.setTitleText(mChartData.name);
        mHeaderView.setTitleClickListener(v -> onHeaderTitleClicked());
        container.addView(mHeaderView);

        // Chart Full View
        mChartView = createChartFullView(mActivity);
        container.addView(mChartView);
        // Chart Preview
        mChartPreview = createChartPreviewView(mActivity);
        container.addView(mChartPreview);
        // Chart CheckBoxes
        mCheckBoxesContainer = createCheckboxesContainer(mActivity);
        container.addView(mCheckBoxesContainer);

        mChartView.setListener(this);
        mChartPreview.setListener(this);
        mCheckBoxesContainer.setListener(this);
    }

    void showChart() {
        initChartDrawers(mCurrentChartLinesData);
        int pointsSize = mCurrentChartLinesData.getXPoints().getPoints().size();
        int leftBound = (int) (pointsSize * (1 - CHART_PREVIEW_MIN_SELECTED_WIDTH));
        int rightBound = pointsSize - 1;
        // Update full chart
        mChartView.updateChartData(mCurrentChartLinesData, leftBound, rightBound, false);
        // Update preview chart with default bounds
        mChartPreview.updateChartData(mCurrentChartLinesData, false);
        mChartPreview.updateSelectedAreaBounds(leftBound, rightBound, rightBound - leftBound);
        // Create new checkboxes for new data
        mCheckBoxesContainer.createCheckBoxes(mCurrentChartLinesData);
    }

    private void initChartDrawers(ChartLinesData<DateCoordinate, LongCoordinate> chartData) {
        String chartType = chartData.getYPoints().get(0).getType();
        if (chartType.equals(ChartLinesData.CHART_TYPE_BAR) || chartType.equals(ChartLinesData.CHART_TYPE_AREA)) {
            mChartView.setMinYValue(LongCoordinate.valueOf(0));
            mChartPreview.setMinYValue(LongCoordinate.valueOf(0));
        } else if (chartData.isYScaled()) {
            // Bind yLabelsDrawer to first graph
            ChartPointsData<LongCoordinate> firstGraph = mCurrentChartLinesData.getYPoints().get(0);
            mChartView.getYLabelsDrawer().setScaledPointsId(firstGraph.getId(), firstGraph.getColor());

            // Create yLabelsDrawer for the second graph
            mYRightLabelsDrawer = new ChartYAxisLabelsDrawer(mChartView, ChartAxisLabelsDrawer.SIZE_MATCH_PARENT);
            mYRightLabelsDrawer.setSide(ChartYAxisLabelsDrawer.SIDE_RIGHT);
            ChartPointsData<LongCoordinate> secondGraph = mCurrentChartLinesData.getYPoints().get(1);
            mYRightLabelsDrawer.setScaledPointsId(secondGraph.getId(), secondGraph.getColor());
            mChartView.addDrawer(mYRightLabelsDrawer);
        }
        mChartView.setPointsDrawer(createPointsDrawer(chartData, mChartView));
        mChartPreview.setPointsDrawer(createPointsDrawer(chartData, mChartPreview));
    }

    private static ChartPointsDrawer<DateCoordinate, LongCoordinate, ?> createPointsDrawer(ChartLinesData<DateCoordinate, LongCoordinate> chartData,
                                                                                           ChartView<DateCoordinate, LongCoordinate> chartView) {
        String chartType = chartData.getYPoints().get(0).getType();
        if (chartType.equals(ChartLinesData.CHART_TYPE_BAR) || chartType.equals(ChartLinesData.CHART_TYPE_AREA)) {
            if (chartData.isPercentage()) {
                return new ChartPercentagesAreasDrawer<>(chartView);
            } else if (chartData.isStacked()) {
                return new ChartStackedBarsDrawer<>(chartView);
            } else {
                return new ChartBarsDrawer<>(chartView);
            }
        } else if (chartData.isYScaled()) {
            return new ChartScaledLinesDrawer<>(chartView);
        }
        return new ChartLinesDrawer<>(chartView);
    }

    private void updateHeaderBoundsText(int minXIndex, int maxXIndex) {
        String fromText = mCurrentChartLinesData.getXPoints().getPoints().get(minXIndex).getHeaderName();
        String toText = mCurrentChartLinesData.getXPoints().getPoints().get(maxXIndex).getHeaderName();
        String areaText = fromText.equals(toText) ? fromText : fromText + " - " + toText;
        mHeaderView.setAreaTitleText(areaText);
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

        // Header text
        if (isExpanded) {
            AppDesign.applyColorWithAnimation(AppDesign.getZoomOutText(prevTheme),
                    AppDesign.getZoomOutText(curTheme), duration, updatedColor -> {
                        mHeaderView.setTitleColor(updatedColor);
                    });
        } else {
            AppDesign.applyColorWithAnimation(AppDesign.getChartHeaderText(prevTheme),
                    AppDesign.getChartHeaderText(curTheme), duration, updatedColor -> {
                        mHeaderView.setTitleColor(updatedColor);
                    });
        }
        AppDesign.applyColorWithAnimation(AppDesign.getChartHeaderText(prevTheme),
                AppDesign.getChartHeaderText(curTheme), duration, updatedColor -> {
                    mHeaderView.setAreaTitleColor(updatedColor);
                });
    }

    @Override
    public void onChartPreviewAreaChanged(int minXIndex, int maxXIndex) {
        mChartView.updateHorizontalBounds(minXIndex, maxXIndex);
        updateHeaderBoundsText(minXIndex, maxXIndex);
    }

    @Override
    public void onChartLineCheckBoxStateChanged(String id, boolean checked) {
        mChartView.updatePointsVisibility(id, checked);
        mChartPreview.updatePointsVisibility(id, checked);
    }

    @Override
    public void onExpandChartClicked(ChartFullView<DateCoordinate, LongCoordinate> view, int pointsIndex) {
        long timeStamp = mCurrentChartLinesData.getXPoints().getPoints().get(pointsIndex).getValue();
        mActivity.setProgressVisibility(View.VISIBLE);
        new Thread(() -> {
            ChartLinesData<DateCoordinate, LongCoordinate> expandedData = null;
            try {
                expandedData = ChartDataProvider.getExpandedChartData(mChartView.getContext(), mChartData.assetsFolderName, timeStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final ChartLinesData<DateCoordinate, LongCoordinate> finalData = expandedData;
            mChartView.post(() -> {
                mActivity.setProgressVisibility(View.GONE);
                // Hide progress
                if (finalData != null) {
                    isExpanded = true;

                    mCollapsedChartBounds = new ChartBounds<>(mChartView.getBounds());

                    // Apply local min values for line charts - 0 for bar charts
                    String chartType = finalData.getYPoints().get(0).getType();
                    if (chartType.equals(ChartLinesData.CHART_TYPE_BAR) || chartType.equals(ChartLinesData.CHART_TYPE_AREA)) {
                        mChartView.setMinYValue(LongCoordinate.valueOf(0));
                        mChartPreview.setMinYValue(LongCoordinate.valueOf(0));
                    } else {
                        mChartView.setMinYValue(null);
                        mChartPreview.setMinYValue(null);
                    }

                    // Hardcoded positions as we definitely know that there are 3 days before and after selected day
                    int newMinXIndex = 72;
                    int newMaxXIndex = 96;
                    if (mChartData.id.equals(ChartData.CHART_ID_SINGLE_BAR)) {
                        // Such chart contains all data within 1 dat
                        newMinXIndex = 0;
                        newMaxXIndex = finalData.getXPoints().getPoints().size() - 1;
                    }

                    mCurrentChartLinesData = finalData;

                    updateHeaderBoundsText(newMinXIndex, newMaxXIndex);
                    mHeaderView.setTitleText("Zoom Out"); // TODO Hardcoded
                    mHeaderView.setTitleColor(AppDesign.getZoomOutText(AppDesign.getTheme()));

                    mChartView.expand(createPointsDrawer(finalData, mChartView), finalData, pointsIndex, newMinXIndex, newMaxXIndex);
                    mChartPreview.updateChartDataWithAnimation(finalData, newMinXIndex, newMaxXIndex, true, createPointsDrawer(finalData, mChartPreview));
                }
            });
        }).start();

    }

    private void onHeaderTitleClicked() {
        if (!isExpanded || mChartView.isDataAnimatorRunning()) {
            return;
        }

        if (isExpanded) {
            // Collapse
            isExpanded = false;

            mCurrentChartLinesData = mChartData.linesData;
            // Apply local min values for line charts - 0 for bar charts
            String chartType = mCurrentChartLinesData.getYPoints().get(0).getType();
            if (chartType.equals(ChartLinesData.CHART_TYPE_BAR) || chartType.equals(ChartLinesData.CHART_TYPE_AREA)) {
                mChartView.setMinYValue(LongCoordinate.valueOf(0));
                mChartPreview.setMinYValue(LongCoordinate.valueOf(0));
            } else {
                mChartView.setMinYValue(null);
                mChartPreview.setMinYValue(null);
            }

            int newMinXIndex = mCollapsedChartBounds.getMinXIndex();
            int newMaxXIndex = mCollapsedChartBounds.getMaxXIndex();

            updateHeaderBoundsText(newMinXIndex, newMaxXIndex);
            mHeaderView.setTitleText(mChartData.name);
            mHeaderView.setTitleColor(AppDesign.getZoomOutText(AppDesign.getTheme()));

            mChartView.collapse(createPointsDrawer(mCurrentChartLinesData, mChartView), mCurrentChartLinesData, mCollapsedChartBounds);
            mChartPreview.updateChartDataWithAnimation(mCurrentChartLinesData, newMinXIndex, newMaxXIndex, true, createPointsDrawer(mCurrentChartLinesData, mChartPreview));
        }
    }

    private static ChartHeaderView createChartHeaderView(Context ctx) {
        ChartHeaderView headerView = new ChartHeaderView(ctx);
        int margin = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.setMargins(margin, margin, margin, margin / 2);
        headerView.setLayoutParams(params);
        return headerView;
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
        return view;
    }

    private static ChartCheckBoxesContainer createCheckboxesContainer(Context ctx) {
        ChartCheckBoxesContainer view = new ChartCheckBoxesContainer(ctx);
        int margin = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.topMargin = ChartUtils.getPixelForDp(ctx, 4);
        params.leftMargin = margin;
        params.rightMargin = margin;
        view.setLayoutParams(params);
        return view;
    }
}
