package com.dlutskov.chart_lib.data.coordinates;

/**
 * Base data type for X or Y axis chart's coordinates
 * Used to support different data types displaying as a chart (also with complex types like BigDecimal)
 * @param <T>
 */
public interface ChartCoordinate<T extends ChartCoordinate> extends Comparable<T>, Cloneable {

    /**
     * Used to find location on the screen by specifying min and max displaying coordinates
     * Calculation: (Current coordinate - min) / (max - min)
     * @return Value from 0 to 1 which reflects ratio between max and min coordinates (1 - max, 0 - min)
     */
    float calcCoordinateRatio(T min, T max);

    /**
     * @return Sum of current coordinate and @param coordinate as a new instance
     */
    T add(T coordinate);

    /**
     * @return The same as in {@link #add(ChartCoordinate)} but result will be written to "result" param.
     * Override it for your coordinate to not create new instance each time calling this method
     */
    default T add(T coordinate, T result) {
        result.set(add(coordinate));
        return result;
    }

    /**
     * @return Subtraction of current coordinate from @param coordinate as a new instance
     */
    T distanceTo(T coordinate);

    /**
     * @return The same as in {@link #distanceTo(ChartCoordinate)} but result will be written to "result" param.
     * Override it for your coordinate to not create new instance each time calling this method
     */
    default T distanceTo(T coordinate, T result) {
        result.set(distanceTo(coordinate));
        return result;
    }

    /**
     * Returns coordinate as first part of current coordinate as first new instance
     * @param ratio Value from 0 to 1 (0 - 0, 1 - current coordinate)
     * @return Coordinate and ratio multiplication (current coordinate * ratio)
     */
    T getPart(float ratio);

    /**
     * @return The same as in {@link #getPart(float)} but result will be written to "result" param.
     * Override it for your coordinate to not create new instance each time calling this method
     */
    default T getPart(float ratio, T result) {
        result.set(getPart(ratio));
        return result;
    }

    /**
     * Sets internal coordinate value.
     * Used to keep and update single object for calculations and not create new one
     */
    T set(T coordinate);

    /**
     * @return Name which will be displayed as the axis label
     */
    String getAxisName();

    /**
     * @return Name which will be displayed as the interval in the chart header
     */
    String getHeaderName();

    /**
     * Used to be displayed in Points Details window
     * @return Full name which represents current coordinate as a string
     */
    String getFullName();

    /**
     * Returns name for expanded charts coordinates
     */
    String getExpandedName();

    T clone();

}