package com.dlutskov.chart_lib.data;

import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

import java.util.List;

/**
 * Contains all data columns of the chart. All points collections should have the same size
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartLinesData<X extends ChartCoordinate, Y extends ChartCoordinate> {

    /**
     * X points collection which is common for all mYPoints
     */
    private final ChartPointsData<X> mXPoints;

    /**
     * Collection of all Y points of lines
     */
    private final List<ChartPointsData<Y>> mYPoints;

    public ChartLinesData(ChartPointsData<X> xPoints, List<ChartPointsData<Y>> yLines) {
        // Prevent wrong data creation
        for (ChartPointsData<Y> yPoints : yLines) {
            if (yPoints.getPoints().size() != xPoints.getPoints().size()) {
                throw new IllegalArgumentException("All ChartPointsData should have the same size");
            }
        }
        mXPoints = xPoints;
        mYPoints = yLines;
    }

    public ChartPointsData<X> getXPoints() {
        return mXPoints;
    }

    public List<ChartPointsData<Y>> getYPoints() {
        return mYPoints;
    }

}
