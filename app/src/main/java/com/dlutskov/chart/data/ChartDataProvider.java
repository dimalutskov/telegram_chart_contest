package com.dlutskov.chart.data;

import android.content.Context;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChartDataProvider {

    public static List<ChartLinesData<DateCoordinate, LongCoordinate>> readChartData(Context context) throws IOException {
        List<ChartLinesData<DateCoordinate, LongCoordinate>> chartData = new ArrayList<>();
        chartData.add(ChartDataParser.parse(context.getAssets().open("1/overview.json")));
        chartData.add(ChartDataParser.parse(context.getAssets().open("2/overview.json")));
        chartData.add(ChartDataParser.parse(context.getAssets().open("3/overview.json")));
        chartData.add(ChartDataParser.parse(context.getAssets().open("4/overview.json")));
        chartData.add(ChartDataParser.parse(context.getAssets().open("5/overview.json")));
        return chartData;
    }

    public static ChartLinesData<DateCoordinate, LongCoordinate> getExpandedChartData(Context context, String chartFolderName, long timestamp) throws IOException {
        String folderName = new SimpleDateFormat("yyyy-MM").format(timestamp);
        String fileName = new SimpleDateFormat("dd").format(timestamp);

        InputStream stream = context.getAssets().open(chartFolderName + "/" + folderName + "/" + fileName + ".json");
        return ChartDataParser.parse(stream);
    }
    
}
