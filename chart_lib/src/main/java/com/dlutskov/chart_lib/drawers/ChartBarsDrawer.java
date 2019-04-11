package com.dlutskov.chart_lib.drawers;

import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;

import java.util.List;

public class ChartBarsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartLinesDrawer<X, Y> {

    public ChartBarsDrawer(ChartView<X, Y> chartView) {
        super(chartView);
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        int pointsCount = bounds.getMaxXIndex() - bounds.getMinXIndex();
        int columnWidth = drawingRect.width() / pointsCount;
        // Adjust columnWidth to get rid of gaps between bars
        int columnWidthAdjustment = (drawingRect.width() % pointsCount) / pointsCount + 2;
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (drawingData == null) {
                drawingData = new DrawingData<>(pointsData, columnWidth);
                this.drawingDataList.add(drawingData);
            }
            if (!drawingData.isVisible()) continue;

            drawingData.paint.setStrokeWidth(columnWidth + columnWidthAdjustment);
            buildLines(drawingData.mLines, pointsData.getPoints(), bounds, columnWidth, drawingRect);
        }
    }

    private void buildLines(float lines[], List<Y> yPoints, ChartBounds<X, Y> bounds, int strokeWidth, Rect drawingRect) {
        int lineIndex = 0;
        for (int i = bounds.getMinXIndex(); i < bounds.getMaxXIndex(); i++) {
            float x = ChartUtils.calcXCoordinate(bounds, drawingRect, i);
            float y = ChartUtils.calcYCoordinate(bounds, drawingRect, yPoints.get(i));
            lines[lineIndex++] = x + strokeWidth / 2;
            lines[lineIndex++] = drawingRect.bottom;
            lines[lineIndex++] = x + strokeWidth / 2;
            lines[lineIndex++] = y;
        }
    }

}
