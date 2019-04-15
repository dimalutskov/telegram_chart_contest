package com.dlutskov.chart_lib.drawers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.Pair;

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
    interface Listener<Y extends ChartCoordinate> {
        /**
         * @param yBounds - newly updated Y bounds
         * @param updateProgress - animator update progress (value from 0 to 1)
         */
        void onBoundsAnimationUpdated(Pair<Y, Y> yBounds, float updateProgress);
    }

    private final Pair<Y, Y> mInitialYBounds;
    private final Pair<Y, Y> mTargetYBounds;
    private final Pair<Y, Y> mCurrentYBounds;

    private ValueAnimator mAnimator;

    private final Listener mListener;

    private long mStartTime;

    BoundsUpdateAnimator(ChartBounds<X, Y> initialBounds, ChartBounds<X, Y> targetBounds,
                         Listener<Y> listener) {
        mInitialYBounds = new Pair<>(initialBounds.getMinY(), initialBounds.getMaxY());
        mTargetYBounds = new Pair<>(targetBounds.getMinY(), targetBounds.getMaxY());
        mCurrentYBounds = new Pair<>((Y)initialBounds.getMinY().clone(), (Y)initialBounds.getMaxY().clone());
        mListener = listener;
    }

    void start(long duration, Animator.AnimatorListener animatorListener) {
        mStartTime = System.currentTimeMillis();

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

    Pair<Y, Y> getCurrentYBounds() {
        return mCurrentYBounds;
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
        update(progress);
    }

    public void update(float progress) {
        progress = Math.min(progress, 1f);
        mInitialYBounds.first.distanceTo(mTargetYBounds.first, mCurrentYBounds.first);
        mCurrentYBounds.first.getPart(progress, mCurrentYBounds.first);
        mInitialYBounds.first.add(mCurrentYBounds.first, mCurrentYBounds.first);

        mInitialYBounds.second.distanceTo(mTargetYBounds.second, mCurrentYBounds.second);
        mCurrentYBounds.second.getPart(progress, mCurrentYBounds.second);
        mInitialYBounds.second.add(mCurrentYBounds.second, mCurrentYBounds.second);

        mListener.onBoundsAnimationUpdated(mCurrentYBounds, progress);
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getDuration() {
        return mAnimator == null ? 0 : mAnimator.getDuration();
    }

}
