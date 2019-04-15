package com.dlutskov.chart_lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.drawers.ChartAxisLabelsDrawer;
import com.dlutskov.chart_lib.drawers.ChartPercentagesAreasDrawer;
import com.dlutskov.chart_lib.drawers.ChartPieDrawer;
import com.dlutskov.chart_lib.drawers.ChartPointsDetailsDrawer;
import com.dlutskov.chart_lib.drawers.ChartPointsDrawer;
import com.dlutskov.chart_lib.drawers.ChartXAxisLabelsDrawer;
import com.dlutskov.chart_lib.drawers.ChartYAxisLabelsDrawer;
import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.utils.Pair;

/**
 * Extends {@link ChartView} by adding own {@link ChartXAxisLabelsDrawer} and {@link ChartYAxisLabelsDrawer}
 * for drawing labels on x and axis and {@link ChartPointsDetailsDrawer} for drawing window with details of selected
 * points on chart touch. All the drawers properties can be changed by calling appropriate get methods
 * and replaced by calling set method for required drawer
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of y axis chart coordinates
 */
public class ChartFullView<X extends ChartCoordinate, Y extends ChartCoordinate>  extends ChartView<X, Y> {

    public interface Listener<X extends ChartCoordinate, Y extends ChartCoordinate> {
        void onExpandChartClicked(ChartFullView<X, Y> view, int pointsIndex);
    }

    public static final long POINTS_DETAILS_DISAPPEARING_DELAY = 3000;

    private static final int AREA_CHART_CLIP_DURATION = 300;
    private static final int AREA_CHART_ROTATE_DURATION = 500;
    private static final int PIE_CHART_APPEAR_DELAY = 300;
    private static final int PIE_CHART_APPEAR_DURATION = 500;
    private static final int PIE_CHART_OVERSHOT_BACK_DURATION = 800;
    private static final int PIE_CHART_OVERSHOT_ANGLE = 30;

    private ChartXAxisLabelsDrawer<X, Y> mXAxisLabelsDrawer;
    private ChartYAxisLabelsDrawer<X, Y> mYAxisLabelsDrawer;
    private ChartPointsDetailsDrawer<X, Y> mPointsDetailsDrawer;

    // Pints details
    private ValueAnimator mPointsDetailsAnimator;
    private int mPointsDetailsXIndex = -1;
    private int mPointsDetailsAlpha;
    private long mPointsDetailsAnimDuration;

    // Expanded points handling
    private AnimatorSet mExpandCollapseAnimator;

    private boolean isExpanded;

    ////////////////////////////

    private GestureDetectorListener mGestureHandler = new GestureDetectorListener();

    private Listener<X, Y> mListener;

    private Runnable mHidePointsDetailsTask = () -> {
        if (mPointsDetailsAlpha != 0) {
            startPointsDetailsAnimator(false);
        }
    };

    public ChartFullView(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        super.init();
        mXAxisLabelsDrawer = new ChartXAxisLabelsDrawer<>(this, ChartUtils.getPixelForDp(getContext(), 28));
        mYAxisLabelsDrawer = new ChartYAxisLabelsDrawer<>(this, ChartAxisLabelsDrawer.SIZE_MATCH_PARENT);
        mYAxisLabelsDrawer.setDrawOverPoints(true);
        mPointsDetailsDrawer = new ChartPointsDetailsDrawer<>(this, false);

        mPointsDetailsAnimDuration = 300;

        addDrawer(mYAxisLabelsDrawer);
        addDrawer(mXAxisLabelsDrawer);
        addDrawer(mPointsDetailsDrawer);
    }

    @Override
    public void updateHorizontalBounds(int minXIndex, int maxXIndex) {
        super.updateHorizontalBounds(minXIndex, maxXIndex);
        if (mPointsDetailsXIndex > 0 && (mPointsDetailsXIndex < minXIndex || mPointsDetailsXIndex > maxXIndex)) {
            removeCallbacks(mHidePointsDetailsTask);
            mPointsDetailsAlpha = 0;
            mPointsDrawer.setSelectedPointAlpha(mPointsDetailsAlpha);
            mPointsDetailsDrawer.setAlpha(mPointsDetailsAlpha);
            mPointsDetailsDrawer.setShown(false);
        }
    }

