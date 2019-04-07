package com.dlutskov.chart_lib.drawers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Pair;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

/**
 * Handles updates from current ChartBounds to new one by the Animator
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
class BoundsUpdateAnimator<X extends ChartCoordinate, Y extends ChartCoordinate> implements
        ValueAnimator.AnimatorUpdateListener {

    /**
     * Notifies about each ChartBounds changes by the Animator
     */
    interface Listener<X extends ChartCoordinate, Y extends ChartCoordinate> {
        /**
         * @param bounds - newly updated bounds
         * @param updateProgress - animator update progress (value from 0 to 1)
         */
        void onBoundsAnimationUpdated(ChartBounds<X, Y> bounds, float updateProgress);
    }

    private final ChartBounds<X, Y> mCurrentBounds;
    private final Pair<Y, Y> mInitialYBounds;
    private final Pair<Y, Y> mTargetYBounds;

    private ValueAnimator mAnimator;

    private final Listener mListener;

    // Buffer values which used for calculations to not create new instances each time
    private Y minBuf;
    private Y maxBuf;

    BoundsUpdateAnimator(ChartBounds<X, Y> initialBounds, ChartBounds<X, Y> targetBounds,
                         Listener<X, Y> listener) {
        mCurrentBounds = initialBounds;
        mInitialYBounds = new Pair<>(initialBounds.getMinY(), initialBounds.getMaxY());
        mTargetYBounds = new Pair<>(targetBounds.getMinY(), targetBounds.getMaxY());
        mListener = listener;
        minBuf = (Y) initialBounds.getMinY().clone();
        maxBuf = (Y) initialBounds.getMaxY().clone();
    }

    void start(long duration, Animator.AnimatorListener animatorListener) {
        cancel();
        mAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(duration);
        mAnimator.addUpdateListener(this);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimator = null;
            }
        });
        mAnimator.addListener(animatorListener);
        mAnimator.start();
    }

    void cancel() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    ChartBounds<X, Y> getCurrentBounds() {
        return mCurrentBounds;
    }

    void updateXBounds(int minXIndex, int maxXIndex) {
        mCurrentBounds.setMinXIndex(minXIndex);
        mCurrentBounds.setMaxXIndex(maxXIndex);
    }

    /**
     * @return true when target Y values are the same with Y values of specified bounds param
     */
    boolean isTargetTheSame(ChartBounds<X, Y> bounds) {
        return bounds.getMinY().compareTo(mTargetYBounds.first) == 0
                && bounds.getMaxY().compareTo(mTargetYBounds.second) == 0;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float progress = (float) animation.getAnimatedValue();

        mInitialYBounds.first.distanceTo(mTargetYBounds.first, minBuf);
        minBuf.getPart(progress, minBuf);
        mInitialYBounds.first.add(minBuf, minBuf);

        mInitialYBounds.second.distanceTo(mTargetYBounds.second, maxBuf);
        maxBuf.getPart(progress, maxBuf);
        mInitialYBounds.second.add(maxBuf, maxBuf);

        mCurrentBounds.setMinY(minBuf);
        mCurrentBounds.setMaxY(maxBuf);

        mListener.onBoundsAnimationUpdated(mCurrentBounds, progress);
    }

}
