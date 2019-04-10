package com.dlutskov.chart_lib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.FrameLayout;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.drawers.ChartDataDrawer;
import com.dlutskov.chart_lib.drawers.ChartLinesDrawer;
import com.dlutskov.chart_lib.drawers.ChartPointsDrawer;
import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.utils.Pair;

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
 * Chart's lines drawing performed by the {@link ChartPointsDrawer}. Call {@link #setPointsDrawer(ChartPointsDrawer)}
 * to use your own drawer
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartView<X extends ChartCoordinate, Y extends ChartCoordinate> extends FrameLayout {

    /**
     * Data set which will be displayed by the chart drawers
     */
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
    protected ChartPointsDrawer<X, Y, ?> mPointsDrawer;

    /**
     * All added drawers which wil handle chart updates and be drawn on onDraw callback
     */
    private List<ChartDataDrawer<X, Y>> mDrawers = new ArrayList<>();

    /**
     * Rect where chart points and all rest drawers will be drawn
     */
    protected Rect mDrawingRect = new Rect();

    // By default each time when bounds or data changed - min/max Y values will be recalculated depends on
    // the visible data on the chart. Values below used to force use specified min max values not depend on the chart's data
    private Y mMinYValue;
    private Y mMaxYValue;

    // Used to calculate minY and maxY to not create new instance on each calculations
    private Pair<Y, Y> mYBoundsPair = new Pair<>(null, null);

    public ChartView(Context context) {
        super(context);
        init();
    }

    protected void init() {
        mBounds = new ChartBounds<>(0, 0, null, null);
        mPointsDrawer = new ChartLinesDrawer<>(this);
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
        mPointsDrawer.updateData(mLinesData, mBounds);
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
        mPointsDrawer.updatePointsVisibility(pointsId, visible);
        for (ChartDataDrawer<X, Y> drawer : mDrawers) {
            drawer.updatePointsVisibility(pointsId, visible);
        }
    }

    protected void onBoundsUpdated(ChartBounds<X, Y> oldBounds, ChartBounds<X, Y> newBounds) {
        mPointsDrawer.updateBounds(oldBounds, newBounds);
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
        mPointsDrawer.draw(canvas, mDrawingRect);
        // Post Drawing
        for (ChartDataDrawer<X, Y> drawer : mDrawers) {
            drawer.onAfterDraw(canvas, mDrawingRect);
        }
    }

    private void calculateCurrentBounds(int minXIndex, int maxXIndex) {
        mYBoundsPair = mLinesData.calculateYBounds(minXIndex, maxXIndex, mHiddenChartLines, mYBoundsPair);
        if (mMinYValue != null && mMinYValue.compareTo(mYBoundsPair.first) < 0) {
            mYBoundsPair.first = mMinYValue;
        }
        if (mMaxYValue != null && mMaxYValue.compareTo(mYBoundsPair.second) > 0) {
            mYBoundsPair.second = mMaxYValue;
        }
        mBounds.update(minXIndex, maxXIndex, mYBoundsPair.first, mYBoundsPair.second);
    }

    public void setPointsDrawer(ChartPointsDrawer<X, Y, ?> pointsDrawer) {
        mPointsDrawer = pointsDrawer;
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

}
