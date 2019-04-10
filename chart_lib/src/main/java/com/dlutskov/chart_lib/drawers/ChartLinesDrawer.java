package com.dlutskov.chart_lib.drawers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

import java.util.List;

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

    public ChartLinesDrawer(ChartView<X, Y> chartView) {
        super(chartView);
        mLineStrokeWidth = ChartUtils.getPixelForDp(chartView.getContext(), DEFAULT_LINE_STROKE_WIDTH);
    }

    public void setLineStrokeWidth(int lineStrokeWidth) {
        mLineStrokeWidth = lineStrokeWidth;
    }

    public int getLineStrokeWidth() {
        return mLineStrokeWidth;
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (drawingData == null) {
                drawingData = new DrawingData<>(pointsData, mLineStrokeWidth);
                this.drawingDataList.add(drawingData);
            }
            if (!drawingData.isVisible()) continue;

            buildLines(drawingData.mLines, pointsData.getPoints(), bounds, drawingRect);
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {
        int linesCount = (getBounds().getMaxXIndex() - getBounds().getMinXIndex()) * 4;
        for (DrawingData<Y> drawingData : drawingDataList) {
            if (drawingData.isVisible()) {
                canvas.drawLines(drawingData.mLines, 0, linesCount, drawingData.getPaint());
            }
        }
    }

    private void buildLines(float lines[], List<Y> yPoints, ChartBounds<X, Y> bounds, Rect drawingRect) {
        int lineIndex = 0;
        for (int i = bounds.getMinXIndex(); i < bounds.getMaxXIndex(); i++) { // TODO
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
