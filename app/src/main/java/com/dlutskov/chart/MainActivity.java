package com.dlutskov.chart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dlutskov.chart.data.ChartDataProvider;
import com.dlutskov.chart.view.MoonIconView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.coordinates.DateCoordinate;
import com.dlutskov.chart_lib.data.coordinates.LongCoordinate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Example of using custom created {@link ChartView} for Telegram Contests
 * Activity's layout and all app resources are created programmatically to reduce apk size
 */
public class MainActivity extends Activity {

    /**
     * General padding in DP which is used by most of the activity's views
     */
    public static final int PADDING_GENERAL = 16;

    public static final int APP_MODE_ANIM_DURATION = 250;

    private LinearLayout mRootView;

    private ViewGroup mHeaderLayout;
    private MoonIconView mIconMoon;
    private View mBottomSpace;

    private View mProgress;

    private List<ChartController> mChartsControllers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Root Container
        mRootView = new LinearLayout(this);
        mRootView.setLayoutParams(new ScrollView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        mRootView.setOrientation(LinearLayout.VERTICAL);

        // Header Container
        mHeaderLayout = createHeaderView(this);
        mIconMoon = (MoonIconView) mHeaderLayout.getChildAt(1);
        mIconMoon.setOnClickListener(v -> {
            if (!mProgress.isShown()) {
                AppDesign.switchTheme();
                applyCurrentColors(true);
            }
        });
        mRootView.addView(mHeaderLayout);

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        // Scroll view for charts
        ScrollView chartsScrollView = new ScrollView(this);
        chartsScrollView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        // Container for charts
        LinearLayout chartsContainer = new LinearLayout(this);
        chartsContainer.setLayoutParams(new ScrollView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        chartsContainer.setOrientation(LinearLayout.VERTICAL);
        chartsScrollView.addView(chartsContainer);

        createChartControllers(chartsContainer);
        frameLayout.addView(chartsScrollView);
        /////////////////////

        // Progress view
        mProgress = createProgressView(this);
        frameLayout.addView(mProgress);

        mRootView.addView(frameLayout);
        setContentView(mRootView);

        applyCurrentColors(false);
    }

    private static View createProgressView(Context context) {
        FrameLayout progressView = new FrameLayout(context);
        progressView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        progressView.setClickable(true);

        ProgressBar progressBar = new ProgressBar(context);
        int size = ChartUtils.getPixelForDp(context, 50);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);

        progressView.addView(progressBar);
        progressView.setVisibility(View.GONE);
        return progressView;
    }

    private void createChartControllers(ViewGroup chartsContainer) {
        try {
            // Followers
            ChartLinesData<DateCoordinate, LongCoordinate> linesData = ChartDataProvider.getOverviewChartData(this, "1");
            ChartData chartData = new ChartData(ChartData.CHART_ID_LINES, "Followers", "1", linesData);
            createChartController(chartData, chartsContainer);

            // Interactions
            linesData = ChartDataProvider.getOverviewChartData(this, "2");
            chartData = new ChartData(ChartData.CHART_ID_SCALED_LINES, "Interactions", "2", linesData);
            createChartController(chartData, chartsContainer);

            // Fruits
            linesData = ChartDataProvider.getOverviewChartData(this, "3");
            chartData = new ChartData(ChartData.CHART_ID_STACKED_BARS, "Fruits", "3", linesData);
            createChartController(chartData, chartsContainer);

            // Views
            linesData = ChartDataProvider.getOverviewChartData(this, "4");
            chartData = new ChartData(ChartData.CHART_ID_SINGLE_BAR, "Views", "4", linesData);
            createChartController(chartData, chartsContainer);

            // Fruits
            linesData = ChartDataProvider.getOverviewChartData(this, "5");
            chartData = new ChartData(ChartData.CHART_ID_AREAS,"Fruits","5", linesData);
            createChartController(chartData, chartsContainer);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add some space after the last chart
        mBottomSpace = new View(this);
        mBottomSpace.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, ChartUtils.getPixelForDp(this, PADDING_GENERAL)));
        chartsContainer.addView(mBottomSpace);
    }

    private void createChartController(ChartData chartData, ViewGroup chartsContainer) {
        ChartController controller = new ChartController(this, chartData);
        controller.attachChart(chartsContainer);
        controller.showChart();
        mChartsControllers.add(controller);
    }

    private static ViewGroup createHeaderView(Context ctx) {
        // Container
        LinearLayout containerView = new LinearLayout(ctx);
        int headerHeight = ChartUtils.getPixelForDp(ctx, 56);
        containerView.setOrientation(LinearLayout.HORIZONTAL);
        int padding = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        containerView.setPadding(padding, 0, padding, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, headerHeight);
        containerView.setLayoutParams(params);

        TextView titleView = new TextView(ctx);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT);
        textParams.weight = 1;
        textParams.gravity = Gravity.CENTER_VERTICAL;
        titleView.setLayoutParams(textParams);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        titleView.setTextColor(Color.WHITE);
        titleView.setText("Telegram Chart Contest");
        containerView.addView(titleView);

        MoonIconView moonIconView = new MoonIconView(ctx);
        int moonIconSize = ChartUtils.getPixelForDp(ctx, 24);
        LinearLayout.LayoutParams moonIconParams = new LinearLayout.LayoutParams(moonIconSize, moonIconSize);
        moonIconParams.gravity = Gravity.CENTER_VERTICAL;
        moonIconView.setLayoutParams(moonIconParams);
        containerView.addView(moonIconView);

        return containerView;
    }

    private void applyCurrentColors(boolean animate) {
        int duration = animate ? APP_MODE_ANIM_DURATION : 0;

        AppDesign.Theme prevTheme = AppDesign.getTheme().invertedTheme();
        AppDesign.Theme curTheme = AppDesign.getTheme();

        AppDesign.applyColorWithAnimation(AppDesign.bgActivity(prevTheme),
                AppDesign.bgActivity(curTheme), duration, updatedColor -> {
                    mRootView.setBackgroundColor(updatedColor);
                });

        AppDesign.applyColorWithAnimation(AppDesign.bgHeader(prevTheme),
                AppDesign.bgHeader(curTheme), duration, updatedColor -> {
                    mHeaderLayout.setBackgroundColor(updatedColor);
                    mIconMoon.setBackgroundColor(updatedColor);
                });

        for (ChartController controller : mChartsControllers) {
            controller.applyCurrentColors(curTheme, animate);
        }
    }

    public void setProgressVisibility(int visibility) {
        mProgress.setVisibility(visibility);
    }

}