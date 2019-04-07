package com.dlutskov.chart_lib;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;

import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

public class ChartUtils {

    public static long DEFAULT_CHART_CHANGES_ANIMATION_DURATION = 250;

    public static int getPixelForDp(Context context, float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    public static float getDpForPixel(Context context, int pixel) {
        return pixel / context.getResources().getDisplayMetrics().density;
    }

    public static float calcXCoordinate(ChartBounds bounds , Rect drawingRect, int xIndex) {
        return drawingRect.left + (xIndex - bounds.getMinXIndex()) / (float) bounds.getXPointsCount() * drawingRect.width();
    }

    public static <C extends ChartCoordinate> float calcYCoordinate(ChartBounds<? extends ChartCoordinate, C> bounds, Rect drawingRect, C yCoordinate) {
        return drawingRect.top + drawingRect.height() - bounds.calcYCoordinateRatio(yCoordinate) * drawingRect.height();
    }

}
