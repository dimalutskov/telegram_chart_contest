package com.dlutskov.chart_lib.data.coordinates;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Extends LongCoordinate with custom name displaying
 */
public class DateCoordinate extends LongCoordinate {

    private static final String DEFAULT_AXIS_DATE_FORMAT = "dd MMM";
    private static final String DEFAULT_FULL_DATE_FORMAT = "E, MMM dd";

    private final DateFormat mAxisFormat;
    private final DateFormat mDetailsFormat;

    DateCoordinate(long value) {
        this(value, new SimpleDateFormat(DEFAULT_AXIS_DATE_FORMAT, Locale.getDefault()),
                new SimpleDateFormat(DEFAULT_FULL_DATE_FORMAT, Locale.getDefault()));
    }

    DateCoordinate(long value, SimpleDateFormat axisDateFormat, SimpleDateFormat fullDateFormat) {
        super(value);
        mAxisFormat = axisDateFormat;
        mDetailsFormat = fullDateFormat;
        initLabels(value);
    }

    private void initLabels(long value) {
        mAxisName = mAxisFormat.format(value);
        mFullName = mDetailsFormat.format(value);
    }

    @Override
    public LongCoordinate set(LongCoordinate coordinate) {
        super.set(coordinate);
        initLabels(coordinate.mValue);
        return this;
    }

    @Override
    public DateCoordinate clone() {
        return new DateCoordinate(mValue);
    }

    public static DateCoordinate valueOf(long value) {
        return new DateCoordinate(value);
    }
}
