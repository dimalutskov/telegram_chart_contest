package com.dlutskov.chart_lib.drawers;

import android.graphics.Color;
import android.graphics.Paint;

import com.dlutskov.chart_lib.ChartUtils;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

/**
 * Base drawer for displaying text on the chart's axis
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public abstract class ChartAxisLabelsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartDataDrawer<X, Y> {

    /**
     * Used to tell the ChartView that the drawing rect of the drawer will be the same as the chart size
     */
    public static int SIZE_MATCH_PARENT = -1;

    public static int DEFAULT_AXIS_LABELS_COUNT = 5;

    /**
     * Size on the chart's canvas which is need to draw axis labels. Width for x and Height for y
     * Will affect on the drawing rect
     */
    private int mSize;

    /**
     * Reflects whether the axis labels will be drawn over the rest chart drawers
     * or will be drawn aside and the drawing rect of the rest drawers will be change (will exclude mSize value)
     */
    private boolean mDrawOverPoints;

    /**
     * Number of labels which will be displayed on the axis
     */
    protected int mLabelsCount;

    protected int mTextSize;

    protected Paint mLabelPaint;

    ChartAxisLabelsDrawer(ChartView<X, Y> chartView, int size) {
        super(chartView);
        mSize = size;
        mLabelsCount = DEFAULT_AXIS_LABELS_COUNT;
        mTextSize = ChartUtils.getPixelForDp(chartView.getContext(), 12);

        mLabelPaint = new Paint();
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setTextSize(mTextSize);
        mLabelPaint.setColor(Color.BLACK);
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public void setDrawOverPoints(boolean drawOverPoints) {
        mDrawOverPoints = drawOverPoints;
    }

    public boolean isDrawOverPoints() {
        return mDrawOverPoints;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        mLabelPaint.setTextSize(textSize);
    }

    public void setTextColor(int textColor) {
        mLabelPaint.setColor(textColor);
    }

}
