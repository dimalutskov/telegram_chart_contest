package com.dlutskov.chart;


import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;

/**
 * Contains all app colors related to each app theme (Day or Night)
 * Consider subscription on app mode changes instead of setting up all the colors in the {@link MainActivity}
 */
public class AppDesign {

    private static final int COLOR_BLUE1  = Color.rgb(81, 125, 162);
    private static final int COLOR_BLUE2  = Color.rgb(56, 150, 212);
    private static final int COLOR_GRAY1  = Color.rgb(150, 162, 170);
    private static final int COLOR_GRAY2  = Color.rgb(241, 241, 242);
    private static final int COLOR_GRAY3  = Color.rgb(245, 248, 249);

    private static final int COLOR_DARK_1 = Color.rgb(21, 30, 39);
    private static final int COLOR_DARK_2 = Color.rgb(33, 45, 59);
    private static final int COLOR_DARK_3 = Color.rgb(29, 39, 51);
    private static final int COLOR_DARK_4 = Color.rgb(16, 25, 36);

    private static final int COLOR_DARK_1_50 = Color.argb(130, 21, 30, 39);
    private static final int COLOR_GRAY3_80  = Color.argb(200, 245, 248, 249);
    private static final int COLOR_BLUE1_20   = Color.argb(100,81, 125, 162);

    private static final int ZOOM_OUT_TEXT_DAY = Color.parseColor("#108BE3");
    private static final int ZOOM_OUT_TEXT_NIGHT = Color.parseColor("#48AAF0");

    public enum Theme {
        DAY,
        NIGHT;

        public Theme invertedTheme() {
            switch (this) {
                case DAY: return NIGHT;
                case NIGHT: return DAY;
            }
            return DAY;
        }
    }

    public interface ColorUpdatePredicate {
        void onColorUpdated(int updatedColor);
    }

    private static AppDesign.Theme sTheme = AppDesign.Theme.DAY;

    public static AppDesign.Theme getTheme() {
        return sTheme;
    }

    public static void switchTheme() {
        sTheme = sTheme.invertedTheme();
    }
    
    public static int bgActivity(Theme theme) {
        switch (theme) {
            case NIGHT: return COLOR_DARK_3;
        }
        return Color.WHITE;
    }
    
    public static int bgHeader(Theme theme) {
        switch (theme) {
            case NIGHT: return COLOR_DARK_2;
        }
        return COLOR_BLUE1;
    }
    
    public static int textColorChartLabels(Theme theme) {
        return COLOR_GRAY1;
    }
    
    public static int chartGridColor(Theme theme) {
        switch (theme) {
            case NIGHT: return COLOR_DARK_4;
        }
        return COLOR_GRAY2;
    }
    
    public static int bgChartPreview(Theme theme) {
        switch (theme) {
            case NIGHT: return COLOR_DARK_1;
        }
        return COLOR_GRAY3;
    }
    
    public static int bgChartPreviewUnselectedArea(Theme theme) {
        switch (theme) {
            case NIGHT: return COLOR_DARK_1_50;
        }
        return COLOR_GRAY3_80;
    }

    public static int bgChartPreviewSelectedArea(Theme theme) {
        switch (theme) {
            case NIGHT: return COLOR_DARK_3;
        }
        return Color.WHITE;
    }
    
    public static int chartPreviewBordersColor(Theme theme) {
        return COLOR_BLUE1_20;
    }

    public static int bgChartPointsDetails(Theme theme) {
        switch (theme) {
            case NIGHT: return COLOR_DARK_2;
        }
        return Color.WHITE;
    }

    public static int chartPointsDetailsXLabel(Theme theme) {
        switch (theme) {
            case NIGHT: return Color.WHITE;
        }
        return Color.BLACK;
    }

    public static int getZoomOutText(Theme theme) {
        switch (theme) {
            case NIGHT: return ZOOM_OUT_TEXT_NIGHT;
        }
        return ZOOM_OUT_TEXT_DAY;
    }

    public static int getChartHeaderText(Theme theme) {
        switch (theme) {
            case NIGHT: return Color.WHITE;
        }
        return Color.BLACK;
    }

    public static int textCheckBox(Theme theme) {
        return Color.WHITE;
    }

    public static void applyColorWithAnimation(int colorFrom, int colorTo, int duration, ColorUpdatePredicate predicate) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(duration);
        colorAnimation.addUpdateListener(animator -> predicate.onColorUpdated((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    /**
     * Creates colors selector for selected and default tab state
     */
    public static ColorStateList getTabTextColor() {
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_selected},
                new int[] {android.R.attr.state_enabled}
        };
        int[] colors = new int[] {
                COLOR_BLUE2,
                COLOR_GRAY1
        };
        return new ColorStateList(states, colors);
    }

}
