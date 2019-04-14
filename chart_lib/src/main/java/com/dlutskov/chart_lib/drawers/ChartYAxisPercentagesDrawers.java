package com.dlutskov.chart_lib.drawers;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

public class ChartYAxisPercentagesDrawers<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartYAxisLabelsDrawer<X, Y> {

    private int mTopPadding;

    public ChartYAxisPercentagesDrawers(ChartView<X, Y> chartView, int size, int topPadding) {
        super(chartView, size);
        mTopPadding = topPadding;
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {}

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {}

    @Override
    public void onAfterDraw(Canvas canvas, Rect drawingRect) {
        int height = drawingRect.bottom - mTopPadding;
        for (int i = 0; i < mLabelsCount; i++) {
            float position = (i / (float) (mLabelsCount - 1));
            float y = drawingRect.bottom - height * position;
            mGridPaint.setAlpha(mAlpha);
            canvas.drawLine(0, y, drawingRect.right, y,mGridPaint);
            mLabelPaint.setAlpha(mAlpha);
            canvas.drawText(String.valueOf((int)(position * 100)), 0, y - mGridPadding, mLabelPaint);
        }
    }
}
