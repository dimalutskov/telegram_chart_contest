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

import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.drawers.ChartAxisLabelsDrawer;
import com.dlutskov.chart_lib.drawers.ChartPointsDetailsDrawer;
import com.dlutskov.chart_lib.drawers.ChartPointsDrawer;
import com.dlutskov.chart_lib.drawers.ChartXAxisLabelsDrawer;
import com.dlutskov.chart_lib.drawers.ChartYAxisLabelsDrawer;
import com.dlutskov.chart_lib.utils.ChartUtils;

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

    private ChartXAxisLabelsDrawer<X, Y> mXAxisLabelsDrawer;
    private ChartYAxisLabelsDrawer<X, Y> mYAxisLabelsDrawer;
    private ChartPointsDetailsDrawer<X, Y> mPointsDetailsDrawer;

    // Pints details
    private ValueAnimator mPointsDetailsAnimator;
    private int mPointsDetailsXIndex = -1;
    private int mPointsDetailsAlpha;
    private long mPointsDetailsAnimDuration;

    // Expanded points handling
    private ChartPointsDrawer<X, Y, ?> mCollapsedPointsDrawer;
    private ChartLinesData<X, Y> mCollapsedData;
    private ChartBounds<X, Y> mCollapedBounds;

    private ChartPointsDetailsDrawer<X, Y> mDisappearingPointsDetailsDrawer;
    private AnimatorSet mExpandAnimator;

    private boolean isExpanded;

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
    protected void init() {
        super.init();
        mXAxisLabelsDrawer = new ChartXAxisLabelsDrawer<>(this, ChartUtils.getPixelForDp(getContext(), 32));
        mYAxisLabelsDrawer = new ChartYAxisLabelsDrawer<>(this, ChartAxisLabelsDrawer.SIZE_MATCH_PARENT);
        mYAxisLabelsDrawer.setDrawOverPoints(true);
        mPointsDetailsDrawer = new ChartPointsDetailsDrawer<>(this, false);

        mPointsDetailsAnimDuration = 300;

        addDrawer(mYAxisLabelsDrawer);
        addDrawer(mXAxisLabelsDrawer);
        addDrawer(mPointsDetailsDrawer);
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDisappearingPointsDetailsDrawer != null) {
            mDisappearingPointsDetailsDrawer.onAfterDraw(canvas, mDrawingRect);
        }
    }

    @Override
    protected void drawPoints(Canvas canvas, Rect drawingRect) {
        if (mCollapsedPointsDrawer != null) {
            mCollapsedPointsDrawer.draw(canvas, drawingRect);
        }

        super.drawPoints(canvas, drawingRect);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
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
        mExpandAnimator = new AnimatorSet();
        Animator hideAnimator = createExpandedDisappearingAnimator(selectedXIndex, 3000);
        Animator showAnimator = createExpandedAppearanceAnimator(pointsDrawer, expandedData, newMinXIndex, newMaxXIndex, 3000, 1500);
        mExpandAnimator.playTogether(hideAnimator, showAnimator);

        mExpandAnimator.start();
    }

    private Animator createExpandedDisappearingAnimator(int xIndex, int duration) {
        // Set current points drawer as disappearing drawer
        mCollapsedPointsDrawer = getPointsDrawer();
        mDisappearingPointsDetailsDrawer = mPointsDetailsDrawer;

        // Keep references on previous data
        mCollapsedData = mLinesData;
        mCollapedBounds = new ChartBounds<>(mBounds);

        ChartBounds<X, Y> prevDisappearingBounds = new ChartBounds<>(mBounds);
        ChartBounds<X, Y> disappearingBounds = new ChartBounds<>(mBounds);

        int minRequiredIndex = xIndex == 0 ? 0 : xIndex - 1;
        int maxRequiredIndex = xIndex == mLinesData.getXPoints().getPoints().size() - 1 ? xIndex : xIndex + 1;

        ValueAnimator hideAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(duration);
        hideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();

                int minXIndex = (int) (mCollapedBounds.getMinXIndex() + (minRequiredIndex - mCollapedBounds.getMinXIndex()) * progress);
                int maxXIndex = (int) (mCollapedBounds.getMaxXIndex() - (mCollapedBounds.getMaxXIndex() - maxRequiredIndex) * progress);

                int alpha = (int) (255 * (1 - progress));

                // Hide points details
                mCollapsedPointsDrawer.setSelectedPointAlpha(alpha);
                mDisappearingPointsDetailsDrawer.setAlpha(alpha);

                // Hide disappearing drawer
                mCollapsedPointsDrawer.setPointsAlpha(alpha);

                // Update horizontal bounds
                calculateCurrentBounds(mCollapsedData, minXIndex, maxXIndex, disappearingBounds);
                mCollapsedPointsDrawer.updateBounds(prevDisappearingBounds, disappearingBounds);
                prevDisappearingBounds.update(disappearingBounds);
            }
        });
        hideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                removeDrawer(mDisappearingPointsDetailsDrawer);
                mDisappearingPointsDetailsDrawer = null;
            }
        });
        return hideAnimator;
    }

    private Animator createExpandedAppearanceAnimator(ChartPointsDrawer<X, Y, ?> pointsDrawer, ChartLinesData<X, Y> expandedData,
                                                      int newMinXIndex, int newMaxXIndex, int duration, int startDelay) {
        ValueAnimator showAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(duration);
        showAnimator.setStartDelay(startDelay);
        showAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                int alpha = (int) (255 * progress);
                mPointsDrawer.setPointsAlpha(alpha);
            }
        });
        showAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Replace current points drawer to new one
                setPointsDrawer(pointsDrawer);
                setPointsDetailsDrawer(new ChartPointsDetailsDrawer<>(ChartFullView.this, true));
                updateChartData(expandedData, newMinXIndex, newMaxXIndex, true);

                // Reset selected points index
                mPointsDetailsXIndex = -1;
                mPointsDetailsAlpha = 0;

                mXAxisLabelsDrawer.setExpandedPoints(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isExpanded = true;
            }
        });
        return showAnimator;
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
                if (mListener != null) {
                    mHidePointsOnTouchUp = false;
                    removeCallbacks(mHidePointsDetailsTask); // It'll be hidden on animation
                    mListener.onExpandChartClicked(ChartFullView.this, mPointsDetailsXIndex);
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
