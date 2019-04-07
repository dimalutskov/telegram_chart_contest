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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ChartPointsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate, P extends ChartPointsDrawer.DrawingData<Y>>
        extends ChartDataDrawer<X, Y>
        implements BoundsUpdateAnimator.Listener<X, Y> {

    protected final List<P> drawingData = new ArrayList<>();

    private BoundsUpdateAnimator<X, Y> mBoundsAnimHandler;

    private Map<String, ValueAnimator> mPointsAnimators = new HashMap<>();

    private long mAnimDuration;

    protected ChartPointsDrawer(ChartView chartView) {
        super(chartView);
        mAnimDuration = ChartUtils.DEFAULT_CHART_CHANGES_ANIMATION_DURATION;
    }

    public void setAnimDuration(long animDuration) {
        mAnimDuration = animDuration;
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds) {
        super.updateData(data, bounds);
        drawingData.clear();
        if (mBoundsAnimHandler != null) {
            mBoundsAnimHandler.cancel();
        }
    }

    @Override
    public void updateBounds(ChartBounds<X, Y> currentBounds, ChartBounds<X, Y> targetBounds) {
        // Instantly update only x values, y values will be updated by animation
        currentBounds = new ChartBounds<>(currentBounds);
        currentBounds.setMinXIndex(targetBounds.getMinXIndex());
        currentBounds.setMaxXIndex(targetBounds.getMaxXIndex());
        super.updateBounds(currentBounds, currentBounds);

        if (mBoundsAnimHandler != null) {
            mBoundsAnimHandler.updateXBounds(targetBounds.getMinXIndex(), targetBounds.getMaxXIndex());
            if (mBoundsAnimHandler.isTargetTheSame(targetBounds)) {
                // No need to update y bounds animator already running to move to same target
                return;
            } else {
                // Use currently animated bounds
                currentBounds.setMinY(mBoundsAnimHandler.getCurrentBounds().getMinY());
                currentBounds.setMaxY(mBoundsAnimHandler.getCurrentBounds().getMaxY());
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
        currentAnimator.addUpdateListener(new LinesAnimatorUpdateListener(linesDrawer, visible));
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
    public void onBoundsAnimationUpdated(ChartBounds<X, Y> bounds, float updateProgress) {
        super.updateBounds(bounds, bounds);
    }

    protected P findDrawingData(String pointsId) {
        for (P data : drawingData) {
            if (data.getId().equals(pointsId)) return data;
        }
        return null;
    }

    class LinesAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private final P mPointsData;
        private final int mInitialAlpha;
        private boolean mAppear;

        LinesAnimatorUpdateListener(P pointsData, boolean appear) {
            mPointsData = pointsData;
            mInitialAlpha = pointsData.getPaint().getAlpha();
            mAppear = appear;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float progress = (float) animation.getAnimatedValue();
            int alpha = (int) (mAppear ? mInitialAlpha + (255 - mInitialAlpha) * progress :  mInitialAlpha * (1 - progress));
            mPointsData.getPaint().setAlpha(alpha);
            mChartView.invalidate();
        }
    }

    protected static class DrawingData<C extends ChartCoordinate> {

        private final String mId;

        private boolean isVisible = true;

        protected Paint paint;

        DrawingData(ChartPointsData<C> pointsData) {
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
    }
}
