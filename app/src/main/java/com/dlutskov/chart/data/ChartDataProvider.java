package com.dlutskov.chart.data;

import android.content.Context;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChartDataProvider {

    public static List<ChartData> readChartData(Context context) throws IOException {
        List<ChartData> chartData = new ArrayList<>();

        ChartLinesData<DateCoordinate, LongCoordinate> linesData = ChartDataParser.parse(context.getAssets().open("1/overview.json"));
        chartData.add(new ChartData(linesData, Collections.emptyMap()));

        linesData = ChartDataParser.parse(context.getAssets().open("2/overview.json"));
        chartData.add(new ChartData(linesData, Collections.emptyMap()));

        linesData = ChartDataParser.parse(context.getAssets().open("3/overview.json"));
        chartData.add(new ChartData(linesData, Collections.emptyMap()));

        linesData = ChartDataParser.parse(context.getAssets().open("4/overview.json"));
        chartData.add(new ChartData(linesData, Collections.emptyMap()));

        linesData = ChartDataParser.parse(context.getAssets().open("5/overview.json"));
        chartData.add(new ChartData(linesData, Collections.emptyMap()));


        return chartData;
    }

}
