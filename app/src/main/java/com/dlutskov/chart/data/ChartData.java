package com.dlutskov.chart.data;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;

import java.util.Map;

public class ChartData {

    private final ChartLinesData<DateCoordinate, LongCoordinate> mOverviewData;
    private final Map<Long, ChartLinesData<DateCoordinate, LongCoordinate>> mExpandedData;

    public ChartData(ChartLinesData<DateCoordinate, LongCoordinate> overviewData, Map<Long, ChartLinesData<DateCoordinate, LongCoordinate>> expandedData) {
        mOverviewData = overviewData;
        mExpandedData = expandedData;
    }

    public ChartLinesData<DateCoordinate, LongCoordinate> getOverviewData() {
        return mOverviewData;
    }

    public Map<Long, ChartLinesData<DateCoordinate, LongCoordinate>> getExpandedData() {
        return mExpandedData;
    }
}
