package com.dlutskov.chart_lib.drawers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Paint;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ChartPointsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate, P extends ChartPointsDrawer.DrawingData<Y>>
        extends ChartDataDrawer<X, Y>
        implements BoundsUpdateAnimator.Listener<Y> {

    protected final List<P> drawingDataList = new ArrayList<>();

    private BoundsUpdateAnimator<X, Y> mBoundsAnimHandler;

    private Map<String, ValueAnimator> mPointsAnimators = new HashMap<>();

    /**
     * Common alpha value for all chart points
     */
    protected int mPointsAlpha = 255;

    /**
     * Index of X point which is selected now and all related Y points need to be draw as selected
     */
    protected int mSelectedPointIndex = -1;

    /**
     * Alpha value of selected point. Used for corresponding paint which will draw selected points
     */
    protected int mSelectedPointAlpha;

    private long mAnimDuration;

    protected boolean mAnimateBoundsChanges = true;

    protected ChartPointsDrawer(ChartView chartView) {
        super(chartView);
        mAnimDuration = ChartUtils.DEFAULT_CHART_CHANGES_ANIMATION_DURATION;
    }

    public void setAnimDuration(long animDuration) {
        mAnimDuration = animDuration;
    }

    public long getAnimDuration() {
        return mAnimDuration;
    }

    public void setSelectedPointIndex(int selectedPointIndex) {
        mSelectedPointIndex = selectedPointIndex;
    }

    public void setSelectedPointAlpha(int alpha) {
        mSelectedPointAlpha = alpha;
        mChartView.invalidate();
    }

    public void setPointsAlpha(int alpha) {
        mPointsAlpha = alpha;
        mChartView.invalidate();
    }

    public void setAnimateBoundsChanges(boolean animateBoundsChanges) {
        mAnimateBoundsChanges = animateBoundsChanges;
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Set<String> hiddenChartPoints) {
        super.updateData(data, bounds, hiddenChartPoints);
        drawingDataList.clear();
        if (mBoundsAnimHandler != null) {
            mBoundsAnimHandler.cancel();
        }
    }

    @Override
    public void updateBounds(ChartBounds<X, Y> currentBounds, ChartBounds<X, Y> targetBounds) {
        if (!mAnimateBoundsChanges) {
            super.updateBounds(currentBounds, targetBounds);
            return;
        }
        // Instantly update only x values, y values will be updated by animation
        currentBounds = new ChartBounds<>(currentBounds);
        currentBounds.setMinXIndex(targetBounds.getMinXIndex());
        currentBounds.setMaxXIndex(targetBounds.getMaxXIndex());
        super.updateBounds(currentBounds, currentBounds);

        if (mBoundsAnimHandler != null) {
            if (mBoundsAnimHandler.isTargetTheSame(targetBounds)) {
                // No need to update y bounds animator already running to move to same target
                return;
            } else {
                // Use currently animated bounds
                currentBounds.setMinY(mBoundsAnimHandler.getCurrentYBounds().first);
                currentBounds.setMaxY(mBoundsAnimHandler.getCurrentYBounds().second);
                // Cancel previously started animator
                mBoundsAnimHandler.cancel();
            }
        }
        mBoundsAnimHandler = new BoundsUpdateAnimator<>(currentBounds, targetBounds, this);
        mBoundsAnimHandler.start(mAnimDuration, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBoundsAnimHandler = null;
            }
        });
    }

    @Override
    public void updatePointsVisibility(final String pointsId, boolean visible) {
        final P linesDrawer = findDrawingData(pointsId);
        if (linesDrawer == null) {
            return;
        }

        // Cancel running animator
        ValueAnimator currentAnimator = mPointsAnimators.get(pointsId);
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        currentAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(mAnimDuration);
        currentAnimator.addUpdateListener(new VisibilityAnimatorUpdateListener(linesDrawer, visible));
        if (!visible) {
            currentAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    linesDrawer.setVisible(false);
                }
            });
        }
        linesDrawer.setVisible(true);
        currentAnimator.start();
        mPointsAnimators.put(pointsId, currentAnimator);
    }

    @Override
    public void onBoundsAnimationUpdated(Pair<Y, Y> yBounds, float updateProgress) {
        getBounds().setMinY(yBounds.first);
        getBounds().setMaxY(yBounds.second);
        invalidate();
        mChartView.invalidate();
    }

    protected P findDrawingData(String pointsId) {
        for (P data : drawingDataList) {
            if (data.getId().equals(pointsId)) return data;
        }
        return null;
    }

    protected void onVisibilityAnimatorUpdate(P pointsData, int alpha) {
        pointsData.setAlpha(alpha);
        mChartView.invalidate();
    }

    /**
     * Handles points visibility changes
     */
    class VisibilityAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private final P mPointsData;
        private final int mInitialAlpha;
        private boolean mAppear;

        VisibilityAnimatorUpdateListener(P pointsData, boolean appear) {
            mPointsData = pointsData;
            mInitialAlpha = pointsData.getAlpha();
            mAppear = appear;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float progress = (float) animation.getAnimatedValue();
            int alpha = (int) (mAppear ? mInitialAlpha + (255 - mInitialAlpha) * progress :  mInitialAlpha * (1 - progress));
            onVisibilityAnimatorUpdate(mPointsData, alpha);
        }
    }

    /**
     * Drawing data which is bound to specific {@link ChartPointsData} with same id
     * Holds all required drawing options for bound points data
     */
    protected static class DrawingData<C extends ChartCoordinate> {

        protected ChartPointsData<C> pointsData;

        private final String mId;

        private boolean isVisible = true;

        private int mAlpha = 255;

        protected Paint paint;

        DrawingData(ChartPointsData<C> pointsData) {
            this.pointsData = pointsData;
            mId = pointsData.getId();

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(pointsData.getColor());
        }

        public String getId() {
            return mId;
        }

        public void setVisible(boolean visible) {
            this.isVisible = visible;
        }

        public boolean isVisible() {
            return this.isVisible;
        }

        public Paint getPaint() {
            return paint;
        }

        public int getAlpha() {
            return mAlpha;
        }

        public void setAlpha(int alpha) {
            mAlpha = alpha;
        }
    }
}
