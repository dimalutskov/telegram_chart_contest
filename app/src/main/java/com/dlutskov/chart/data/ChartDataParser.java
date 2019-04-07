package com.dlutskov.chart.data;

import android.content.Context;
import android.graphics.Color;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;
import com.dlutskov.dlutskov.customchart.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsing using default org.json parser. Stream parser (like Jackson) is much faster but
 * not used to not include any 3rd party libs and to reduce final apk size
 */
public class ChartDataParser {

    public static List<ChartLinesData<DateCoordinate, LongCoordinate>> parse(Context context) {
        List<ChartLinesData<DateCoordinate, LongCoordinate>> result = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(readRawTextFile(context, R.raw.chart_data));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, ChartColumnData> dataHolderMap = new HashMap<>();

                JSONArray columns = jsonObject.getJSONArray("columns");
                for (int j = 0; j < columns.length(); j++) {
                    JSONArray points = columns.getJSONArray(j);
                    ChartColumnData pointsDataHolder = null;
                    for (int k = 0; k < points.length(); k++) {
                        if (k == 0) {
                            pointsDataHolder = findChartColumnData(dataHolderMap, points.getString(k));
                        } else {
                            pointsDataHolder.points.add(points.getLong(k));
                        }
                    }
                }

                JSONObject types = jsonObject.getJSONObject("types");
                for (int j = 0; j < types.names().length(); j++) {
                    String id = (String) types.names().get(j);
                    findChartColumnData(dataHolderMap, id).type = types.getString(id);
                }

                JSONObject names = jsonObject.getJSONObject("names");
                for (int j = 0; j < names.names().length(); j++) {
                    String id = (String) names.names().get(j);
                    findChartColumnData(dataHolderMap, id).name = names.getString(id);
                }

                JSONObject colors = jsonObject.getJSONObject("colors");
                for (int j = 0; j < colors.names().length(); j++) {
                    String id = (String) colors.names().get(j);
                    findChartColumnData(dataHolderMap, id).color = colors.getString(id);
                }

                ChartPointsData<DateCoordinate> xPoints = null;
                List<ChartPointsData<LongCoordinate>> yPoints = new ArrayList<>();
                for (ChartColumnData chartColumnData : dataHolderMap.values()) {
                    if (chartColumnData.type.equals("x")) {
                        xPoints = new ChartPointsData<>(chartColumnData.id, chartColumnData.name, 0,
                                createDateCoordinates(chartColumnData.points));
                    } else if (chartColumnData.type.equals("line")) {
                        yPoints.add(new ChartPointsData<>(chartColumnData.id, chartColumnData.name, Color.parseColor(chartColumnData.color),
                                createLongCoordinates(chartColumnData.points)));
                    }
                }
                result.add(new ChartLinesData<>(xPoints, yPoints));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static ChartColumnData findChartColumnData(Map<String, ChartColumnData> dataMap, String id) {
        ChartColumnData result = dataMap.get(id);
        if (result == null) {
            result = new ChartColumnData(id);
            dataMap.put(id, result);
        }
        return result;
    }

    private static List<LongCoordinate> createLongCoordinates(List<Long> points) {
        List<LongCoordinate> result = new ArrayList<>(points.size());
        for (Long point : points) {
            result.add(LongCoordinate.valueOf(point));
        }
        return result;
    }

    private static List<DateCoordinate> createDateCoordinates(List<Long> points) {
        List<DateCoordinate> result = new ArrayList<>(points.size());
        for (Long point : points) {
            result.add(DateCoordinate.valueOf(point));
        }
        return result;
    }

    private static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            return null;
        }
        return byteArrayOutputStream.toString();
    }

    private static class ChartColumnData {
        String id;
        String type;
        String name;
        String color;
        List<Long> points = new ArrayList<>();
        ChartColumnData(String id) {
            this.id = id;
        }
    }

}
