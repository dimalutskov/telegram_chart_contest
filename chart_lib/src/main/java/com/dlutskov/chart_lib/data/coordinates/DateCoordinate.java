package com.dlutskov.chart_lib.data.coordinates;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Extends LongCoordinate with custom name displaying
 */
public class DateCoordinate extends LongCoordinate {

    private static final String DEFAULT_AXIS_DATE_FORMAT = "dd MMM";
    private static final String DEFAULT_FULL_DATE_FORMAT = "E, MMM dd";

    DateCoordinate(long value) {
        this(value, new SimpleDateFormat(DEFAULT_AXIS_DATE_FORMAT, Locale.getDefault()),
                new SimpleDateFormat(DEFAULT_FULL_DATE_FORMAT, Locale.getDefault()));
    }

    DateCoordinate(long value, SimpleDateFormat axisDateFormat, SimpleDateFormat fullDateFormat) {
        super(value);
        mAxisName = axisDateFormat.format(value);
        mFullName = fullDateFormat.format(value);
    }

    @Override
    public DateCoordinate clone() {
        return new DateCoordinate(mValue);
    }

    public static DateCoordinate valueOf(long value) {
        return new DateCoordinate(value);
    }
}
