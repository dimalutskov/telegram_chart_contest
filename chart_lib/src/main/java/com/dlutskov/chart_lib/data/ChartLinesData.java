package com.dlutskov.chart_lib.data;

import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.Pair;

import java.util.List;
import java.util.Set;

/**
 * Contains all data columns of the chart. All points collections should have the same size
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartLinesData<X extends ChartCoordinate, Y extends ChartCoordinate> {

    public static final String CHART_TYPE_X = "x";
    public static final String CHART_TYPE_LINE = "line";
    public static final String CHART_TYPE_BAR = "bar";
    public static final String CHART_TYPE_AREA = "area";

    /**
     * X points collection which is common for all mYPoints
     */
    private final ChartPointsData<X> mXPoints;

    /**
     * Collection of all Y points of lines
     */
    private final List<ChartPointsData<Y>> mYPoints;

    private boolean isPercentage;
    private boolean isStacked;
    private boolean isYScaled;

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

    public boolean isPercentage() {
        return isPercentage;
    }

    public void setPercentage(boolean percentage) {
        isPercentage = percentage;
    }

    public boolean isStacked() {
        return isStacked;
    }

    public void setStacked(boolean stacked) {
        isStacked = stacked;
    }

    public boolean isYScaled() {
        return isYScaled;
    }

    public void setYScaled(boolean YScaled) {
        isYScaled = YScaled;
    }

    public Pair<Y, Y> calculateYBounds(int minXIndex, int maxXIndex, Set<String> hiddenChartLines, Pair<Y, Y> result) {
        return isStacked ? calculateStackedYBounds(minXIndex, maxXIndex, hiddenChartLines, result)
                         : calculateDefaultYBounds(minXIndex, maxXIndex, hiddenChartLines, result);
    }

    /**
     * Just finds min and max Y values from all visible chart points
     */
    private Pair<Y, Y> calculateDefaultYBounds(int minXIndex, int maxXIndex, Set<String> hiddenChartLines, Pair<Y, Y> result) {
        Y minValue = null, maxValue = null;
        for (int i = minXIndex; i <= maxXIndex; i++) {
            for (ChartPointsData<Y> pointsData : mYPoints) {
                if (hiddenChartLines.contains(pointsData.getId())) {
                    // Ignore hidden chart lines
                    continue;
                }

                // Just compare value to find min and max values
                Y value = pointsData.getPoints().get(i);
                if (minValue == null) {
                    // MinMax values were not initialized - init it
                    minValue = value;
                    maxValue = value;
                } else {
                    if (value.compareTo(minValue) < 0) {
                        minValue = value;
                    } else if (value.compareTo(maxValue) > 0) {
                        maxValue = value;
                    }
                }
            }
        }
        return result.update(minValue, maxValue);
    }

    /**
     * Finds min and max SUM of Y values for each X point
     */
    private Pair<Y, Y> calculateStackedYBounds(int minXIndex, int maxXIndex, Set<String> hiddenChartLines, Pair<Y, Y> result) {
        Y minValue = null, maxValue = null;
        Y sum = null; // Sum of y points for specific x point
        boolean resetSumBuffer;
        for (int i = minXIndex; i <= maxXIndex; i++) {
            resetSumBuffer = true;
            for (ChartPointsData<Y> pointsData : mYPoints) {
                if (hiddenChartLines.contains(pointsData.getId())) {
                    // Ignore hidden chart lines
                    continue;
                }
                Y value = pointsData.getPoints().get(i);
                if (sum == null) {
                    sum = (Y) value.clone();
                    resetSumBuffer = false;
                } else if (resetSumBuffer) {
                    sum.set(value);
                    resetSumBuffer = false;
                } else {
                    sum.add(value, sum);
                }
            }

            if (minValue == null) {
                // MinMax values were not initialized - init it
                minValue = (Y) sum.clone();
                maxValue = (Y) sum.clone();
            } else {
                if (sum.compareTo(minValue) < 0) {
                    minValue.set(sum);
                } else if (sum.compareTo(maxValue) > 0) {
                    maxValue.set(sum);
                }
            }

        }
        return result.update(minValue, maxValue);
    }


}
