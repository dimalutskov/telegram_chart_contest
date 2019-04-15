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

    private static final int COLOR_BLUE2  = Color.rgb(56, 150, 212);
    private static final int COLOR_GRAY1  = Color.rgb(150, 162, 170);

    private static final int BG_ACTIVITY_DAY = Color.parseColor("#F0F0F0");
    private static final int BG_ACTIVITY_NIGHT = Color.parseColor("#1B2433");

    private static final int BG_CHART_DAY = Color.WHITE;
    private static final int BG_CHART_NIGHT = Color.parseColor("#242F3E");

    private static final int ZOOM_OUT_TEXT_DAY = Color.parseColor("#108BE3");
    private static final int ZOOM_OUT_TEXT_NIGHT = Color.parseColor("#48AAF0");

    private static final int GRID_LINE_DAY = Color.parseColor("#182D3B");
    private static final int GRID_LINE_NIGHT = Color.WHITE;

    private static final int AXIS_LABEL_1_DAY = Color.parseColor("#8E8E93");
    private static final int AXIS_LABEL_2_DAY = Color.parseColor("#252529");
    private static final int AXIS_LABEL_1_NIGHT = Color.parseColor("#A3B1C2"); // TODO
    private static final int AXIS_LABEL_2_NIGHT = Color.parseColor("#ECF2F8");

    private static final int PREVIEW_SCROLL_OVERLAY_DAY = Color.parseColor("#66E2EEF9");
    private static final int PREVIEW_SCROLL_OVERLAY_NIGHT = Color.parseColor("#66304259");

    private static final int PREVIEW_SCROLL_OVERLAY_BORDERS_DAY = Color.parseColor("#8086A9C4");
    private static final int PREVIEW_SCROLL_OVERLAY_BORDERS_NIGHT = Color.parseColor("#806F899E");

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
            case NIGHT: return BG_ACTIVITY_NIGHT;
        }
        return BG_ACTIVITY_DAY;
    }
    
    public static int bgChart(Theme theme) {
        switch (theme) {
            case NIGHT: return BG_CHART_NIGHT;
        }
        return BG_CHART_DAY;
    }
    
    public static int textColorChartLabels(Theme theme) {
        switch (theme) {
            case NIGHT: return AXIS_LABEL_2_NIGHT;
        }
        return AXIS_LABEL_2_DAY;
    }
    
    public static int chartGridColor(Theme theme) {
        switch (theme) {
            case NIGHT: return GRID_LINE_NIGHT;
        }
        return GRID_LINE_DAY;
    }
    
    public static int bgChartPreviewUnselectedArea(Theme theme) {
        switch (theme) {
            case NIGHT: return PREVIEW_SCROLL_OVERLAY_NIGHT;
        }
        return PREVIEW_SCROLL_OVERLAY_DAY;
    }

    public static int chartPreviewBordersColor(Theme theme) {
        switch (theme) {
            case NIGHT: return PREVIEW_SCROLL_OVERLAY_BORDERS_NIGHT;
        }
        return PREVIEW_SCROLL_OVERLAY_BORDERS_DAY;
    }

    public static int bgChartPointsDetails(Theme theme) {
        return bgChart(theme);
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
