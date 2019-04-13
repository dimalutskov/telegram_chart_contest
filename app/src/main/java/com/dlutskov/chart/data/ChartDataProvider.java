package com.dlutskov.chart.data;

import android.content.Context;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

public class ChartDataProvider {

    public static ChartLinesData<DateCoordinate, LongCoordinate> getOverviewChartData(Context context, String assetsFolderName) throws IOException {
        return ChartDataParser.parse(context.getAssets().open(assetsFolderName + "/overview.json"));
    }

    public static ChartLinesData<DateCoordinate, LongCoordinate> getExpandedChartData(Context context, String chartFolderName, long timestamp) throws IOException {
        String folderName = new SimpleDateFormat("yyyy-MM").format(timestamp);
        String fileName = new SimpleDateFormat("dd").format(timestamp);

        InputStream stream = context.getAssets().open(chartFolderName + "/" + folderName + "/" + fileName + ".json");
        return ChartDataParser.parse(stream);
    }
    
}
