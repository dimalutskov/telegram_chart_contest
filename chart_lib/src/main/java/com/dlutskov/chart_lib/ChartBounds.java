package com.dlutskov.chart_lib;

import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

/**
 * Contains appropriate x and y boundaries
 * As all y points has the same x values - collection indexes used for x values for better looping
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartBounds<X extends ChartCoordinate, Y extends ChartCoordinate> {

    private int mMinXIndex;
    private int mMaxXIndex;
    private Y mMaxY;
    private Y mMinY;

    public ChartBounds(ChartBounds<X, Y> chartBounds) {
        this(chartBounds.mMinXIndex, chartBounds.mMaxXIndex, chartBounds.mMinY, chartBounds.mMaxY);
    }

    public ChartBounds(int minXIndex, int maxXIndex, Y minY, Y maxY) {
        update(minXIndex, maxXIndex, minY, maxY);
    }

    public void update(ChartBounds<X, Y> chartBounds) {
        update(chartBounds.mMinXIndex, chartBounds.mMaxXIndex, chartBounds.mMinY, chartBounds.mMaxY);
    }

    public void update(int minXIndex, int maxXIndex, Y minY, Y maxY) {
        this.mMinXIndex = minXIndex;
        this.mMaxXIndex = maxXIndex;
        this.mMinY = minY;
        this.mMaxY = maxY;
    }

    public int getMinXIndex() {
        return mMinXIndex;
    }

    public void setMinXIndex(int minXIndex) {
        this.mMinXIndex = minXIndex;
    }

    public int getMaxXIndex() {
        return mMaxXIndex;
    }

    public void setMaxXIndex(int maxXIndex) {
        this.mMaxXIndex = maxXIndex;
    }

    public Y getMaxY() {
        return mMaxY;
    }

    public void setMaxY(Y maxY) {
        this.mMaxY = maxY;
    }

    public Y getMinY() {
        return mMinY;
    }

    public void setMinY(Y minY) {
        this.mMinY = minY;
    }

    public int getXPointsCount() {
        return mMaxXIndex - mMinXIndex;
    }

    public float calcYCoordinateRatio(Y coordinate) {
        return coordinate.calcCoordinateRatio(mMinY, mMaxY);
    }

    public boolean isXBoundsEquals(ChartBounds<X, Y> bounds) {
        return mMinXIndex == bounds.mMinXIndex && mMaxXIndex == bounds.mMaxXIndex;
    }

    public boolean isYBoundsEquals(ChartBounds<X, Y> bounds) {
        return mMinY.compareTo(bounds.getMinY()) == 0 && mMaxY.compareTo(bounds.getMaxY()) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChartBounds<?, ?> that = (ChartBounds<?, ?>) o;
        return mMinXIndex == that.mMinXIndex &&
                mMaxXIndex == that.mMaxXIndex &&
                mMaxY.equals(that.mMaxY) &&
                mMinY.equals(that.mMinY);
    }

}