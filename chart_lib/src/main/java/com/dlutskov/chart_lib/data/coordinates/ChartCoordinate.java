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
     * @return Sum of current coordinate and @param coordinate
     */
    T add(T coordinate);

    /**
     * @return Subtraction of current coordinate from @param coordinate
     */
    T distanceTo(T coordinate);

    /**
     * Returns coordinate as a part of current coordinate
     * @param ratio Value from 0 to 1 (0 - 0, 1 - current coordinate)
     * @return Coordinate and ratio multiplication (current coordinate * ratio)
     */
    T getPart(float ratio);

    /**
     * @return Name which will be displayed as the axis label
     */
    String getAxisName();

    /**
     * Used to be displayed in Points Details window
     * @return Full name which represents current coordinate as a string
     */
    String getFullName();

    T clone();

}