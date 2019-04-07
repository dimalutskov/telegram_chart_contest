package com.dlutskov.chart_lib.drawers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws Y axis labels according to current chart's y bounds
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartYAxisLabelsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartAxisLabelsDrawer<X, Y>
        implements BoundsUpdateAnimator.Listener<X, Y> {

    // Labels which reflects current chart's bounds
    private List<DrawnLabel<Y>> mCurrentLabels = new ArrayList<>();

    // Labels which reflects new chart's bounds which is used when bounds updated to animate
    // transition from current labels to target
    private List<DrawnLabel<Y>> mTargetLabels = new ArrayList<>();

    private ChartBounds<X, Y> mTargetBounds;
    private BoundsUpdateAnimator<X, Y> mBoundsAnimHandler;

    private int mCurrentLabelsAlpha = 255;

    // Dividers paint
    private Paint mGridPaint;
    // Padding between divider and label
    private int mGridPadding;

    // Reflects whether need to rebuild target bounds on next rebuild callback
    private boolean rebuildTargetBounds;

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
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds) {
        super.updateData(data, bounds);
        mCurrentLabels.clear();
        mTargetLabels.clear();
        mCurrentLabelsAlpha = 255;
        if (mBoundsAnimHandler != null) {
            mBoundsAnimHandler.cancel();
        }
    }

    @Override
    public void updateBounds(ChartBounds<X, Y> currentBounds, ChartBounds<X, Y> targetBounds) {
        if (targetBounds.isYBoundsEquals(currentBounds)) {
            return;
        }
        mTargetBounds = new ChartBounds<>(targetBounds);
        rebuildTargetBounds = true;

        if (mBoundsAnimHandler != null) {
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
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        // Build current labels
        if (mCurrentLabels.isEmpty()) {
            buildLabels(mCurrentLabels, bounds, drawingRect);
        } else {
            for (int i = 0; i < mCurrentLabels.size(); i++) {
                DrawnLabel<Y> label = mCurrentLabels.get(i);
                label.y = ChartUtils.calcYCoordinate(bounds, drawingRect, label.value);
            }
        }
        // Build target labels if bounds were updated
        if (mTargetBounds != null) {
            if (rebuildTargetBounds) {
                rebuildTargetBounds = false;
                buildLabels(mTargetLabels, mTargetBounds, drawingRect);
            } else {
                for (int i = 0; i < mTargetLabels.size(); i++) {
                    DrawnLabel<Y> label = mTargetLabels.get(i);
                    label.y = ChartUtils.calcYCoordinate(bounds, drawingRect, label.value);
                }
            }
        }
    }

    private void buildLabels(List<DrawnLabel<Y>> labels, ChartBounds<X, Y> bounds, Rect drawingRect) {
        labels.clear();
        // As label has own size - last label need to be drawn not at the top point
        float heightToLastLabel = (drawingRect.height()  - mTextSize - mGridPadding);
        Y maxLabelValue = (Y) bounds.getMinY().add(bounds.getMinY().distanceTo(bounds.getMaxY())
                .getPart(heightToLastLabel / drawingRect.height()));
        Y step = (Y) bounds.getMinY().distanceTo(maxLabelValue).getPart(1.f / mLabelsCount);
        Y currentValue = bounds.getMinY();
        for (int i = 0; i <= mLabelsCount; i++) {
            String label = currentValue.getAxisName();
            DrawnLabel<Y> drawnLabel = new DrawnLabel(currentValue, label);
            drawnLabel.y = ChartUtils.calcYCoordinate(bounds, drawingRect, currentValue);
            labels.add(drawnLabel);
            currentValue = (Y) currentValue.add(step);
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {
        // Draws grid for current labels
        for (DrawnLabel label : mCurrentLabels) {
            mGridPaint.setAlpha(mCurrentLabelsAlpha);
            canvas.drawLine(drawingRect.left, label.y, drawingRect.right, label.y, mGridPaint);
        }
        // Draws grid for targeting labels
        for (DrawnLabel label : mTargetLabels) {
            mGridPaint.setAlpha(255 - mCurrentLabelsAlpha);
            canvas.drawLine(drawingRect.left, label.y, drawingRect.right, label.y, mGridPaint);
        }
    }

    @Override
    public void onAfterDraw(Canvas canvas, Rect drawingRect) {
        super.onAfterDraw(canvas, drawingRect);
        // Draws current labels text
        for (DrawnLabel label : mCurrentLabels) {
            mLabelPaint.setAlpha(mCurrentLabelsAlpha);
            canvas.drawText(label.text, drawingRect.left, label.y - mGridPadding, mLabelPaint);
        }
        // Draws targeting labels text
        for (DrawnLabel label : mTargetLabels) {
            mLabelPaint.setAlpha(255 - mCurrentLabelsAlpha);
            canvas.drawText(label.text, drawingRect.left, label.y - mGridPadding, mLabelPaint);
        }
    }

    @Override
    public void onBoundsUpdated(ChartBounds bounds, float updateProgress) {
        super.updateBounds(bounds, bounds);
        mCurrentLabelsAlpha = (int) (200 * (1 - updateProgress));

        if (mTargetBounds != null && mTargetBounds.isYBoundsEquals(bounds)) {
            mCurrentLabels = mTargetLabels;
            mCurrentLabelsAlpha = 255;
            mTargetLabels = new ArrayList<>();
            mTargetBounds = null;
        }
    }

    public void setGridColor(int color) {
        mGridPaint.setColor(color);
        mChartView.invalidate();
    }

    public void setGridStrokeWidth(int strokeWidth) {
        mGridPaint.setStrokeWidth(strokeWidth);
        mChartView.invalidate();
    }

    public void setGridPadding(int gridPadding) {
        mGridPadding = gridPadding;
        mChartView.invalidate();
    }

    // Contains data about axis label which is drawn on the canvas
    private static class DrawnLabel<C extends ChartCoordinate> {
        final C value;
        final String text;
        float y;
        DrawnLabel(C value, String text) {
            this.text = text;
            this.value = value;
        }
    }
}