package com.dlutskov.chart_lib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Pair;
import android.widget.FrameLayout;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.drawers.ChartDataDrawer;
import com.dlutskov.chart_lib.drawers.ChartLinesDrawer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base view for drawing chart according to specified {@link ChartLinesData}.
 * Call {@link #updateChartData(ChartLinesData)} to draw chart with specified data
 * All drawing is performed on the {@link #onDraw(Canvas)} callback by delegating drawing
 * to all registered {@link ChartDataDrawer} instances.
 * To draw something more - register own ChartDataDrawer by calling {@link #addDrawer(ChartDataDrawer)}
 * Chart's lines drawing performed by the {@link ChartLinesDrawer}. Call {@link #getLinesDrawer()} to get drawer
 * and change it's properties if you need ot {@link #setLinesDrawer(ChartLinesDrawer)} to use your own drawer
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartView<X extends ChartCoordinate, Y extends ChartCoordinate> extends FrameLayout {

    // Data to display
    protected ChartLinesData<X, Y> mLinesData;

    /**
     * Contains ids of chart lines which is no need to be displayed
     * All lines data which ids are added here - will be not drawn and won't take part in bounds calculations
     */
    protected Set<String> mHiddenChartLines = new HashSet<>();

    /**
     * Contains actual chart bounds. According to all visible lines and lines bounds - chart's Y bounds
     * are calculated automatically when new data is added or x bounds are changed
     * If {@link #mMinYValue} and {@link #mMaxYValue} are specified (not null) - bounds can be less or more that values
     */
    protected ChartBounds<X, Y> mBounds;

    /**
     * Main drawer which draws chart lines
     */
    protected ChartLinesDrawer<X, Y> mLinesDrawer;

    /**
     * All added drawers which wil handle chart updates and be drawn on onDraw callback
     */
    private List<ChartDataDrawer<X, Y>> mDrawers = new ArrayList<>();

    /**
     * Rect where chart points and all rest drawers will be drawn
     */
    protected Rect mDrawingRect = new Rect();

    // TODO
    private Y mMinYValue;
    private Y mMaxYValue;

    public ChartView(Context context) {
        super(context);
        init();
    }

    protected void init() {
        mBounds = new ChartBounds<>(0, 0, null, null);
        mLinesDrawer = new ChartLinesDrawer<>(this);
        // Set small top padding by default to have some space above the highest point
        int topPadding = ChartUtils.getPixelForDp(getContext(), 6);
        setPadding(0, topPadding, 0, 0);
        // Used to force onDraw callback
        setWillNotDraw(false);
    }

    public void updateChartData(ChartLinesData<X, Y> chartData) {
        updateChartData(chartData,0, chartData.getXPoints().getPoints().size() - 1);
    }

    public void updateChartData(ChartLinesData<X, Y> chartData, int minXIndex, int maxXindex) {
        updateChartDataInternal(chartData, minXIndex, maxXindex);

    }

    private void updateChartDataInternal(ChartLinesData<X, Y> chartData, int minXIndex, int maxXindex) {
        mLinesData = chartData;
        mHiddenChartLines.clear();
        // Calculate initial bounds
        calculateCurrentBounds(minXIndex, maxXindex);
        mLinesDrawer.updateData(mLinesData, mBounds);
        for (ChartDataDrawer<X, Y> drawer : mDrawers) {
            drawer.updateData(chartData, mBounds);
        }
    }

    public void updateHorizontalBounds(int minXIndex, int maxXIndex) {
        ChartBounds<X, Y> currentBounds = new ChartBounds<>(mBounds);
        calculateCurrentBounds(minXIndex, maxXIndex);
        onBoundsUpdated(currentBounds, mBounds);
    }

    public void updatePointsVisibility(String pointsId, boolean visible) {
        if (visible) {
            mHiddenChartLines.remove(pointsId);
        } else {
            mHiddenChartLines.add(pointsId);
        }

        // Calculate and update new bounds
        if (mHiddenChartLines.size() != mLinesData.getYPoints().size()) {
            ChartBounds<X, Y> currentBounds = new ChartBounds<>(mBounds);
            calculateCurrentBounds(mBounds.getMinXIndex(), mBounds.getMaxXIndex());
            onBoundsUpdated(currentBounds, mBounds);
        }
        // Update points
        mLinesDrawer.updatePointsVisibility(pointsId, visible);
        for (ChartDataDrawer<X, Y> drawer : mDrawers) {
            drawer.updatePointsVisibility(pointsId, visible);
        }
    }

    protected void onBoundsUpdated(ChartBounds<X, Y> oldBounds, ChartBounds<X, Y> newBounds) {
        mLinesDrawer.updateBounds(oldBounds, newBounds);
        for (ChartDataDrawer<X, Y> drawer : mDrawers) {
            drawer.updateBounds(oldBounds, newBounds);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        updatePointsDrawingRect(mDrawingRect);
        super.draw(canvas);
    }

    protected void updatePointsDrawingRect(Rect drawingRect) {
        drawingRect.set(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Pre Drawing
        for (ChartDataDrawer<X, Y> drawer : mDrawers) {
            drawer.draw(canvas, mDrawingRect);
        }
        // Draw lines
        mLinesDrawer.draw(canvas, mDrawingRect);
        // Post Drawing
        for (ChartDataDrawer<X, Y> drawer : mDrawers) {
            drawer.onAfterDraw(canvas, mDrawingRect);
        }
    }

    private void calculateCurrentBounds(int minXIndex, int maxXIndex) {
        Y minY = mBounds.getMinY();
        Y maxY = mBounds.getMaxY();
        if (mLinesData.getYPoints().size() != mHiddenChartLines.size()) {
            // Calculate Y bounds only if at least 1 line is visible
            Pair<Y, Y> yMinMax = calculateCurrentYBounds(mLinesData.getYPoints(), minXIndex, maxXIndex, mHiddenChartLines);
            minY = yMinMax.first;
            maxY = yMinMax.second;
            if (mMinYValue != null && mMinYValue.compareTo(minY) < 0) {
                minY = mMinYValue;
            }
            if (mMaxYValue != null && mMaxYValue.compareTo(maxY) > 0) {
                maxY = mMaxYValue;
            }
        }
        mBounds.update(minXIndex, maxXIndex, minY, maxY);
    }

    public ChartLinesDrawer<X, Y> getLinesDrawer() {
        return mLinesDrawer;
    }

    public void setLinesDrawer(ChartLinesDrawer<X, Y> pointsDrawer) {
        mLinesDrawer = pointsDrawer;
        invalidate();
    }

    public void addDrawer(ChartDataDrawer<X, Y> drawer) {
        mDrawers.add(drawer);
    }

    public void removeDrawer(ChartDataDrawer<X, Y> drawer) {
        mDrawers.remove(drawer);
    }

    public void setMinYValue(Y minYValue) {
        mMinYValue = minYValue;
    }

    public void setMaxYValue(Y maxYValue) {
        mMaxYValue = maxYValue;
    }

    private static <C extends ChartCoordinate> Pair<C, C> calculateCurrentYBounds(List<ChartPointsData<C>> pointsData, int minIndex, int maxIndex, Set<String> excludedLines) {
        C minY = null; C maxY = null;
        for (ChartPointsData<C> chartColumnData : pointsData) {
            if (excludedLines.contains(chartColumnData.getId())) {
                // Ignore hidden chart lines
                continue;
            }
            Pair<Integer, Integer> chartColumnBounds = ChartPointsData.calculateMinMaxIndexes(chartColumnData, minIndex, maxIndex);
            C min = chartColumnData.getPoints().get(chartColumnBounds.first);
            C max = chartColumnData.getPoints().get(chartColumnBounds.second);

            if (minY == null || min.compareTo(minY) < 0) {
                minY = min;
            }
            if (maxY == null || max.compareTo(maxY) > 0) {
                maxY = max;
            }
        }
        return new Pair<>(minY, maxY);
    }

}