    @Override
    protected void updatePointsDrawingRect(Rect rect) {
        super.updatePointsDrawingRect(rect);

        int xLabelsHeight = mXAxisLabelsDrawer.getSize();
        int yLabelsWidth = mYAxisLabelsDrawer.getSize();
        if (!mXAxisLabelsDrawer.isDrawOverPoints() && xLabelsHeight > 0) {
            rect.bottom -= xLabelsHeight;
        }
        if (!mYAxisLabelsDrawer.isDrawOverPoints() && yLabelsWidth > 0) {
            rect.left += yLabelsWidth;
        }
    }

    @Override
    public void updatePointsVisibility(String pointsId, boolean visible) {
        super.updatePointsVisibility(pointsId, visible);
        if (mLinesData.getYPoints().size() == mHiddenChartLines.size()) {
            // Hide points details if there are no visible points
            instantlyHidePointsDetails();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isDataAnimatorRunning() || mHiddenChartLines.size() == mLinesData.getYPoints().size()) {
            return super.onTouchEvent(ev);
        }

        if (mPointsDrawer instanceof ChartPieDrawer) {
            // Not implemented
            return super.onTouchEvent(ev);
        }

        return mGestureHandler.onTouchEvent(ev);
    }

    private void disableParentTouch() {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private void showPointsDetails(int xIndex) {
        removeCallbacks(mHidePointsDetailsTask);

        mPointsDetailsXIndex = xIndex;
        mPointsDrawer.setSelectedPointIndex(xIndex);
        mPointsDetailsDrawer.setSelectedPointIndex(xIndex);
        if (mPointsDetailsAlpha > 0 || (mPointsDetailsAnimator != null && mPointsDetailsAnimator.isRunning())) {
            invalidate();
        } else {
            startPointsDetailsAnimator(true);
        }
    }

    public void hidePointsDetails(long delay) {
        removeCallbacks(mHidePointsDetailsTask);

        if (delay == 0) {
            mHidePointsDetailsTask.run();
        } else {
            postDelayed(mHidePointsDetailsTask, delay);
        }
    }

    private void instantlyHidePointsDetails() {
        mPointsDetailsAlpha = 0;
        mPointsDetailsDrawer.setShown(false);
        removeCallbacks(mHidePointsDetailsTask);
        mPointsDrawer.setSelectedPointAlpha(0);
    }

    private void startPointsDetailsAnimator(boolean appear) {
        if (mPointsDetailsAnimator != null) {
            mPointsDetailsAnimator.cancel();
        }
        mPointsDetailsAnimator = ValueAnimator.ofInt(mPointsDetailsAlpha, appear ? 255 : 0).setDuration(mPointsDetailsAnimDuration);
        mPointsDetailsAnimator.addUpdateListener(animation -> {
            mPointsDetailsAlpha = (int) animation.getAnimatedValue();
            mPointsDrawer.setSelectedPointAlpha(mPointsDetailsAlpha);
            mPointsDetailsDrawer.setAlpha(mPointsDetailsAlpha);
            invalidate();
        });
        if (!appear) {
            mPointsDetailsAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mPointsDetailsDrawer.setShown(false);
                }
            });
        }
        mPointsDetailsDrawer.setShown(true);
        mPointsDetailsAnimator.start();
    }

    public void expand(ChartPointsDrawer<X, Y, ?> pointsDrawer, ChartLinesData<X, Y> expandedData, int selectedXIndex,
                       int newMinXIndex, int newMaxXIndex) {
        isExpanded = true;

        instantlyHidePointsDetails();

        mExpandCollapseAnimator = new AnimatorSet();

        Animator hideAnimator = mLinesData.isPercentage()
                ? getExpandHidePercentageAnimator()
                : getExpandHideAnimator(selectedXIndex);

        Animator showAnimator = mLinesData.isPercentage()
                ? getExpandShowPercentageAnimator(expandedData, newMinXIndex, newMaxXIndex, (ChartPieDrawer<X, Y>) pointsDrawer, true)
                : getShowDataAnimator(expandedData, newMinXIndex, newMaxXIndex, pointsDrawer, true);

        mXAxisLabelsDrawer.setExpandedPoints(true);

        mExpandCollapseAnimator.playTogether(hideAnimator, showAnimator);
        mExpandCollapseAnimator.start();
    }

    public void collapse(ChartPointsDrawer<X, Y, ?> pointsDrawer, ChartLinesData<X, Y> collapsedData,
                         ChartBounds<X, Y> collapsedBounds, boolean keepHiddenCharts) {
        isExpanded = false;

        instantlyHidePointsDetails();

        mExpandCollapseAnimator = new AnimatorSet();
        mPointsDrawer.setAnimateBoundsChanges(false);

        Animator hideAnimator = collapsedData.isPercentage()
                ? getCollapseHidePercentageAnimator()
                : getHideDataAnimator();

        Animator showAnimator = collapsedData.isPercentage()
                ? getCollapseShowPercentageAnimator(collapsedData, collapsedBounds, (ChartPercentagesAreasDrawer<X, Y>) pointsDrawer, keepHiddenCharts)
                : getCollapseShowAnimator(pointsDrawer, collapsedData, collapsedBounds, keepHiddenCharts);

        mXAxisLabelsDrawer.setExpandedPoints(false);

        mExpandCollapseAnimator.playTogether(hideAnimator, showAnimator);
        mExpandCollapseAnimator.start();
    }

    private Animator getExpandHideAnimator(int xIndex) {
        // Set current points drawer as disappearing drawer
        mDisappearingPointsDrawer = getPointsDrawer();

        // Keep references on previous data
        ChartLinesData<X, Y> collapsedData = mLinesData;

        ChartBounds<X, Y> collapsedBounds = new ChartBounds<>(mBounds);
        ChartBounds<X, Y> prevDisappearingBounds = new ChartBounds<>(mBounds);
        ChartBounds<X, Y> disappearingBounds = new ChartBounds<>(mBounds);

        int minRequiredIndex = xIndex == 0 ? 0 : xIndex - 1;
        int maxRequiredIndex = xIndex == mLinesData.getXPoints().getPoints().size() - 1 ? xIndex : xIndex + 1;

        ValueAnimator hideAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(mDataDisappearAnimationDuration);
        hideAnimator.setInterpolator(new DecelerateInterpolator());
        hideAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();

            int minXIndex = (int) (collapsedBounds.getMinXIndex() + (minRequiredIndex - collapsedBounds.getMinXIndex()) * progress);
            int maxXIndex = (int) (collapsedBounds.getMaxXIndex() - (collapsedBounds.getMaxXIndex() - maxRequiredIndex) * progress);

            int alpha = (int) (255 * (1 - progress));

            // Hide points details
            // Let it disappear 2 times faster
            mDisappearingPointsDrawer.setSelectedPointAlpha((int) (255 * (1 - Math.min(1, progress * 2))));

            // Hide disappearing drawer
            mDisappearingPointsDrawer.setPointsAlpha(alpha);

            // Update horizontal bounds
            calculateCurrentBounds(collapsedData, minXIndex, maxXIndex, disappearingBounds);
            mDisappearingPointsDrawer.updateBounds(prevDisappearingBounds, disappearingBounds);
            prevDisappearingBounds.update(disappearingBounds);
            invalidate();
        });
        hideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mDisappearingPointsDrawer = null;
            }
        });
        return hideAnimator;
    }

    private Animator getCollapseShowAnimator(ChartPointsDrawer<X, Y, ?> pointsDrawer, ChartLinesData<X, Y> collapsedData,
                                             ChartBounds<X, Y> collapsedBounds, boolean keepHiddenCharts) {
        // Keep references on previous data
        int xPosition = (collapsedBounds.getMaxXIndex() - collapsedBounds.getMinXIndex()) / 2;
        Pair<Y, Y> yBounds = new Pair<>(null, null);
        collapsedData.calculateYBounds(xPosition - 1, xPosition + 1, mHiddenChartLines, yBounds);

        ChartBounds<X, Y> initialBounds = new ChartBounds<>(xPosition - 1, xPosition + 1, yBounds.first, yBounds.second);
        ChartBounds<X, Y> prevAppearingBounds = new ChartBounds<>(initialBounds);
        ChartBounds<X, Y> appearingBounds = new ChartBounds<>(initialBounds);

        int minRequiredIndex = collapsedBounds.getMinXIndex();
        int maxRequiredIndex = collapsedBounds.getMaxXIndex();

        boolean[] isStarted = new boolean[1];
        isStarted[0] = false;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(mDataAppearAnimationDuration);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(animation -> {
            // HOTFIX For some devices onUpdate called before onStart
            if (!isStarted[0]) return;

            float progress = (float) animation.getAnimatedValue();

            int minXIndex = (int) (initialBounds.getMinXIndex() + (minRequiredIndex - initialBounds.getMinXIndex()) * progress);
            int maxXIndex = (int) (initialBounds.getMaxXIndex() - (initialBounds.getMaxXIndex() - maxRequiredIndex) * progress);

            // Hide disappearing drawer
            pointsDrawer.setPointsAlpha((int) (255 * progress));

            // Update horizontal bounds
            calculateCurrentBounds(collapsedData, minXIndex, maxXIndex, appearingBounds);
            pointsDrawer.updateBounds(prevAppearingBounds, appearingBounds);
            prevAppearingBounds.update(appearingBounds);
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isStarted[0] = true;
                // Replace current points drawer to new one
                setPointsDrawer(pointsDrawer);
                updateChartData(collapsedData, collapsedBounds.getMinXIndex(), collapsedBounds.getMaxXIndex(), keepHiddenCharts);
                pointsDrawer.setAnimateBoundsChanges(false);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                pointsDrawer.setAnimateBoundsChanges(true);
            }
        });
        return animator;
    }

    @Override
    protected void onShowDataAnimatorStarted(ChartLinesData<X, Y> chartData, int minXIndex, int maxXindex,
                                             ChartPointsDrawer<X, Y, ?> newPointsDrawer, boolean keepHiddenChartLines) {
        // Set new point details drawer
        ChartPointsDetailsDrawer detailsDrawer = new ChartPointsDetailsDrawer<>(ChartFullView.this, true);
        detailsDrawer.setBackgroundColor(mPointsDetailsDrawer.getBackgroundColor());
        detailsDrawer.setBackgroundBorderColor(mPointsDetailsDrawer.getBackgroundBorderColor());
        detailsDrawer.setXLabelColor(mPointsDetailsDrawer.getXLabelColor());
        setPointsDetailsDrawer(detailsDrawer);

        super.onShowDataAnimatorStarted(chartData, minXIndex, maxXindex, newPointsDrawer, keepHiddenChartLines);

        // Reset selected points index
        mPointsDetailsXIndex = -1;
        mPointsDetailsAlpha = 0;
    }

    ////////////////////// PERCENTAGES CHART ANIMATORS /////////////////

    private Animator getExpandHidePercentageAnimator() {
        AnimatorSet result = new AnimatorSet();

        // Set current points drawer as disappearing drawer
        mDisappearingPointsDrawer = getPointsDrawer();
        ChartPercentagesAreasDrawer<X, Y> disappearingPointsDrawer = (ChartPercentagesAreasDrawer<X, Y>) mDisappearingPointsDrawer;

        // Transform drawer to circle
        ValueAnimator clipAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(AREA_CHART_CLIP_DURATION);
        clipAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            disappearingPointsDrawer.setClipValue(progress);

            int alpha = (int) (255 * (1 - progress));
            // Hide points details
            mDisappearingPointsDrawer.setSelectedPointAlpha(alpha);
            // Hide y labels
            mYAxisLabelsDrawer.setAlpha(alpha);
            mXAxisLabelsDrawer.setAlpha(alpha);
            invalidate();
        });
        clipAnimator.setInterpolator(new AccelerateInterpolator());

        // Rotate and hide circle
        ValueAnimator rotateAndHideAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(AREA_CHART_ROTATE_DURATION);
        rotateAndHideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAndHideAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();

            disappearingPointsDrawer.setRotationAngle((int) (540 * progress));
            invalidate();
        });
        rotateAndHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDisappearingPointsDrawer = null;
            }
        });

        result.playSequentially(clipAnimator, rotateAndHideAnimator);
        return result;
    }

    private Animator getExpandShowPercentageAnimator(ChartLinesData<X, Y> chartData, int minXIndex, int maxXIndex,
                                                     ChartPieDrawer<X, Y> newPointsDrawer, boolean keepHiddenChartLines) {
        AnimatorSet result = new AnimatorSet();
        result.setStartDelay(PIE_CHART_APPEAR_DELAY);

        ValueAnimator showAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(PIE_CHART_APPEAR_DURATION);
        showAnimator.setInterpolator(new AccelerateInterpolator());
        showAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                onShowDataAnimatorStarted(chartData, minXIndex, maxXIndex, newPointsDrawer, keepHiddenChartLines);
            }
        });
        showAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            newPointsDrawer.setPointsAlpha((int) (255 * progress));
            newPointsDrawer.setRotationAngle((int) ((360 + PIE_CHART_OVERSHOT_ANGLE) * progress));
        });


        ValueAnimator overshotReturnAnimator = ValueAnimator.ofInt(360 + PIE_CHART_OVERSHOT_ANGLE, 360);
        overshotReturnAnimator.setDuration(PIE_CHART_OVERSHOT_BACK_DURATION);
        overshotReturnAnimator.addUpdateListener(animation -> {
            newPointsDrawer.setRotationAngle((Integer) animation.getAnimatedValue());
            invalidate();
        });

        result.playSequentially(showAnimator, overshotReturnAnimator);
        return result;
    }

    private Animator getCollapseHidePercentageAnimator() {
        // Set current points drawer as disappearing drawer
        mDisappearingPointsDrawer = getPointsDrawer();
        ChartPieDrawer<X, Y> disappearingPointsDrawer = (ChartPieDrawer<X, Y>) mDisappearingPointsDrawer;

        ValueAnimator hideAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(PIE_CHART_APPEAR_DURATION);
        hideAnimator.setInterpolator(new AccelerateInterpolator());
        hideAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            disappearingPointsDrawer.setRotationAngle((int) (360 * (1 - progress)));
            invalidate();
        });
        hideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mDisappearingPointsDrawer = null;
            }
        });

        return hideAnimator;
    }

    private Animator getCollapseShowPercentageAnimator(ChartLinesData<X, Y> chartData, ChartBounds<X, Y> bounds,
                                                       ChartPercentagesAreasDrawer<X, Y> newPointsDrawer, boolean keepHiddenChartLines) {
        AnimatorSet result = new AnimatorSet();
        result.setStartDelay(PIE_CHART_APPEAR_DELAY);

        // Rotate and show circle
        ValueAnimator rotateAndHideAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(AREA_CHART_ROTATE_DURATION);
        rotateAndHideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAndHideAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();

            int alpha = (int) (255 * progress);
            newPointsDrawer.setPointsAlpha(alpha);
            newPointsDrawer.setRotationAngle((int) (540 * (1 - progress)));
            invalidate();
        });
        rotateAndHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                onShowDataAnimatorStarted(chartData, bounds.getMinXIndex(), bounds.getMaxXIndex(), newPointsDrawer, keepHiddenChartLines);
                newPointsDrawer.setClipValue(1f);
            }
        });

        // Transform drawer to circle
        ValueAnimator clipAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(AREA_CHART_CLIP_DURATION);
        clipAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            newPointsDrawer.setClipValue(1 - progress);

            int alpha = (int) (255 * progress);
            // Show y labels
            mYAxisLabelsDrawer.setAlpha(alpha);
            mXAxisLabelsDrawer.setAlpha(alpha);
            invalidate();
        });
        clipAnimator.setInterpolator(new AccelerateInterpolator());

        result.playSequentially(rotateAndHideAnimator, clipAnimator);
        return result;
    }

    /////////////////////////////////////////////////////

    public ChartXAxisLabelsDrawer<X, Y> getXLabelsDrawer() {
        return mXAxisLabelsDrawer;
    }

    public void setXLabelsDrawer(ChartXAxisLabelsDrawer<X, Y> xLabelsDrawer) {
        removeDrawer(mXAxisLabelsDrawer);
        mXAxisLabelsDrawer = xLabelsDrawer;
        addDrawer(mXAxisLabelsDrawer);
        invalidate();
    }

    public ChartYAxisLabelsDrawer<X, Y> getYLabelsDrawer() {
        return mYAxisLabelsDrawer;
    }

    public void setYLabelsDrawer(ChartYAxisLabelsDrawer<X, Y> yLabelsDrawer) {
        removeDrawer(mYAxisLabelsDrawer);
        mYAxisLabelsDrawer = yLabelsDrawer;
        addDrawer(mYAxisLabelsDrawer);

        // Hotfix
        removeDrawer(mPointsDetailsDrawer);
        addDrawer(mPointsDetailsDrawer);

        invalidate();
    }

    public ChartPointsDetailsDrawer<X, Y> getPointsDetailsDrawer() {
        return mPointsDetailsDrawer;
    }

    public void setPointsDetailsDrawer(ChartPointsDetailsDrawer<X, Y> pointsDetailsDrawer) {
        removeDrawer(mPointsDetailsDrawer);
        mPointsDetailsDrawer = pointsDetailsDrawer;
        addDrawer(mPointsDetailsDrawer);
        invalidate();
    }

    public void setLabelsTextSize(int textSize) {
        mXAxisLabelsDrawer.setTextSize(textSize);
        mYAxisLabelsDrawer.setTextSize(textSize);
        invalidate();
    }

    public void setLabelsTextColor(int textColor) {
        mXAxisLabelsDrawer.setTextColor(textColor);
        mYAxisLabelsDrawer.setTextColor(textColor);
        invalidate();
    }

    @Override
    public boolean isDataAnimatorRunning() {
        return super.isDataAnimatorRunning() || (mExpandCollapseAnimator != null && mExpandCollapseAnimator.isRunning());
    }

    public void setListener(Listener<X, Y> listener) {
        mListener = listener;
    }

    /**
     * Handles points details showing logic according to touch events
     */
    class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * Default touch slop to detect if we capture something to handles dragging on touch events
         */
        private int mTouchSlop;

        /**
         * X value of last ACTION_DOWN touch event
         */
        private float mDownTouchX;

        private boolean mShowPointsDetailsOnTouch = true;
        private boolean mDetectClickOnPointsDetails;
        private boolean mHidePointsOnTouchUp = true;
        private boolean isTouchIntercepted;

        private final GestureDetector mGestureDetector;

        GestureDetectorListener() {
            mGestureDetector = new GestureDetector(this);

            ViewConfiguration vc = ViewConfiguration.get(getContext());
            mTouchSlop = vc.getScaledTouchSlop();
        }

        boolean onTouchEvent(MotionEvent ev) {
            mGestureDetector.onTouchEvent(ev);

            float x = ev.getX();
            if (x < mDrawingRect.left || x > mDrawingRect.right) {
                return true;
            }

            // Check if point details window opened and there is touch on it - in this case - no need to detect point details touches
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (mPointsDetailsDrawer.isShown() && mPointsDetailsDrawer.isTouchInside(ev.getX(), ev.getY())) {
                    // Skip clicks on the details window - it'll be handled by gesture listener
                    mDetectClickOnPointsDetails = true;
                    mShowPointsDetailsOnTouch = false;
                    return true;
                } else {
                    mDetectClickOnPointsDetails = false;
                }
            }

            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mHidePointsOnTouchUp = true;
                    mDownTouchX = x;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isTouchIntercepted && Math.abs(x - mDownTouchX) > mTouchSlop / 2) {
                        isTouchIntercepted = true;
                        disableParentTouch();
                    }
                    if (mShowPointsDetailsOnTouch && isTouchIntercepted) {
                        int xIndex = mBounds.getMinXIndex() + Math.round((x / mDrawingRect.width()) * mBounds.getXPointsCount());
                        showPointsDetails(xIndex);
                        disableParentTouch();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (mHidePointsOnTouchUp && mPointsDetailsDrawer.isShown()) {
                        hidePointsDetails(POINTS_DETAILS_DISAPPEARING_DELAY);
                    }
                    isTouchIntercepted = false;
                    mShowPointsDetailsOnTouch = true;
                    break;
            }

            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mDetectClickOnPointsDetails && mPointsDetailsDrawer.isShown() && mPointsDetailsDrawer.isTouchInside(e.getX(), e.getY())) {
                if (isExpanded) {
                    hidePointsDetails(0);
                } else {
                    if (mListener != null) {
                        mHidePointsOnTouchUp = false;
                        removeCallbacks(mHidePointsDetailsTask); // It'll be hidden on animation
                        mListener.onExpandChartClicked(ChartFullView.this, mPointsDetailsXIndex);
                    }
                }
            } else {
                if (mPointsDetailsDrawer.isShown()) {
                    hidePointsDetails(0);
                } else {
                    int xIndex = mBounds.getMinXIndex() + Math.round((e.getX() / mDrawingRect.width()) * mBounds.getXPointsCount());
                    showPointsDetails(xIndex);
                }
            }
            return super.onSingleTapUp(e);
        }

    }
}
