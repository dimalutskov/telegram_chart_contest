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
import com.dlutskov.chart.view.ChartCheckBoxesContainer;
import com.dlutskov.chart.view.MoonIconView;
import com.dlutskov.chart_lib.ChartFullView;
import com.dlutskov.chart_lib.ChartPreviewView;
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

    private View mProgress;

    private List<ChartController> mChartsControllers = new ArrayList<>();

    private AppDesign.Theme mTheme = AppDesign.Theme.DAY;

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
            mTheme = mTheme.invertedTheme();
            applyCurrentColors(true);
        });
        mRootView.addView(mHeaderLayout);

        ////////////////////
        List<ChartLinesData<DateCoordinate, LongCoordinate>> chartDataList = null;
        try {
            chartDataList = ChartDataProvider.readChartData(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        createChartControllers(chartDataList, chartsContainer);
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

    private void createChartControllers(List<ChartLinesData<DateCoordinate, LongCoordinate>> chartDataList, ViewGroup chartsContainer) {
        int margin = ChartUtils.getPixelForDp(this, PADDING_GENERAL * 2);

        View spaceView = new View(this);
        spaceView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, margin / 2));
        chartsContainer.addView(spaceView);

        for (int i = 0; i < chartDataList.size(); i++) {
            ChartLinesData<DateCoordinate, LongCoordinate> chartData = chartDataList.get(i);
            // Chart Full View
            ChartFullView<DateCoordinate, LongCoordinate> chartView = createChartFullView(this);
            chartsContainer.addView(chartView);
            // Chart Preview
            ChartPreviewView<DateCoordinate, LongCoordinate> chartPreview = createChartPreviewView(this);
            chartsContainer.addView(chartPreview);
            // Chart CheckBoxes
            ChartCheckBoxesContainer checkboxesContainer = createCheckboxesContainer(this);
            chartsContainer.addView(checkboxesContainer);

            spaceView = new View(this);
            spaceView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, margin));
            chartsContainer.addView(spaceView);

            ChartController controller = new ChartController(this, chartData, String.valueOf(i + 1),
                    chartView, chartPreview, checkboxesContainer);
            controller.showChart();
            mChartsControllers.add(controller);
        }
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

    private static ChartFullView<DateCoordinate, LongCoordinate> createChartFullView(Context ctx) {
        ChartFullView<DateCoordinate, LongCoordinate> view = new ChartFullView<>(ctx);
        int height = ChartUtils.getPixelForDp(ctx, 220);
        int margin = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, height);
        params.leftMargin = margin;
        params.rightMargin = margin;
        view.setLayoutParams(params);
        // Styling
        view.setLabelsTextSize(ChartUtils.getPixelForDp(ctx, 12));
        return view;
    }

    private static ChartPreviewView<DateCoordinate, LongCoordinate> createChartPreviewView(Context ctx) {
        ChartPreviewView<DateCoordinate, LongCoordinate> view = new ChartPreviewView<>(ctx);
        int height = ChartUtils.getPixelForDp(ctx, 40);
        int margin = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, height);
        params.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(params);
        return view;
    }

    private static ChartCheckBoxesContainer createCheckboxesContainer(Context ctx) {
        ChartCheckBoxesContainer view = new ChartCheckBoxesContainer(ctx);
        int margin = ChartUtils.getPixelForDp(ctx, PADDING_GENERAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.topMargin = ChartUtils.getPixelForDp(ctx, 4);
        params.leftMargin = margin;
        params.rightMargin = margin;
        view.setLayoutParams(params);
        return view;
    }

    private void applyCurrentColors(boolean animate) {
        int duration = animate ? APP_MODE_ANIM_DURATION : 0;

        AppDesign.Theme prevTheme = mTheme.invertedTheme();
        AppDesign.Theme curTheme = mTheme;

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
            controller.applyCurrentColors(mTheme, animate);
        }
    }

    public void setProgressVisibility(int visibility) {
        mProgress.setVisibility(visibility);
    }

}