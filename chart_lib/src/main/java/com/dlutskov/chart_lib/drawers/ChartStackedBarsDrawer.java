package com.dlutskov.chart_lib.drawers;

import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;

public class ChartStackedBarsDrawer <X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartLinesDrawer<X, Y> {

    public ChartStackedBarsDrawer(ChartView chartView) {
        super(chartView);
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        int pointsCount = bounds.getMaxXIndex() - bounds.getMinXIndex();
        int columnWidth = drawingRect.width() / pointsCount;

        int lineIndex = 0;
        for (int i = bounds.getMinXIndex(); i < bounds.getMaxXIndex(); i++) {
            float prevY = drawingRect.bottom;
            for (ChartPointsData<Y> pointsData : data.getYPoints()) {
                DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
                if (drawingData == null) {
                    drawingData = new DrawingData<>(pointsData, columnWidth);
                    this.drawingData.add(drawingData);
                }
                if (!drawingData.isVisible()) continue;

                drawingData.paint.setStrokeWidth(columnWidth); // TODO

                float x = ChartUtils.calcXCoordinate(bounds, drawingRect, i);
                float y = ChartUtils.calcYCoordinate(bounds, drawingRect, pointsData.getPoints().get(i));
                float appearingRatio = drawingData.getAlpha() / (float)255; // Reduce bar height with reducing bar visibility
                float newY = prevY - (drawingRect.bottom - y) * appearingRatio;
                drawingData.mLines[lineIndex] = x + columnWidth / 2;
                drawingData.mLines[lineIndex + 1] = prevY;
                drawingData.mLines[lineIndex + 2] = x + columnWidth / 2;
                drawingData.mLines[lineIndex + 3] = newY;

                prevY = newY;
            }

            lineIndex += 4;
        }
    }

    @Override
    protected void onVisibilityAnimatorUpdate(DrawingData<Y> pointsData, int alpha) {
        System.out.println("@@@ ChartStackedBarsDrawer.onVisibilityAnimatorUpdate " + alpha);
        pointsData.setAlpha(alpha);
        // Do not change paint's alpha for this drawer - bars size will be reduced according to disappearance progress
        mChartView.invalidate();
    }
}
