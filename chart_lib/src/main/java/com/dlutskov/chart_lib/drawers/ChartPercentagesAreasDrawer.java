package com.dlutskov.chart_lib.drawers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;

public class ChartPercentagesAreasDrawer<X extends ChartCoordinate, Y extends ChartCoordinate>
        extends ChartPointsDrawer<X, Y, ChartPercentagesAreasDrawer.DrawingData<Y>> {

    // Buffer values which used for data calculations - to not create new instance on each rebuild
    private ChartBounds<X, Y> mLocalBounds;
    private Y mLocalMaxValue;
    private Y mLocalYValue;

    public ChartPercentagesAreasDrawer(ChartView<X, Y> chartView) {
        super(chartView);
    }

    @Override
    public void updateBounds(ChartBounds<X, Y> currentBounds, ChartBounds<X, Y> targetBounds) {
        // Do not call super - no need to start bounds update animator
        updateBoundsInternal(targetBounds);
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        // Update local bounds reference
        if (mLocalBounds == null) {
            mLocalBounds = new ChartBounds<>(bounds);
        } else {
            mLocalBounds.update(bounds);
        }

        int lineIndex = 0;
        for (int i = bounds.getMinXIndex(); i <= bounds.getMaxXIndex(); i++) { // TODO <=
//            boolean maxValueInited = false;
//            for (ChartPointsData<Y> pointsData : data.getYPoints()) {
//                // Create drawing data if it's not exists
//                DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
//                if (drawingData == null) {
//                    drawingData = new DrawingData<>(pointsData, ChartUtils.getPixelForDp(mChartView.getContext(), 1));
//                    this.drawingDataList.add(drawingData);
//                }
//
//                if (!drawingData.isVisible()) continue;
//
//                // Calculate Y value according to visibility
//                Y yPoint = pointsData.getPoints().get(i);
//                if (mLocalYValue == null) {
//                    mLocalYValue = (Y) yPoint.clone();
//                } else {
//                    mLocalYValue.set(yPoint);
//                }
//                yPoint.getPart(drawingData.getAlpha() / (float)255, mLocalYValue);
//
//                // Add y value to sum
//                if (!maxValueInited) {
//                    // Set first value to local max value object
//                    maxValueInited = true;
//                    if (mLocalMaxValue == null) {
//                        mLocalMaxValue = (Y) mLocalYValue.clone();
//                    } else {
//                        mLocalMaxValue.set(mLocalYValue);
//                    }
//                } else {
//                    // Add new value to previous value
//                    mLocalMaxValue.add(mLocalYValue, mLocalMaxValue);
//                }
//            }
//
//            mLocalBounds.setMaxY(mLocalMaxValue);

//            drawStackedBars(data, mLocalBounds, drawingRect, lineIndex, i);
            buildAreaPathes(data, bounds, drawingRect, i);
            lineIndex += 4;
        }
    }

    private void drawStackedBars(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect,
                                 int lineIndex, int pointIndex) {
        int lineWidth = ChartUtils.getPixelForDp(mChartView.getContext(), 1);

        float prevY = drawingRect.bottom;
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (drawingData == null) {
                drawingData = new DrawingData<>(pointsData, ChartUtils.getPixelForDp(mChartView.getContext(), 1));
                this.drawingDataList.add(drawingData);
            }
            if (!drawingData.isVisible()) continue;

            drawingData.paint.setStrokeWidth(lineWidth); // TODO
            drawingData.paint.setAlpha(mPointsAlpha);

            float x = ChartUtils.calcXCoordinate(bounds, drawingRect, pointIndex);
            float y = ChartUtils.calcYCoordinate(bounds, drawingRect, pointsData.getPoints().get(pointIndex));
            float appearingRatio = drawingData.getAlpha() / (float) 255; // Reduce bar height with reducing bar visibility
            float newY = prevY - (drawingRect.bottom - y) * appearingRatio;
            drawingData.mLines[lineIndex] = x;// + columnWidth / 2;
            drawingData.mLines[lineIndex + 1] = prevY;
            drawingData.mLines[lineIndex + 2] = x;// + columnWidth / 2;
            drawingData.mLines[lineIndex + 3] = newY;

            prevY = newY;
        }
    }

//    @Override
//    public void onDraw(Canvas canvas, Rect drawingRect) {
//        int linesCount = (getBounds().getMaxXIndex() - getBounds().getMinXIndex()) * 4;
//        for (DrawingData<Y> drawingData : drawingDataList) {
//            if (drawingData.isVisible()) {
//                canvas.drawLines(drawingData.mLines, 0, linesCount, drawingData.getPaint());
//            }
//        }
//    }

    private void buildAreaPathes(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect, int pointIndex) {
        float prevY = drawingRect.bottom;
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (drawingData == null) {
                drawingData = new DrawingData<>(pointsData, ChartUtils.getPixelForDp(mChartView.getContext(), 1));
                this.drawingDataList.add(drawingData);
            }
            if (!drawingData.isVisible()) continue;

            if (pointIndex == bounds.getMinXIndex()) {
                // First point
                drawingData.mPath.reset();
                drawingData.mPath.moveTo(drawingRect.left, drawingRect.bottom);
            }

            float x = ChartUtils.calcXCoordinate(bounds, drawingRect, pointIndex);
            float y = ChartUtils.calcYCoordinate(bounds, drawingRect, pointsData.getPoints().get(pointIndex));
            float appearingRatio = drawingData.getAlpha() / (float) 255; // Reduce bar height with reducing bar visibility
            float newY = prevY - (drawingRect.bottom - y) * appearingRatio;

            drawingData.mPath.lineTo(x, newY);

            if (pointIndex == bounds.getMaxXIndex()) {
                // Last point - connect to the first point
                drawingData.mPath.lineTo(drawingRect.right, drawingRect.bottom);
                drawingData.mPath.lineTo(drawingRect.left, drawingRect.bottom);
            }

            prevY = newY;
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {
        for (int i = drawingDataList.size() - 1; i >= 0; i--) {
            DrawingData<Y> data = drawingDataList.get(i);
            if (data.isVisible()) {
                canvas.drawPath(data.mPath, data.paint);
            }
        }
    }

    static class DrawingData<C extends ChartCoordinate> extends ChartPointsDrawer.DrawingData<C> {

        protected float[] mLines;
        Path mPath;

        DrawingData(ChartPointsData<C> pointsData, int strokeWidth) {
            super(pointsData);

            paint.setStyle(Paint.Style.FILL);
//            paint.setStrokeWidth(strokeWidth);

            mPath = new Path();

            mLines = new float[pointsData.getPoints().size() * 4];
        }

    }
}
