package com.dlutskov.chart_lib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;

/**
 * Extends {@link ChartView} and allow to select some area on the chart
 * between specified {@link #mSelectedMinXIndex} and {@link #mSelectedMaxXIndex} values
 * by extending, constricting and dragging actions
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartPreviewView<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartView<X, Y> {

    /**
     * Used to notify about chart selected area changed
     */
    public interface Listener {
        void onChartPreviewAreaChanged(int minXIndex, int maxXIndex);
    }

    /**
     * Reflect type of current touch handling
     */
    private enum SlideType {
        // Left border was touched on ACTION_DOWN and only this border will be dragged on next touches
        LEFT_BORDER,
        // Left border was touched on ACTION_DOWN and only this border will be dragged on next touches
        RIGHT_BORDER,
        // Whole selected chart area will be dragged on next touches
        AREA,
        // Next touches won't be handled
        NONE
    }

    /**
     * Index of minX collection index of selected chart area
     */
    private int mSelectedMinXIndex;

    /**
     * Index of minX collection index of selected chart area
     */
    private int mSelectedMaxXIndex;

    /**
     * Total number of x points
     */
    private int mXPointsCount;

    /**
     * Minimum number of x points inside selected area. Selected area won't be constricting to contains less points
     */
    private int mMinXPointsCount;

    // Left Right bounds reflected as canvas coordinate values
    private float mAreaLeftBound = -1;
    private float mAreaRightBound = -1;

    // Selected area width reflected as canvas coordinate
    private float mMinAreaWidth = -1;

    // Selected area border paints stroke width
    private int mHorizontalBorderStrokeWidth;
    private int mVerticalBorderStrokeWidth;

    // Drawing paints
    private Paint mSelectedAreaBackgroundPaint;
    private Paint mUnselectedAreaBackgroundPaint;
    private Paint mAreaBordersPaint;

    /**
     * Default touch slop to detect if we capture something to handles dragging on touch events
     */
    private int mTouchSlop;

    /**
     * X value of last ACTION_DOWN touch event
     */
    private float mDownTouchX;

    /**
     * X value of any last touch event
     */
    private float mLastTouchX;

    private SlideType mSlideType = SlideType.NONE;

    private boolean isTouchIntercepted;

    private Listener mListener;

    public ChartPreviewView(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        super.init();

        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledTouchSlop();

        // Default values
        mHorizontalBorderStrokeWidth = ChartUtils.getPixelForDp(getContext(), 2);
        mVerticalBorderStrokeWidth = ChartUtils.getPixelForDp(getContext(), 10);

        mSelectedAreaBackgroundPaint = new Paint();
        mSelectedAreaBackgroundPaint.setStyle(Paint.Style.FILL);
        mSelectedAreaBackgroundPaint.setColor(Color.WHITE);

        mUnselectedAreaBackgroundPaint = new Paint();
        mUnselectedAreaBackgroundPaint.setStyle(Paint.Style.FILL);
        mUnselectedAreaBackgroundPaint.setColor(Color.GRAY);

        mAreaBordersPaint = new Paint();
        mSelectedAreaBackgroundPaint.setStyle(Paint.Style.FILL);
        mAreaBordersPaint.setColor(Color.DKGRAY);
    }

    @Override
    public void updateChartData(ChartLinesData<X, Y> chartData, int minXIndex, int maxXindex, boolean keepHiddenChartLines) {
        super.updateChartData(chartData, minXIndex, maxXindex, keepHiddenChartLines);

        mXPointsCount = chartData.getXPoints().getPoints().size();

        mSelectedMinXIndex = minXIndex;
        mSelectedMaxXIndex = maxXindex;

        mMinXPointsCount = Math.max(mMinXPointsCount, mSelectedMaxXIndex - mSelectedMinXIndex);

        if (getWidth() != 0) {
            // Calculate coordinates if view was already drawn - if no - it'll be calculated in updatePointsDrawingRect
            calculateCurrentCoordinates();
        }
    }

    public void updateSelectedAreaBounds(int minXIndex, int maxXIndex, int minXPointsCount) {
        mSelectedMinXIndex = minXIndex;
        mSelectedMaxXIndex = maxXIndex;
        mMinXPointsCount = minXPointsCount;
        calculateCurrentCoordinates();
        notifyBordersChanged();
        invalidate();
    }

    /**
     * Calculates area bounds canvas coordinates according to area indexes
     */
    private void calculateCurrentCoordinates() {
        mAreaLeftBound = indexToCoordinate(mSelectedMinXIndex);
        mAreaRightBound = indexToCoordinate(mSelectedMaxXIndex);
        mMinAreaWidth = indexToCoordinate(mMinXPointsCount);
    }

    private float indexToCoordinate(float index) {
        return (index / (mXPointsCount - 1)) * mDrawingRect.width();
    }

    private int coordinateToIndex(float coordinate) {
        return Math.round((coordinate / mDrawingRect.width()) * (mXPointsCount - 1));
    }

    @Override
    protected void updatePointsDrawingRect(Rect drawingRect) {
        super.updatePointsDrawingRect(drawingRect);
        calculateCurrentCoordinates();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // Draw selected area
        canvas.drawRect(mAreaLeftBound, 0, mAreaRightBound, getHeight(), mSelectedAreaBackgroundPaint);

        // Draw chart lines
        super.onDraw(canvas);

        // Draw selected area borders
        // Vertical borders
        canvas.drawRect(mAreaLeftBound, 0, mAreaLeftBound + mVerticalBorderStrokeWidth, getHeight(), mAreaBordersPaint);
        canvas.drawRect(mAreaRightBound - mVerticalBorderStrokeWidth, 0, mAreaRightBound, getHeight(), mAreaBordersPaint);
        // Horizontal borders
        canvas.drawRect(mAreaLeftBound + mVerticalBorderStrokeWidth, 0,
                mAreaRightBound - mVerticalBorderStrokeWidth, mHorizontalBorderStrokeWidth, mAreaBordersPaint);
        canvas.drawRect(mAreaLeftBound + mVerticalBorderStrokeWidth, getHeight() - mHorizontalBorderStrokeWidth,
                mAreaRightBound - mVerticalBorderStrokeWidth, getHeight(), mAreaBordersPaint);

        // Draw overlay upon not selected area to make graph less visible
        canvas.drawRect(0, 0, mAreaLeftBound, getHeight(), mUnselectedAreaBackgroundPaint);
        canvas.drawRect(mAreaRightBound, 0, getWidth(), getHeight(), mUnselectedAreaBackgroundPaint);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownTouchX = x;
                if (x > mAreaLeftBound - mHorizontalBorderStrokeWidth - mTouchSlop && x < mAreaLeftBound + mHorizontalBorderStrokeWidth + mTouchSlop) {
                    // Touch on left border
                    mSlideType = SlideType.LEFT_BORDER;
                } else if (x > mAreaRightBound - mHorizontalBorderStrokeWidth - mTouchSlop && x < mAreaRightBound + mHorizontalBorderStrokeWidth + mTouchSlop) {
                    // Touch on right border
                    mSlideType = SlideType.RIGHT_BORDER;
                } else if (x > mAreaLeftBound && x < mAreaRightBound) {
                    // Touch on selected area
                    mSlideType = SlideType.AREA;
                } else {
                    mSlideType = SlideType.NONE;
                    return false;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isTouchIntercepted = false;
                mSlideType = SlideType.NONE;
                break;
        }
        mLastTouchX = x;
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (mSlideType != SlideType.NONE) {
                    float x = event.getX();
                    if (!isTouchIntercepted && Math.abs(x - mDownTouchX) > mTouchSlop / 2) {
                        isTouchIntercepted = true;
                        disableParentTouch();
                    }
                    if (isTouchIntercepted) {
                        move(x - mLastTouchX);
                        mLastTouchX = x;
                        disableParentTouch();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isTouchIntercepted = false;
                mSlideType = SlideType.NONE;
                break;
        }

        return true;
    }

    private void disableParentTouch() {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private void move(float dx) {
        float areaWidth = mAreaRightBound - mAreaLeftBound;
        float leftBound = mAreaLeftBound;
        float rightBound = mAreaRightBound;
        switch (mSlideType) {

            case LEFT_BORDER:
                leftBound = mAreaLeftBound + dx;
                if (leftBound < 0) {
                    leftBound = 0;
                }
                break;
            case RIGHT_BORDER:
                rightBound = mAreaRightBound + dx;
                if (rightBound > getWidth()) {
                    rightBound = getWidth();
                }
                break;
            case AREA:
                leftBound = mAreaLeftBound + dx;
                rightBound = mAreaRightBound + dx;
                if (leftBound < 0) {
                    leftBound = 0;
                    rightBound = areaWidth;
                } else if (rightBound > getWidth()) {
                    rightBound = getWidth();
                    leftBound = rightBound - areaWidth;
                }
                break;
        }
        if (mSlideType != SlideType.AREA && rightBound - leftBound < mMinAreaWidth) {
            return;
        }
        if (mAreaLeftBound != leftBound || mAreaRightBound != rightBound) {
            onSelectedAreaChanged(leftBound, rightBound);
        }
    }

    private void onSelectedAreaChanged(float leftBound, float rightBound) {
        if (mAreaLeftBound != leftBound || mAreaRightBound != rightBound) {
            mAreaLeftBound = leftBound;
            mAreaRightBound = rightBound;
            int leftIndex = coordinateToIndex(leftBound);
            int rightIndex = coordinateToIndex(rightBound);
            if (leftIndex != mSelectedMinXIndex || rightIndex != mSelectedMaxXIndex) {
                int currentSize = mSelectedMaxXIndex - mSelectedMinXIndex;
                mSelectedMinXIndex = leftIndex;
                mSelectedMaxXIndex = mSlideType == SlideType.AREA ? leftIndex + currentSize : rightIndex;
                notifyBordersChanged();
                invalidate();
            }
        }
    }

    private void notifyBordersChanged() {
        if (mListener != null) {
            mListener.onChartPreviewAreaChanged(mSelectedMinXIndex, mSelectedMaxXIndex);
        }
    }

    public void setAreaBordersColor(int bordersColor) {
        mAreaBordersPaint.setColor(bordersColor);
        invalidate();
    }

    public void setUnselectedBackgroundColor(int color) {
        mUnselectedAreaBackgroundPaint.setColor(color);
        invalidate();
    }

    public void setSelectedBackgroundColor(int color) {
        mSelectedAreaBackgroundPaint.setColor(color);
        invalidate();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

}