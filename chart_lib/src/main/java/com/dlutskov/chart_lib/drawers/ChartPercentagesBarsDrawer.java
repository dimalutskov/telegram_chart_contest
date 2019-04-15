package com.dlutskov.chart_lib.drawers;

import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

public class ChartPercentagesBarsDrawer <X extends ChartCoordinate, Y extends ChartCoordinate>
        extends ChartStackedBarsDrawer<X, Y> {

    public ChartPercentagesBarsDrawer(ChartView chartView) {
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

        Y zero = (Y) bounds.getMinY().zero();
        Y maxValue = (Y) zero.clone();
        Y buf = (Y) maxValue.clone();
        ChartBounds<X, Y> localBounds = new ChartBounds<>(bounds);
        int lineIndex = 0;
        for (int i = bounds.getMinXIndex(); i <= bounds.getMaxXIndex(); i++) {

            // Calculate local bounds
            maxValue.set(zero);
            for (DrawingData<Y> drawingData : drawingDataList) {
                if (!drawingData.isVisible()) continue;
                drawingData.pointsData.getPoints().get(i).getPart(drawingData.getAlpha() / 255f, buf);
                maxValue.add(buf, maxValue);
            }
            localBounds.setMaxY(maxValue);

            drawStackedBars(data, localBounds, drawingRect, columnWidth, lineIndex, i);
            lineIndex += 4;
        }
    }

}
