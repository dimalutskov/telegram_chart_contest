package com.dlutskov.chart_lib.drawers;

import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;

public class ChartStackedBarsDrawer <X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartBarsDrawer<X, Y> {

    public ChartStackedBarsDrawer(ChartView chartView) {
        super(chartView);
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        int pointsCount = bounds.getMaxXIndex() - bounds.getMinXIndex();
        int columnWidth = drawingRect.width() / pointsCount;
        // Adjust columnWidth to get rid of gaps between bars
        int columnWidthAdjustment = (drawingRect.width() % pointsCount) / pointsCount + 2;

        // Set column widths
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            drawingData.paint.setStrokeWidth(columnWidth + columnWidthAdjustment);
            drawingData.paint.setAlpha(mPointsAlpha);
        }

        int lineIndex = 0;
        for (int i = bounds.getMinXIndex(); i < bounds.getMaxXIndex(); i++) {
            drawStackedBars(data, bounds, drawingRect, columnWidth, lineIndex, i);
            lineIndex += 4;
        }
    }

    void drawStackedBars(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect,
                                   int columnWidth, int lineIndex, int pointIndex) {
        float prevY = drawingRect.bottom;
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (!drawingData.isVisible()) continue;

            float x = ChartUtils.calcXCoordinate(bounds, drawingRect, pointIndex);
            float y = ChartUtils.calcYCoordinate(bounds, drawingRect, pointsData.getPoints().get(pointIndex));
            float appearingRatio = drawingData.getAlpha() / (float) 255; // Reduce bar height with reducing bar visibility
            float newY = prevY - (drawingRect.bottom - y) * appearingRatio;
            drawingData.mLines[lineIndex] = x + columnWidth / 2;
            drawingData.mLines[lineIndex + 1] = prevY;
            drawingData.mLines[lineIndex + 2] = x + columnWidth / 2;
            drawingData.mLines[lineIndex + 3] = newY;

            prevY = newY;
        }
    }

}
