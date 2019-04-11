package com.dlutskov.chart_lib.drawers;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

/**
 * Base class for drawing on the {@link ChartView} canvas.
 * Handles all chart's data and properties changes before drawing it on the canvas
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public abstract class ChartDataDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> {

    protected final ChartView<X, Y> mChartView;

    // Data with points which are need to be displayed
    private ChartLinesData<X, Y> mData;

    // Boundary values (min, max) which needs to know where to draw essential point
    private ChartBounds<X, Y> mBounds;

    // Rect where the chart points will be drawn
    private Rect mDrawingRect = new Rect();

    /**
     * Reflects whether drawer state is invalidated and need to call rebuild()
     * and rebuild drawing data before displaying.
     * Becomes true when the data or some properties changed which require to rebuild drawer
     */
    private boolean mInvalidated = true;

    protected ChartDataDrawer(ChartView<X, Y> chartView) {
        this.mChartView = chartView;
    }

    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds) {
        mData = data;
        mBounds = new ChartBounds<>(bounds);
        mInvalidated = true;
        mChartView.invalidate();
    }

    public void updateBounds(ChartBounds<X, Y> oldBounds, ChartBounds<X, Y> newBounds) {
       updateBoundsInternal(newBounds);
    }

    protected void updateBoundsInternal(ChartBounds<X, Y> newBounds) {
        if (mBounds == null) {
            mBounds = new ChartBounds<>(newBounds);
        } else {
            mBounds.update(newBounds);
        }
        mInvalidated = true;
        mChartView.invalidate();
    }

    ChartBounds<X, Y> getBounds() {
        return mBounds;
    }

    ChartLinesData<X, Y> getData() {
        return mData;
    }

    public void updatePointsVisibility(String pointsId, boolean visibility) {}

    /**
     * Called from the {@link ChartView} onDraw callback for ech drawer added to the view
     * Will be called for all drawers before {@link ChartLinesDrawer}.
     * @param canvas
     * @param drawingRect - rect where drawer's data need to be drawn
     */
    public final void draw(Canvas canvas, Rect drawingRect) {
        if (mData == null) {
            // There are no data to draw yet
            return;
        }
        if (!mDrawingRect.equals(drawingRect)) {
            mDrawingRect.set(drawingRect);
            mInvalidated = true;
        }
        if (mInvalidated) {
            rebuild(mData, mBounds, mDrawingRect);
            mInvalidated = false;
        }
        onDraw(canvas, drawingRect);
    }

    /**
     * Called for all drawers after {@link ChartLinesDrawer} draw called. Used for drawing above chart lines
     * @param canvas
     * @param drawingRect
     */
    public void onAfterDraw(Canvas canvas, Rect drawingRect) {}

    /**
     * Invalidates current drawer state to rebuild drawing data on next draw callback
     */
    public void invalidate() {
        mInvalidated = true;
    }

    /**
     * Called when need to rebuild drawer when some charts's data or properties were changed
     */
    protected abstract void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect);

    /**
     * Used to drawing below chart lines as it's called from {@link #draw(Canvas, Rect)}
     * Use {@link #onAfterDraw(Canvas, Rect)} to draw over chart lines
     * @param canvas
     * @param drawingRect
     */
    protected abstract void onDraw(Canvas canvas, Rect drawingRect);

}