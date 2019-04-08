package com.dlutskov.chart.data;

import android.graphics.Color;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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

    public static ChartLinesData<DateCoordinate, LongCoordinate> parse(InputStream stream) throws IOException {
        final JsonParser parser = new JsonFactory().createParser(stream);
        parser.nextToken();

        Map<String, ChartColumnData> dataHolderMap = new HashMap<>();

        boolean isPercentage = false;
        boolean isStacked = false;
        boolean isYScaled = false;

        JsonToken token = parser.nextToken();
        while (token != JsonToken.END_OBJECT && token != null) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();

            switch (fieldname) {
                case "columns":
                    parseColumns(dataHolderMap, parser);
                    break;
                case "types":
                    parseChartDataValue(dataHolderMap, parser, (chartData, token1) -> chartData.type = parser.getValueAsString());
                    break;
                case "names":
                    parseChartDataValue(dataHolderMap, parser, (chartData, token1) -> chartData.name = parser.getValueAsString());
                    break;
                case "colors":
                    parseChartDataValue(dataHolderMap, parser, (chartData, token1) -> chartData.color = parser.getValueAsString());
                    break;
                case "percentage":
                    isPercentage = parser.getValueAsBoolean();
                    break;
                case "stacked":
                    isStacked = parser.getValueAsBoolean();
                    break;
                case "y_scaled":
                    isYScaled = parser.getValueAsBoolean();
                    break;
                default:
                    parser.skipChildren();
            }
            token = parser.nextToken();
        }

        ChartPointsData<DateCoordinate> xPoints = null;
        List<ChartPointsData<LongCoordinate>> yPoints = new ArrayList<>();
        for (ChartColumnData chartColumnData : dataHolderMap.values()) {
            if (chartColumnData.type.equals(ChartData.CHART_TYPE_X)) {
                xPoints = new ChartPointsData<>(chartColumnData.id, chartColumnData.name, chartColumnData.type, 0,
                        createDateCoordinates(chartColumnData.points));
            } else {
                yPoints.add(new ChartPointsData<>(chartColumnData.id, chartColumnData.name, chartColumnData.type, Color.parseColor(chartColumnData.color),
                        createLongCoordinates(chartColumnData.points)));
            }
        }
        ChartLinesData<DateCoordinate, LongCoordinate> result = new ChartLinesData<>(xPoints, yPoints);
        result.setPercentage(isPercentage);
        result.setStacked(isStacked);
        result.setYScaled(isYScaled);
        return result;
    }

    private static void parseColumns(Map<String, ChartColumnData> chartDataMap, JsonParser parser) throws IOException  {
        JsonToken token = parser.nextToken();
        while (token != JsonToken.END_ARRAY && token != null) {
            token = parser.nextToken();
            ChartColumnData chartData = null;
            while (token != JsonToken.END_ARRAY && token != null) {
                // First array item is graph id
                if (chartData == null) {
                    chartData = findGraphData(chartDataMap, parser.getValueAsString());
                } else {
                    chartData.points.add(parser.getValueAsLong());
                }
                token = parser.nextToken();
            }
            token = parser.nextToken();
        }
    }

    private static void parseChartDataValue(Map<String, ChartColumnData> chartDataMap, JsonParser parser, ParserPredicate predicate) throws IOException {
        JsonToken token = parser.nextToken();
        while (token != JsonToken.END_OBJECT && token != null) {
            String id = parser.getCurrentName();
            ChartColumnData chartData = findGraphData(chartDataMap, id);
            parser.nextToken();
            predicate.parse(chartData, token);
            token = parser.nextToken();
        }
    }

    /**
     * @param graphsDataMap - where to found
     * @param id - key to found
     * @return Returns the value with specified id, if there were no such value in the map - value wish specified id will
     *         be created, put to map and returned
     */
    private static ChartColumnData findGraphData(Map<String, ChartColumnData> graphsDataMap, String id) {
        ChartColumnData chartData = graphsDataMap.get(id);
        if (chartData == null) {
            chartData = new ChartColumnData(id);
            graphsDataMap.put(id, chartData);
        }
        return chartData;
    }

    /**
     * Used to parse specific field of ChartColumnData
     */
    interface ParserPredicate {
        void parse(ChartColumnData chartData, JsonToken token) throws IOException;
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
