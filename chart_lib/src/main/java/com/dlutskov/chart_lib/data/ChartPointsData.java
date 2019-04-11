package com.dlutskov.chart_lib.data;

import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.Pair;

import java.util.List;

/**
 * List of data column in the chart with all related info how to reflect it (X points or Y points)
 * @param <C> type axis chart coordinates
 */
public class ChartPointsData<C extends ChartCoordinate> {

    private final String mId;
    private final String mName;
    private final String mType;
    private final int mColor;
    private final List<C> mPoints;

    private final int mMinValueIndex;
    private final int mMaxValueIndex;

    public ChartPointsData(String id, String name, String type, int color, List<C> points) {
        this.mId = id;
        this.mName = name;
        this.mType = type;
        this.mColor = color;
        this.mPoints = points;

        Pair<Integer, Integer> minMaxIndexes = calculateMinMaxIndexes(this, 0, points.size() - 1);
        mMinValueIndex = minMaxIndexes.first;
        mMaxValueIndex = minMaxIndexes.second;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getType() {
        return mType;
    }

    public int getColor() {
        return mColor;
    }

    public List<C> getPoints() {
        return mPoints;
    }

    public C getMinValue() {
        return mPoints.get(mMinValueIndex);
    }

    public C getMaxValue() {
        return mPoints.get(mMaxValueIndex);
    }

    public int getMinValueIndex() {
        return mMinValueIndex;
    }

    public int getMaxValueIndex() {
        return mMaxValueIndex;
    }

    public static <C extends ChartCoordinate> Pair<Integer, Integer> calculateMinMaxIndexes(ChartPointsData<C> chartData, int startIndex, int endIndex) {
        C minValue = chartData.getMaxValue();
        C maxValue = chartData.getMinValue();
        int minIndex = startIndex;
        int maxIndex = startIndex;
        for (int i = startIndex; i <= endIndex; i++) {
            C value = chartData.getPoints().get(i);
            if (value.compareTo(minValue) < 0) {
                minValue = value;
                minIndex = i;
            } else if (value.compareTo(maxValue) > 0) {
                maxValue = value;
                maxIndex = i;
            }
        }
        return new Pair<>(minIndex, maxIndex);
    }

}
