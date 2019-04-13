package com.dlutskov.chart_lib.drawers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;

public class ChartBarsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartLinesDrawer<X, Y> {

    // MAX Alpha of rect which will cover unselected bars
    private static final int UNSELECTED_BARS_COVER_ALPHA = 100;

    // Paint which will be used to draw semi-transparent rect above unselected bars
    private Paint mCoverPaint;

    public ChartBarsDrawer(ChartView<X, Y> chartView) {
        super(chartView);

        mCoverPaint = new Paint();
        mCoverPaint.setColor(Color.WHITE);
        mCoverPaint.setStyle(Paint.Style.FILL);

        setDrawSelectedPointsDivider(false);
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
            drawingData.paint.setAlpha(mPointsAlpha);

            // Build lines
            int lineIndex = 0;
            for (int i = bounds.getMinXIndex(); i < bounds.getMaxXIndex(); i++) {
                float x = ChartUtils.calcXCoordinate(bounds, drawingRect, i);
                float y = ChartUtils.calcYCoordinate(bounds, drawingRect, pointsData.getPoints().get(i));
                float appearingRatio = drawingData.getAlpha() / (float) 255; // Reduce bar height with reducing bar visibility
                drawingData.mLines[lineIndex++] = x + columnWidth / 2;
                drawingData.mLines[lineIndex++] = drawingRect.bottom;
                drawingData.mLines[lineIndex++] = x + columnWidth / 2;
                drawingData.mLines[lineIndex++] = y + (drawingRect.height() - y) * (1 - appearingRatio);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {
        super.onDraw(canvas, drawingRect);

        if (mSelectedPointIndex > 0 && mSelectedPointAlpha > 0) {
            // Draw semi transparent rect above all tabs
            mCoverPaint.setAlpha((int) (UNSELECTED_BARS_COVER_ALPHA * mSelectedPointAlpha / (float) 255));
            canvas.drawRect(drawingRect, mCoverPaint);
            // Draw selected lines again to highlight them
            for (DrawingData<Y> drawingData : drawingDataList) {
                if (drawingData.isVisible()) {
                    int offset = (mSelectedPointIndex - getBounds().getMinXIndex()) * 4;
                    canvas.drawLines(drawingData.mLines, offset, 4, drawingData.getPaint());
                }
            }
        }
    }

    protected int getDrawDataAlpha(DrawingData<Y> drawingData) {
        return mPointsAlpha;
    }

    @Override
    protected void drawSelectedPoints(Canvas canvas, Rect drawingRect, float xPosition) {
        // Only selected bar will be with 100% opacity
    }

    public void setCoverColor(int color) {
        mCoverPaint.setColor(color);
    }

}
