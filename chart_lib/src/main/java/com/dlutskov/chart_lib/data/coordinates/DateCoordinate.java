package com.dlutskov.chart_lib.data.coordinates;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Extends LongCoordinate with custom name displaying
 */
public class DateCoordinate extends LongCoordinate {

    private static final String DEFAULT_AXIS_DATE_FORMAT = "dd MMM";
    private static final String DEFAULT_HEADER_DATE_FORMAT = "dd MMM yyyy";
    private static final String DEFAULT_FULL_DATE_FORMAT = "E, MMM dd";
    private static final String DEFAULT_EXPANDED_DATE_FORMAT = "HH:mm";

    private final DateFormat mAxisFormat;
    private final DateFormat mHeaderFormat;
    private final DateFormat mDetailsFormat;
    private final DateFormat mExpandedFormat;

    DateCoordinate(long value) {
        this(value, new SimpleDateFormat(DEFAULT_AXIS_DATE_FORMAT, Locale.getDefault()),
                new SimpleDateFormat(DEFAULT_HEADER_DATE_FORMAT, Locale.getDefault()),
                new SimpleDateFormat(DEFAULT_FULL_DATE_FORMAT, Locale.getDefault()),
                new SimpleDateFormat(DEFAULT_EXPANDED_DATE_FORMAT, Locale.getDefault()));
    }

    DateCoordinate(long value, SimpleDateFormat axisDateFormat, SimpleDateFormat headerFormat,
                   SimpleDateFormat fullDateFormat, SimpleDateFormat expandedDateFormat) {
        super(value);
        mAxisFormat = axisDateFormat;
        mHeaderFormat = headerFormat;
        mDetailsFormat = fullDateFormat;
        mExpandedFormat = expandedDateFormat;
        initLabels(value);
    }

    private void initLabels(long value) {
        mAxisName = mAxisFormat.format(value);
        mHeaderName = mHeaderFormat.format(value);
        mFullName = mDetailsFormat.format(value);
        mExpandedName = mExpandedFormat.format(value);
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
