package com.dlutskov.chart_lib.drawers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;

import java.util.List;
import java.util.Set;

public class ChartPercentagesAreasDrawer<X extends ChartCoordinate, Y extends ChartCoordinate>
        extends ChartPointsDrawer<X, Y, ChartPercentagesAreasDrawer.DrawingData<Y>> {

    /**
     * Value from 0 to 1, which will display clip ratio dur to pie chart transition
     * 0 - default state - 1 - clipped circle, same size as pie chart
     */
    private float mClipProgress;
    private Path mClipPath = new Path();

    private int mRotationAngle = 0;

    private final Paint mSelectedPointsDividerPaint;

    private Y mZero;
    private Y mYMaxValue;
    private Y mYBuf;
    private Y mMinYValue;

    public ChartPercentagesAreasDrawer(ChartView<X, Y> chartView) {
        super(chartView);
        chartView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        setAnimDuration(300);

        mSelectedPointsDividerPaint = new Paint();
        mSelectedPointsDividerPaint.setAntiAlias(true);
        mSelectedPointsDividerPaint.setStyle(Paint.Style.STROKE);
        mSelectedPointsDividerPaint.setStrokeWidth(ChartUtils.getPixelForDp(chartView.getContext(), 1));
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Set<String> hiddenChartPoints) {
        super.updateData(data, bounds, hiddenChartPoints);
        this.drawingDataList.clear();
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = new DrawingData<>(pointsData);
            boolean isVisible = !hiddenChartPoints.contains(pointsData.getId());
            drawingData.setVisible(isVisible);
            drawingData.setAlpha(isVisible ? 255 : 0);
            this.drawingDataList.add(drawingData);
        }
    }

    @Override
    public void updateBounds(ChartBounds<X, Y> currentBounds, ChartBounds<X, Y> targetBounds) {
        // Do not call super - no need to start bounds update animator
        updateBoundsInternal(targetBounds);
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        if (mZero == null) {
            mZero = (Y) bounds.getMinY().zero();
            mYMaxValue = (Y) mZero.clone();
            mYBuf = (Y) mYMaxValue.clone();
        }

        ChartBounds<X, Y> localBounds = new ChartBounds<>(bounds);
        for (int i = bounds.getMinXIndex(); i <= bounds.getMaxXIndex(); i++) {

            // Calculate local bounds
            mYMaxValue.set(mZero);
            for (DrawingData<Y> drawingData : drawingDataList) {
                if (!drawingData.isVisible()) continue;
                Y value = drawingData.pointsData.getPoints().get(i);
                if (mMinYValue == null) {
                    mMinYValue = (Y) value.clone();
                } else if (value.compareTo(mMinYValue) < 0) {
                    mMinYValue.set(value);
                }
                value.getPart(drawingData.getAlpha() / 255f, mYBuf);
                mYMaxValue.add(mYBuf, mYMaxValue);
            }
            if (mMinYValue != null && mYMaxValue.compareTo(mMinYValue) < 0) {
                mYMaxValue.set(mMinYValue);
            }
            localBounds.setMaxY(mYMaxValue);

            float prevY = drawingRect.bottom;
            for (DrawingData<Y> drawingData : drawingDataList) {
                if (!drawingData.isVisible()) continue;

                List<Y> points = drawingData.pointsData.getPoints();

                // First point
                if (i == bounds.getMinXIndex()) {
                    drawingData.path.reset();
                    drawingData.path.moveTo(drawingRect.left, drawingRect.bottom);
                }

                float x = ChartUtils.calcXCoordinate(localBounds, drawingRect, i);
                float y = ChartUtils.calcYCoordinate(localBounds, drawingRect, points.get(i));
                float appearingRatio = drawingData.getAlpha() / 255f; // Reduce bar height with reducing bar visibility
                float yCoordinate = prevY - (drawingRect.bottom - y) * appearingRatio;
                drawingData.path.lineTo(x, yCoordinate);
                prevY = yCoordinate;

                // Last point - connect to the first point
                if (i == bounds.getMaxXIndex()) {
                    drawingData.path.lineTo(drawingRect.right, drawingRect.bottom);
                    drawingData.path.lineTo(drawingRect.left, drawingRect.bottom);
                }
            }
        }
    }

    @Override
    protected void onVisibilityAnimatorUpdate(DrawingData<Y> pointsData, int alpha) {
        super.onVisibilityAnimatorUpdate(pointsData, alpha);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {
        float centerX = drawingRect.width() / 2;
        float centerY = drawingRect.height() / 2 + mChartView.getPaddingTop();
        float circleSize = drawingRect.height() / 2;

        // Clip all chart to circle
        if (mClipProgress > 0) {
            float clipCircleSize = circleSize + centerX * (1 - mClipProgress);
            mClipPath.reset();
            mClipPath.addCircle(centerX, centerY, clipCircleSize, Path.Direction.CW);
            canvas.clipPath(mClipPath);
        }

        // Rotate canvas if need
        if (mRotationAngle != 0) {
            canvas.save();
            canvas.rotate(mRotationAngle, centerX, centerY);
        }

        // Draw paths
        for (int i = drawingDataList.size() - 1; i >= 0; i--) {
            DrawingData<Y> data = drawingDataList.get(i);
            if (data.isVisible()) {
                data.paint.setAlpha(mPointsAlpha);
                canvas.drawPath(data.path, data.paint);
            }
        }

        if (mRotationAngle != 0) {
            canvas.restore();
        }

        // Draw selected point divider
        if (mSelectedPointIndex > 0) {
            float xPointsPosition = ChartUtils.calcXCoordinate(getBounds(), drawingRect, mSelectedPointIndex);
            mSelectedPointsDividerPaint.setAlpha(Math.min(MAX_GRID_ALPHA, mSelectedPointAlpha));
            canvas.drawLine(xPointsPosition, drawingRect.top, xPointsPosition, drawingRect.bottom, mSelectedPointsDividerPaint);
        }
    }

    public void setClipValue(float clipValue) {
        mClipProgress = clipValue;
    }

    public void setRotationAngle(int angle) {
        mRotationAngle = angle;
    }

    public void setSelectedPointsDividerColor(int color) {
        mSelectedPointsDividerPaint.setColor(color);
    }

    static class DrawingData<C extends ChartCoordinate> extends ChartPointsDrawer.DrawingData<C> {

        Path path;

        DrawingData(ChartPointsData<C> pointsData) {
            super(pointsData);

            paint.setStyle(Paint.Style.FILL);

            path = new Path();
        }

    }

}

