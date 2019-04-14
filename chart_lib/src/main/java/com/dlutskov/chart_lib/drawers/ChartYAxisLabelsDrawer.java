package com.dlutskov.chart_lib.drawers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Draws Y axis labels according to current chart's y bounds
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartYAxisLabelsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartAxisLabelsDrawer<X, Y> {

    // Max disappearing animated labels collections count
    private static final int MAX_DISAPPEARING_LABELS_SIZE = 1;

    // Part of drawing rect height which will be used for translate animations
    private static final float TRANSITION_RATIO = 0.25f;

    // Displaying on the drawing area sides constants
    public static final int SIDE_LEFT = -1;
    public static final int SIDE_RIGHT = 1;

    private List<LabelsAnimatorHandler> mLabelsDisappearAnimators = new ArrayList<>();
    private LabelsAnimatorHandler mLabelsAppearAnimator;

    private ChartBounds<X, Y> mLastBounds;

    // Dividers paint
    protected Paint mGridPaint;
    // Padding between divider and label
    protected int mGridPadding;

    // Reflects whether need to draw label dividers
    private boolean mDrawGrid = true;

    // Side where the labels will be drawn (-1 - left side, 1 - right side)
    private int mSide = SIDE_LEFT;

    // - 1 From bottom to top, 1 - from top to bottom
    private int mLastBoundsAppearanceDirection;

    /**
     * Used for scaled charts where each line has separated bounds. If this param is specified - labels will be
     * calculated for bounds which corresponds to this points only. By default is null - labels will be calculated
     * for common chart's bounds
     */
    private String mScaledPointsId;

    private long mAnimDuration = ChartUtils.DEFAULT_CHART_CHANGES_ANIMATION_DURATION;

    public ChartYAxisLabelsDrawer(ChartView<X, Y> chartView, int size) {
        super(chartView, size);

        mGridPaint = new Paint();
        mGridPaint.setAntiAlias(true);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setStrokeWidth(ChartUtils.getPixelForDp(mChartView.getContext(), 1));
        mGridPaint.setColor(Color.LTGRAY);

        mGridPadding = ChartUtils.getPixelForDp(mChartView.getContext(), 6);
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Set<String> hiddenChartPoints) {
        if (mScaledPointsId != null) {
            // Drawer is related to specific points - so need to calculate bounds only for this points
            bounds = calculateScaledBounds(bounds, data);
        }
        super.updateData(data, bounds, hiddenChartPoints);
    }

    @Override
    public void updateBounds(ChartBounds<X, Y> currentBounds, ChartBounds<X, Y> targetBounds) {
        if (mScaledPointsId != null) {
            // Drawer is related to specific points - so need to calculate bounds only for this points
            targetBounds = calculateScaledBounds(targetBounds, getData());
        }

        // Ignore if Y bounds were not changed
        if (targetBounds.isYBoundsEquals(currentBounds) ||
                (mScaledPointsId != null && mLastBounds != null && mLastBounds.isYBoundsEquals(targetBounds))) {
            return;
        }

        mLastBoundsAppearanceDirection = targetBounds.getMaxY().compareTo(currentBounds.getMaxY()) < 0 ? -1 : 1;
        updateBoundsInternal(targetBounds);
        mLastBounds = targetBounds;
    }

    private ChartBounds<X, Y> calculateScaledBounds(ChartBounds<X, Y> bounds, ChartLinesData<X, Y> data) {
        // Drawer is related to specific points - so need to calculate bounds only for this points
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            if (pointsData.getId().equals(mScaledPointsId)) {
                Pair<Integer, Integer> yBounds = ChartPointsData.calculateMinMaxIndexes(pointsData, bounds.getMinXIndex(), bounds.getMaxXIndex());
                bounds = new ChartBounds<>(bounds);
                bounds.setMinY(pointsData.getPoints().get(yBounds.first));
                bounds.setMaxY(pointsData.getPoints().get(yBounds.second));
                break;
            }
        }
        return bounds;
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        // Build current labels
        if (mLabelsAppearAnimator == null) {
            mLabelsAppearAnimator = new LabelsAnimatorHandler(buildLabels(bounds, drawingRect), mLastBoundsAppearanceDirection, true);
            mLabelsAppearAnimator.start(0, null);
        } else {
            // Disappear previous labels
            LabelsAnimatorHandler animatorHandler = new LabelsAnimatorHandler(mLabelsAppearAnimator.mLabels, mLastBoundsAppearanceDirection, false);
            if (mLabelsDisappearAnimators.size() == MAX_DISAPPEARING_LABELS_SIZE) {
                mLabelsDisappearAnimators.set(0, animatorHandler);
            } else {
                mLabelsDisappearAnimators.add(animatorHandler);
            }
            animatorHandler.start(mAnimDuration, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mLabelsDisappearAnimators.remove(animatorHandler);
                }
            });
            // Appear new labels
            mLabelsAppearAnimator.cancel();
            mLabelsAppearAnimator = new LabelsAnimatorHandler(buildLabels(bounds, drawingRect), 1, true);
            mLabelsAppearAnimator.start(mAnimDuration, null);
        }
    }

    private List<DrawnLabel<Y>> buildLabels(ChartBounds<X, Y> bounds, Rect drawingRect) {
        // TODO proper values!!!
        List<DrawnLabel<Y>> labels = new ArrayList<>();
        // As label has own size - last label need to be drawn not at the top point
        float heightToLastLabel = (drawingRect.height()  - mTextSize - mGridPadding);
        Y maxLabelValue = (Y) bounds.getMinY().add(bounds.getMinY().distanceTo(bounds.getMaxY())
                .getPart(heightToLastLabel / drawingRect.height()));
        Y step = (Y) bounds.getMinY().distanceTo(maxLabelValue).getPart(1.f / mLabelsCount);
        Y currentValue = bounds.getMinY();
        for (int i = 0; i <= mLabelsCount; i++) {
            String label = currentValue.getAxisName();
            DrawnLabel<Y> drawnLabel = new DrawnLabel(currentValue, label);
            labels.add(drawnLabel);
            currentValue = (Y) currentValue.add(step);
        }
        return labels;
    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {
        if (mDrawGrid) {
            // Draws grid for current labels
            mLabelsAppearAnimator.onDraw(canvas, drawingRect);
            for (LabelsAnimatorHandler animatorHandler : mLabelsDisappearAnimators) {
                animatorHandler.onDraw(canvas, drawingRect);
            }
        }
    }

    @Override
    public void onAfterDraw(Canvas canvas, Rect drawingRect) {
        super.onAfterDraw(canvas, drawingRect);
        // Draws current labels text
        mLabelsAppearAnimator.onAfterDraw(canvas, drawingRect);
        for (LabelsAnimatorHandler animatorHandler : mLabelsDisappearAnimators) {
            animatorHandler.onAfterDraw(canvas, drawingRect);
        }
    }

    @Override
    public void setTextColor(int textColor) {
        // If scaled points specified - points will be the same as points color
        if (mScaledPointsId == null) {
            super.setTextColor(textColor);
        }
    }

    public void setGridColor(int color) {
        mGridPaint.setColor(color);
        mChartView.invalidate();
    }

    public void setDrawGrid(boolean drawGrid) {
        mDrawGrid = drawGrid;
    }

    public void setGridStrokeWidth(int strokeWidth) {
        mGridPaint.setStrokeWidth(strokeWidth);
        mChartView.invalidate();
    }

    public void setGridPadding(int gridPadding) {
        mGridPadding = gridPadding;
        mChartView.invalidate();
    }

    public void setSide(int side) {
        mSide = side;
    }

    public void setScaledPointsId(String pointsId, int color) {
        mScaledPointsId = pointsId;
        super.setTextColor(color);
    }

    private float getLabelXCoordinate(DrawnLabel label, Rect drawingRect) {
        if (mSide == SIDE_RIGHT) {
            return drawingRect.right - mLabelPaint.measureText(label.text);
        } else {
            return drawingRect.left;
        }
    }

    // Contains data about axis label which is drawn on the canvas
    private static class DrawnLabel<C extends ChartCoordinate> {
        final C value;
        final String text;
        DrawnLabel(C value, String text) {
            this.text = text;
            this.value = value;
        }
    }

    /**
     * Handles labels transition animations - from to to bottom or vice versa
     * according to y bounds changes
     */
    private class LabelsAnimatorHandler implements ValueAnimator.AnimatorUpdateListener {

        private final List<DrawnLabel<Y>> mLabels;
        private final int mAnimationDirection;
        private final boolean mAppear;

        private ValueAnimator mAnimator;
        private float mAnimatorProgress;

        LabelsAnimatorHandler(List<DrawnLabel<Y>> labels, int direction, boolean appear) {
            mLabels = labels;
            mAnimationDirection = direction;
            mAppear = appear;
        }

        void onDraw(Canvas canvas, Rect drawingRect) {
            float strokeWidth = mGridPaint.getStrokeWidth();
            int part = drawingRect.height() / (mLabelsCount + 1);
            for (int i = 0; i < mLabels.size(); i++) {
                mGridPaint.setAlpha(Math.min(mAlpha, (int) (mAppear ? mAnimatorProgress * 255 : (1 - mAnimatorProgress) * 255)));
                float y = drawingRect.bottom - part * i;
                y = calculateAnimatedY(y, drawingRect);
                canvas.drawLine(drawingRect.left, y - strokeWidth, drawingRect.right, y - strokeWidth, mGridPaint);
            }
        }

        void onAfterDraw(Canvas canvas, Rect drawingRect) {
            int part = drawingRect.height() / (mLabelsCount + 1);
            for (int i = 0; i < mLabels.size(); i++) {
                DrawnLabel<Y> label = mLabels.get(i);
                mLabelPaint.setAlpha((Math.min(mAlpha, (int) (mAppear ? mAnimatorProgress * 255 : (1 - mAnimatorProgress) * 255))));
                float x = getLabelXCoordinate(label, drawingRect);
                float y = drawingRect.bottom - part * i;
                y = calculateAnimatedY(y, drawingRect);
                canvas.drawText(label.text, x, y - mGridPadding, mLabelPaint);
            }
        }

        private float calculateAnimatedY(float y, Rect drawingRect) {
            if (mAppear) {
                if (mLastBoundsAppearanceDirection < 0) {
                    y = y + drawingRect.height() * TRANSITION_RATIO * (1 - mAnimatorProgress);
                } else {
                    y = y - drawingRect.height() * TRANSITION_RATIO * (1 - mAnimatorProgress);
                }
            } else {
                if (mAnimationDirection < 0) {
                    y = y - drawingRect.height() * TRANSITION_RATIO * mAnimatorProgress;
                } else {
                    y = y + drawingRect.height()  * TRANSITION_RATIO * mAnimatorProgress;
                }
            }
            return y;
        }

        void start(long duration, Animator.AnimatorListener listener) {
            mAnimator = ValueAnimator.ofFloat(0f, 1f);
            mAnimator.setDuration(duration);
            mAnimator.addUpdateListener(this);
            if (listener != null) {
                mAnimator.addListener(listener);
            }
            mAnimator.start();
        }

        void cancel() {
            if (mAnimator != null) {
                mAnimator.cancel();
            }
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mAnimatorProgress = (float) animation.getAnimatedValue();
            mChartView.invalidate();
        }
    }

}