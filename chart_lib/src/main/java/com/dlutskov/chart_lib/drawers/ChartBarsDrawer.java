package com.dlutskov.chart_lib.drawers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;

import java.util.List;

public class ChartBarsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate>
        extends ChartPointsDrawer<X, Y, ChartBarsDrawer.DrawingData<Y>> {

    public ChartBarsDrawer(ChartView<X, Y> chartView) {
        super(chartView);
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        int columnWidth = drawingRect.width() / (bounds.getMaxXIndex() - bounds.getMinXIndex());
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (drawingData == null) {
                drawingData = new DrawingData<>(pointsData);
                this.drawingData.add(drawingData);
            }
            if (!drawingData.isVisible()) continue;

            drawingData.paint.setStrokeWidth(columnWidth);
            buildLines(drawingData.mLines, pointsData.getPoints(), bounds, columnWidth, drawingRect);
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {
        for (DrawingData<Y> drawingData : drawingData) {
            if (drawingData.isVisible()) {
                canvas.drawLines(drawingData.mLines, drawingData.getPaint());
            }
        }
    }

    private void buildLines(float lines[], List<Y> yPoints, ChartBounds<X, Y> bounds, int strokeWidth, Rect drawingRect) {
        int lineIndex = 0;
        for (int i = 0; i < yPoints.size(); i++) {
            float x = ChartUtils.calcXCoordinate(bounds, drawingRect, i);
            float y = ChartUtils.calcYCoordinate(bounds, drawingRect, yPoints.get(i));
            lines[lineIndex++] = x + strokeWidth / 2;
            lines[lineIndex++] = drawingRect.bottom;
            lines[lineIndex++] = x + strokeWidth / 2;
            lines[lineIndex++] = y;
        }
    }

    static class DrawingData<C extends ChartCoordinate> extends ChartPointsDrawer.DrawingData<C> {

        private float[] mLines;

        DrawingData(ChartPointsData<C> pointsData) {
            super(pointsData);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);

            mLines = new float[pointsData.getPoints().size() * 4];
        }
    }
}
