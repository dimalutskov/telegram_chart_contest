package com.dlutskov.chart;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;

public class ChartData {

    public static final String CHART_ID_LINES = "lines";
    public static final String CHART_ID_SCALED_LINES = "scaled_lines";
    public static final String CHART_ID_STACKED_BARS = "stacked_bars";
    public static final String CHART_ID_SINGLE_BAR = "single_bar";
    public static final String CHART_ID_AREAS = "areas";

    final String id;
    final String name;
    final String assetsFolderName;
    final ChartLinesData<DateCoordinate, LongCoordinate> linesData;

    public ChartData(String id, String name, String assetsFolderId, ChartLinesData<DateCoordinate, LongCoordinate> linesData) {
        this.id = id;
        this.name = name;
        this.assetsFolderName = assetsFolderId;
        this.linesData = linesData;
    }
}
