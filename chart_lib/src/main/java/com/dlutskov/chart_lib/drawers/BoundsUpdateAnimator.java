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
        void onBoundsUpdated(ChartBounds<X, Y> bounds, float updateProgress);
    }

    private final ChartBounds<X, Y> mCurrentBounds;
    private final Pair<Y, Y> mInitialYBounds;
    private final Pair<Y, Y> mTargetYBounds;

    private ValueAnimator mAnimator;

    private final Listener mListener;

    BoundsUpdateAnimator(ChartBounds<X, Y> initialBounds, ChartBounds<X, Y> targetBounds,
                         Listener<X, Y> listener) {
        mCurrentBounds = initialBounds;
        mInitialYBounds = new Pair<>(initialBounds.getMinY(), initialBounds.getMaxY());
        mTargetYBounds = new Pair<>(targetBounds.getMinY(), targetBounds.getMaxY());
        mListener = listener;
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

        Y newMinY = (Y) mInitialYBounds.first.add(mInitialYBounds.first.distanceTo(mTargetYBounds.first).getPart(progress));
        Y newMaxY = (Y) mInitialYBounds.second.add(mInitialYBounds.second.distanceTo(mTargetYBounds.second).getPart(progress));

        mCurrentBounds.setMinY(newMinY);
        mCurrentBounds.setMaxY(newMaxY);

        mListener.onBoundsUpdated(mCurrentBounds, progress);
    }

}
