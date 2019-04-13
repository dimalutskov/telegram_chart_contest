package com.dlutskov.chart_lib.drawers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

import java.util.List;
import java.util.Set;

/**
 * Draws chart's lines and handles update bounds and visibility animations
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartLinesDrawer<X extends ChartCoordinate, Y extends ChartCoordinate>
        extends ChartPointsDrawer<X, Y, ChartLinesDrawer.DrawingData<Y>> {

    // Default drawing line stroke width in DP
    private static final int DEFAULT_LINE_STROKE_WIDTH = 2;

    private int mLineStrokeWidth;

    // Paints for drawing selected points
    private final Paint mSelectedPointCircleBackgroundPaint;
    private final Paint mSelectedPointCircleStokePaint;
    private final Paint mSelectedPointsDividerPaint;

    // Radius of circle which will reflect selected points coordinates
    private int mPointCircleRadius;

    private boolean mDrawSelectedPointsDivider = true;

    public ChartLinesDrawer(ChartView<X, Y> chartView) {
        super(chartView);
        mLineStrokeWidth = ChartUtils.getPixelForDp(chartView.getContext(), DEFAULT_LINE_STROKE_WIDTH);
        mPointCircleRadius = ChartUtils.getPixelForDp(chartView.getContext(), 3);

        mSelectedPointCircleBackgroundPaint = new Paint();
        mSelectedPointCircleBackgroundPaint.setAntiAlias(true);
        mSelectedPointCircleBackgroundPaint.setStyle(Paint.Style.FILL);
        mSelectedPointCircleBackgroundPaint.setColor(Color.WHITE);

        mSelectedPointCircleStokePaint = new Paint();
        mSelectedPointCircleStokePaint.setAntiAlias(true);
        mSelectedPointCircleStokePaint.setStyle(Paint.Style.STROKE);
        mSelectedPointCircleStokePaint.setStrokeWidth(mLineStrokeWidth);

        mSelectedPointsDividerPaint = new Paint();
        mSelectedPointsDividerPaint.setAntiAlias(true);
        mSelectedPointsDividerPaint.setStyle(Paint.Style.STROKE);
        mSelectedPointsDividerPaint.setStrokeWidth(mLineStrokeWidth);
    }

    public void setLineStrokeWidth(int lineStrokeWidth) {
        mLineStrokeWidth = lineStrokeWidth;
    }

    public int getLineStrokeWidth() {
        return mLineStrokeWidth;
    }

    public void setSelectedPointCircleBackground(int color) {
        mSelectedPointCircleBackgroundPaint.setColor(color);
        mChartView.invalidate();
    }

    public void setSelectedPointsDividerColor(int color) {
        mSelectedPointsDividerPaint.setColor(color);
        mChartView.invalidate();
    }

    public void setDrawSelectedPointsDivider(boolean drawSelectedPointsDivider) {
        mDrawSelectedPointsDivider = drawSelectedPointsDivider;
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Set<String> hiddenChartPoints) {
        super.updateData(data, bounds, hiddenChartPoints);
        this.drawingDataList.clear();
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = new DrawingData<>(pointsData, mLineStrokeWidth);
            boolean isVisible = !hiddenChartPoints.contains(pointsData.getId());
            drawingData.setVisible(isVisible);
            drawingData.setAlpha(isVisible ? 255 : 0);
            this.drawingDataList.add(drawingData);
        }
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (!drawingData.isVisible()) continue;

            buildLines(drawingData.mLines, pointsData.getPoints(), bounds, drawingRect);
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {
        float xPointsPosition = ChartUtils.calcXCoordinate(getBounds(), drawingRect, mSelectedPointIndex);

        // Draw vertical line
        if (mDrawSelectedPointsDivider && mSelectedPointIndex > 0 && mSelectedPointAlpha > 0) {
            mSelectedPointsDividerPaint.setAlpha(mSelectedPointAlpha);
            canvas.drawLine(xPointsPosition, drawingRect.top, xPointsPosition, drawingRect.bottom, mSelectedPointsDividerPaint);
        }

        // Draw lines
        int linesCount = (getBounds().getMaxXIndex() - getBounds().getMinXIndex()) * 4;
        for (DrawingData<Y> drawingData : drawingDataList) {
            if (drawingData.isVisible()) {
                drawingData.getPaint().setAlpha(Math.min(mPointsAlpha, drawingData.getAlpha()));
                canvas.drawLines(drawingData.mLines, 0, linesCount, drawingData.getPaint());
            }
        }
        // Draw selected points
        if (mSelectedPointIndex > 0 && mSelectedPointAlpha > 0) {
           drawSelectedPoints(canvas, drawingRect, xPointsPosition);
        }
    }

    protected void drawSelectedPoints(Canvas canvas, Rect drawingRect, float xPointsPosition) {
        // Draw circles
        for (ChartPointsData<Y> pointsData : getData().getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (!drawingData.isVisible()) continue;

            ChartBounds<X, Y> pointBounds = getSelectedPointsBounds(pointsData.getId());
            // Calculate selected points y position
            Y pointY = pointsData.getPoints().get(mSelectedPointIndex);
            float y = ChartUtils.calcYCoordinate(pointBounds, drawingRect, pointY);

            int alpha = Math.min(drawingData.getAlpha(), mSelectedPointAlpha);

            // Draw selected point background
            mSelectedPointCircleBackgroundPaint.setAlpha(alpha);
            canvas.drawCircle(xPointsPosition, y, mPointCircleRadius, mSelectedPointCircleBackgroundPaint);

            // Draw selected point stroke
            mSelectedPointCircleStokePaint.setColor(pointsData.getColor());
            mSelectedPointCircleStokePaint.setAlpha(alpha);
            canvas.drawCircle(xPointsPosition, y, mPointCircleRadius, mSelectedPointCircleStokePaint);
        }
    }

    void buildLines(float lines[], List<Y> yPoints, ChartBounds<X, Y> bounds, Rect drawingRect) {
        int lineIndex = 0;
        for (int i = bounds.getMinXIndex(); i < bounds.getMaxXIndex(); i++) {
            float x = ChartUtils.calcXCoordinate(bounds, drawingRect, i);
            float y = ChartUtils.calcYCoordinate(bounds, drawingRect, yPoints.get(i));
            lines[lineIndex++] = x;
            lines[lineIndex++] = y;
            x = ChartUtils.calcXCoordinate(bounds, drawingRect, i + 1);
            y = ChartUtils.calcYCoordinate(bounds, drawingRect, yPoints.get(i + 1));
            lines[lineIndex++] = x;
            lines[lineIndex++] = y;
        }
    }

    ChartBounds<X, Y> getSelectedPointsBounds(String pointsId) {
        return getBounds();
    }

    static class DrawingData<C extends ChartCoordinate> extends ChartPointsDrawer.DrawingData<C> {

        protected float[] mLines;

        DrawingData(ChartPointsData<C> pointsData, int strokeWidth) {
            super(pointsData);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);

            mLines = new float[pointsData.getPoints().size() * 4];
        }

    }

}
