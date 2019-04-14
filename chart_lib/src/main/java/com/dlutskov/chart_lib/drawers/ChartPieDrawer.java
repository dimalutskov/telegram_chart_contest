package com.dlutskov.chart_lib.drawers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChartPieDrawer<X extends ChartCoordinate, Y extends ChartCoordinate>
        extends ChartPointsDrawer<X, Y, ChartPieDrawer.DrawingData<Y>> {

    private RectF mRect = new RectF();

    private int mRotationAngle = 0;

    public ChartPieDrawer(ChartView chartView) {
        super(chartView);
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
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        Map<String, Y> sumMap = new HashMap<>();
        Y zero = (Y) bounds.getMinY().zero();
        Y totalSum = (Y) bounds.getMinY().zero();
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            Y sum = (Y) drawingData.calculateSum(bounds.getMinXIndex(), bounds.getMaxXIndex()).clone();
            sum.getPart(drawingData.getAlpha() / 255f, sum);

            totalSum.add(sum, totalSum);
            sumMap.put(pointsData.getId(), sum);
        }
        int percents = 0;
        for (int i = 0; i < drawingDataList.size(); i++) {
            DrawingData<Y> drawingData = drawingDataList.get(i);
            Y sum = sumMap.get(drawingData.getId());
            float ratio = sum.calcCoordinateRatio(zero, totalSum);
            drawingData.sweepAngle = Math.round(ratio * 360);
            int p = i == drawingDataList.size() - 1 ? 100 - percents : Math.round(ratio * 100);
            drawingData.text = p + "%";
            percents += p;
        }
    }

    @Override
    protected void onDraw(Canvas canvas, Rect drawingRect) {
        float centerX = drawingRect.width() / 2;
        float centerY = drawingRect.height() / 2 + mChartView.getPaddingTop();
        float size = drawingRect.height() / 2;

        mRect.set(centerX - size, centerY - size, centerX + size, centerY + size);

        // Rotate canvas if need
        if (mRotationAngle != 0) {
            canvas.save();
            canvas.rotate(mRotationAngle, centerX, centerY);
        }

        int startAngle = 0;
        for (int i = 0; i < drawingDataList.size(); i++) {
            DrawingData<Y> drawingData = drawingDataList.get(i);
            // Draw pie
            float sweepAngle = i == drawingDataList.size() - 1 ? 360 - startAngle : drawingData.sweepAngle;
            drawingData.paint.setAlpha(mPointsAlpha);
            canvas.drawArc(mRect, startAngle, sweepAngle, true, drawingData.paint);
            // Draw text
            drawingData.textPaint.setAlpha(mPointsAlpha);
            drawText(drawingData, canvas, drawingRect, startAngle, drawingData.sweepAngle);
            startAngle += drawingData.sweepAngle;
        }

        if (mRotationAngle != 0) {
            canvas.restore();
        }
    }

    private void drawText(DrawingData<Y> drawingData, Canvas canvas, Rect drawingRect, int startAngle, float sweepAngle) {
        // TODO
    }

    public void setRotationAngle(int rotationAngle) {
        mRotationAngle = rotationAngle;
    }

    static class DrawingData<Y extends ChartCoordinate> extends ChartPointsDrawer.DrawingData<Y> {

        // Values which corresponds to the calculated percents and sweepAngle
        private Y mSumValue;
        private int mMinXIndex;
        private int mMaxXIndex;

        private Y mZero;
        private Y mSumBuf;

        float sweepAngle;
        String text;

        Paint textPaint;

        DrawingData(ChartPointsData<Y> pointsData) {
            super(pointsData);

            mZero = (Y) pointsData.getMinValue().zero();
            mSumBuf = (Y) mZero.clone();

            paint.setStyle(Paint.Style.FILL);

            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setColor(Color.WHITE);
        }

        Y calculateSum(int minXIndex, int maxXIndex) {
            if (mSumValue == null) {
                // Recalculate all points
                mSumBuf.set(mZero);
                List<Y> points = pointsData.getPoints();
                for (int i = minXIndex; i <= maxXIndex; i++) {
                    mSumBuf.add(points.get(i), mSumBuf);
                }
            } else {
                List<Y> yPoints = pointsData.getPoints();
                // Recalculate only newly added or removed values
                mSumBuf.set(mSumValue);
                // LEFT SIDE
                if (minXIndex > mMinXIndex) {
                    // Subtract left values
                    for (int i = mMinXIndex; i < minXIndex; i++) {
                        mSumBuf.subtract(yPoints.get(i), mSumBuf);
                    }
                } else {
                    // Add left values
                    for (int i = minXIndex; i < mMinXIndex; i++) {
                        mSumBuf.add(yPoints.get(i), mSumBuf);
                    }
                }
                // RIGHT SIDE
                if (maxXIndex < mMaxXIndex) {
                    // Subtract right values
                    for (int i = maxXIndex; i < mMaxXIndex; i++) {
                        mSumBuf.subtract(yPoints.get(i), mSumBuf);
                    }
                } else {
                    // Add right values
                    for (int i = mMaxXIndex; i < maxXIndex; i++) {
                        mSumBuf.add(yPoints.get(i), mSumBuf);
                    }
                }
            }

            if (mSumValue == null) {
                mSumValue = (Y) mSumBuf.clone();
            } else {
                mSumValue.set(mSumBuf);
            }

            mMinXIndex = minXIndex;
            mMaxXIndex = maxXIndex;
            return mSumValue;
        }
    }

}
