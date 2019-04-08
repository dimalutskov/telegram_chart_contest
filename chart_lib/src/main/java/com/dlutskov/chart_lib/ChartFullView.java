package com.dlutskov.chart_lib;

import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.drawers.ChartAxisLabelsDrawer;
import com.dlutskov.chart_lib.drawers.ChartPointsDetailsDrawer;
import com.dlutskov.chart_lib.drawers.ChartXAxisLabelsDrawer;
import com.dlutskov.chart_lib.drawers.ChartYAxisLabelsDrawer;
import com.dlutskov.chart_lib.utils.ChartUtils;

/**
 * Extends {@link ChartView} by adding own {@link ChartXAxisLabelsDrawer} and {@link ChartYAxisLabelsDrawer}
 * for drawing labels on x and axis and {@link ChartPointsDetailsDrawer} for drawing window with details of selected
 * points on chart touch. All the drawers properties can be changed by calling appropriate get methods
 * and replaced by calling set method for required drawer
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of y axis chart coordinates
 */
public class ChartFullView<X extends ChartCoordinate, Y extends ChartCoordinate>  extends ChartView<X, Y> {

    private ChartXAxisLabelsDrawer<X, Y> mXAxisLabelsDrawer;
    private ChartYAxisLabelsDrawer<X, Y> mYAxisLabelsDrawer;
    private ChartPointsDetailsDrawer<X, Y> mPointsDetailsDrawer;

    private GestureDetector mGestureDetector = new GestureDetector(new GestureDetectorListener());

    private boolean mShowPointsDetailsOnTouch = true;
    private boolean mDetectClickOnPointsDetails;

    public ChartFullView(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        super.init();
        mXAxisLabelsDrawer = new ChartXAxisLabelsDrawer<>(this, ChartUtils.getPixelForDp(getContext(), 32));
        mYAxisLabelsDrawer = new ChartYAxisLabelsDrawer<>(this, ChartAxisLabelsDrawer.SIZE_MATCH_PARENT); // TODO
        mYAxisLabelsDrawer.setDrawOverPoints(true);
        mPointsDetailsDrawer = new ChartPointsDetailsDrawer<>(this);

        addDrawer(mYAxisLabelsDrawer);
        addDrawer(mXAxisLabelsDrawer);
        addDrawer(mPointsDetailsDrawer);
    }

    @Override
    protected void updatePointsDrawingRect(Rect rect) {
        super.updatePointsDrawingRect(rect);

        int xLabelsHeight = mXAxisLabelsDrawer.getSize();
        int yLabelsWidth = mYAxisLabelsDrawer.getSize();
        if (!mXAxisLabelsDrawer.isDrawOverPoints() && xLabelsHeight > 0) {
            rect.bottom -= xLabelsHeight;
        }
        if (!mYAxisLabelsDrawer.isDrawOverPoints() && yLabelsWidth > 0) {
            rect.left += yLabelsWidth;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);

        float x = ev.getX();
        if (x < mDrawingRect.left || x > mDrawingRect.right) {
            return true;
        }

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (mPointsDetailsDrawer.isShown() && mPointsDetailsDrawer.isTouchInside(ev.getX(), ev.getY())) {
                // Skip clicks on the details window - it'll be handled by gesture listener
                mDetectClickOnPointsDetails = true;
                mShowPointsDetailsOnTouch = false;
                return true;
            } else {
                mDetectClickOnPointsDetails = false;
            }
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (mShowPointsDetailsOnTouch) {
                    int xIndex = mBounds.getMinXIndex() + Math.round((x / mDrawingRect.width()) * mBounds.getXPointsCount());
                    mPointsDetailsDrawer.showPointDetails(xIndex);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mPointsDetailsDrawer.isShown()) {
                    mPointsDetailsDrawer.hidePointDetails(ChartPointsDetailsDrawer.DISAPPEARING_DELAY);
                }
                mShowPointsDetailsOnTouch = true;
                break;
        }
        return true;
    }

    public ChartXAxisLabelsDrawer<X, Y> getXLabelsDrawer() {
        return mXAxisLabelsDrawer;
    }

    public void setXLabelsDrawer(ChartXAxisLabelsDrawer<X, Y> xLabelsDrawer) {
        removeDrawer(mXAxisLabelsDrawer);
        mXAxisLabelsDrawer = xLabelsDrawer;
        addDrawer(mXAxisLabelsDrawer);
        invalidate();
    }

    public ChartYAxisLabelsDrawer<X, Y> getYLabelsDrawer() {
        return mYAxisLabelsDrawer;
    }

    public void setYLabelsDrawer(ChartYAxisLabelsDrawer<X, Y> yLabelsDrawer) {
        removeDrawer(mYAxisLabelsDrawer);
        mYAxisLabelsDrawer = yLabelsDrawer;
        addDrawer(mYAxisLabelsDrawer);
        invalidate();
    }

    public ChartPointsDetailsDrawer<X, Y> getPointsDetailsDrawer() {
        return mPointsDetailsDrawer;
    }

    public void setPointsDetailsDrawer(ChartPointsDetailsDrawer<X, Y> pointsDetailsDrawer) {
        removeDrawer(mPointsDetailsDrawer);
        mPointsDetailsDrawer = pointsDetailsDrawer;
        addDrawer(mPointsDetailsDrawer);
        invalidate();
    }

    public void setLabelsTextSize(int textSize) {
        mXAxisLabelsDrawer.setTextSize(textSize);
        mYAxisLabelsDrawer.setTextSize(textSize);
        invalidate();
    }

    public void setLabelsTextColor(int textColor) {
        mXAxisLabelsDrawer.setTextColor(textColor);
        mYAxisLabelsDrawer.setTextColor(textColor);
        invalidate();
    }

    class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mDetectClickOnPointsDetails && mPointsDetailsDrawer.isShown() && mPointsDetailsDrawer.isTouchInside(e.getX(), e.getY())) {
                mPointsDetailsDrawer.hidePointDetails(0);
            }
            return super.onSingleTapConfirmed(e);
        }
    }
}
