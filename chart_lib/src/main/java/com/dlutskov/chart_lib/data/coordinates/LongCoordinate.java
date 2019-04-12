package com.dlutskov.chart_lib.data.coordinates;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * ChartCoordinate implementation which contains simple Long value inside
 */
public class LongCoordinate implements ChartCoordinate<LongCoordinate> {

    private static final NavigableMap<Long, String> sNameFormatSuffixes = new TreeMap<>();
    static {
        sNameFormatSuffixes.put(1_000L, "k");
        sNameFormatSuffixes.put(1_000_000L, "M");
    }

    Long mValue;
    String mAxisName;
    String mFullName;

    LongCoordinate(long value) {
        setInternal(value);
    }

    private LongCoordinate setInternal(long value) {
        mValue = value;
        mAxisName = formatName(value);
        mFullName = String.valueOf(value);
        return this;
    }

    @Override
    public int compareTo(LongCoordinate value) {
        return mValue.compareTo(value.mValue);
    }

    @Override
    public float calcCoordinateRatio(LongCoordinate min, LongCoordinate max) {
        return (float)(mValue - min.mValue) / (max.mValue - min.mValue);
    }

    @Override
    public LongCoordinate add(LongCoordinate coordinate) {
        return LongCoordinate.valueOf(coordinate.mValue + mValue);
    }

    @Override
    public LongCoordinate add(LongCoordinate coordinate, LongCoordinate result) {
        return result.setInternal(coordinate.mValue + mValue);
    }

    @Override
    public LongCoordinate distanceTo(LongCoordinate coordinate) {
        return LongCoordinate.valueOf(coordinate.mValue - mValue);
    }

    @Override
    public LongCoordinate distanceTo(LongCoordinate coordinate, LongCoordinate result) {
        return result.setInternal(coordinate.mValue - mValue);
    }

    @Override
    public LongCoordinate getPart(float ratio) {
        return LongCoordinate.valueOf((long) (mValue * ratio)) ;
    }

    @Override
    public LongCoordinate getPart(float ratio, LongCoordinate result) {
        return result.setInternal((long) (mValue * ratio));
    }

    @Override
    public LongCoordinate set(LongCoordinate coordinate) {
        return setInternal(coordinate.mValue);
    }

    @Override
    public String getAxisName() {
        return mAxisName;
    }

    @Override
    public String getFullName() {
        return mFullName;
    }

    @Override
    public String toString() {
        return getAxisName();
    }

    @Override
    public LongCoordinate clone() {
        return new LongCoordinate(mValue);
    }

    public long getValue() {
        return mValue;
    }

    private static String formatName(long value) {
        if (value == Long.MIN_VALUE) return formatName(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatName(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = sNameFormatSuffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static LongCoordinate valueOf(long value) {
        return new LongCoordinate(value);
    }
}
// TODO Crash on empty
// ChartPreview