package com.dlutskov.chart_lib.drawers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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
import java.util.Set;

/**
 * Draws X axis labels according to x chart's bounds
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartXAxisLabelsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartAxisLabelsDrawer<X, Y> {

    // List of current drawn labels which reflects chart's x axis bounds
    private List<LabelCell> mLabelCells = new ArrayList<>();

    // List of handlers which contains animated axis labels which need to be appeared or disappeared
    private List<AnimatedCellsHandler> mAnimatedCellsHandlers = new ArrayList<>();

    private Paint mBackgroundPaint = new Paint();

    private long mFadingAnimationDuration = ChartUtils.DEFAULT_CHART_CHANGES_ANIMATION_DURATION;

    private boolean isExpandedPoints;

    public ChartXAxisLabelsDrawer(ChartView<X, Y> chartView, int size) {
        super(chartView, size);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.WHITE);
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Set<String> hiddenChartPoints) {
        super.updateData(data, bounds, hiddenChartPoints);
        mLabelCells.clear();
    }

    @Override
    public void updateBounds(ChartBounds<X, Y> oldBounds, ChartBounds<X, Y> newBounds) {
        // Ignore Y bounds updates
        if (!oldBounds.isXBoundsEquals(newBounds)) {
            super.updateBounds(oldBounds, newBounds);
        }
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        List<X> points = data.getXPoints().getPoints();
        // Actual size of cells which can be drawn
        int actualCellSize = (int) ((bounds.getMaxXIndex() - bounds.getMinXIndex()) / (float) (mLabelsCount));
        // Current size of cells which already drawn
        int currentCellSize = mLabelCells.size() < 2 ? actualCellSize : mLabelCells.get(1).position - mLabelCells.get(0).position;

        // Call rebuild for animated labels also
        for (AnimatedCellsHandler animatorHandler : mAnimatedCellsHandlers) {
            animatorHandler.rebuild(bounds, drawingRect);
        }

        // Remove all cells which are out of bounds
        for (int i = mLabelCells.size() - 1; i >= 0; i--) {
            LabelCell drawnLabel = mLabelCells.get(i);
            float left = ChartUtils.calcXCoordinate(bounds, drawingRect, drawnLabel.position);
            float right = left + drawnLabel.textWidth;
            if (right < drawingRect.left || (left - currentCellSize < 0 && left < drawingRect.left)
                    || left > drawingRect.right || (right + currentCellSize > points.size() - 1 && right > drawingRect.right)) {
                mLabelCells.remove(drawnLabel);
            }
        }

        // Add first cell from the right side if there are no cells yet
        if (mLabelCells.isEmpty()) {
            int position = bounds.getMaxXIndex() - currentCellSize;
            String name = isExpandedPoints ? points.get(position).getExpandedName() : points.get(position).getAxisName();
            LabelCell label = new LabelCell(name, position, mLabelPaint.measureText(name));
            mLabelCells.add(label);
        }

        // Add cells before the leftmost cell if need
        while (true) {
            int leftCellPosition = mLabelCells.get(0).position;
            int nextCellPosition = leftCellPosition - currentCellSize;
            if (leftCellPosition < bounds.getMinXIndex() || nextCellPosition < 0) {
                break;
            }
            String name = isExpandedPoints ? points.get(nextCellPosition).getExpandedName() : points.get(nextCellPosition).getAxisName();
            LabelCell label = new LabelCell(name, nextCellPosition, mLabelPaint.measureText(name));
            mLabelCells.add(0, label);
        }

        // Add cells after the rightmost cell if need
        while (true) {
            int rightCellPosition = mLabelCells.get(mLabelCells.size() - 1).position;
            int nextCellPosition = rightCellPosition + currentCellSize;
            if (nextCellPosition > bounds.getMaxXIndex() || nextCellPosition + actualCellSize > points.size() - 1) {
                break;
            }
            String name = isExpandedPoints ? points.get(nextCellPosition).getExpandedName() : points.get(nextCellPosition).getAxisName();
            LabelCell label = new LabelCell(name, nextCellPosition, mLabelPaint.measureText(name));
            mLabelCells.add(label);
        }

        // Loop through added cells to check distance between them
        int labelCellIndex = mLabelCells.size() - 1;
        List<LabelCell> animatedCells = new ArrayList<>();
        boolean newLabelsAppeared = false;
        boolean labelsDisappeared = false;
        while (labelCellIndex > 0) {
            LabelCell currentCell = mLabelCells.get(labelCellIndex);
            LabelCell nextCell = mLabelCells.get(labelCellIndex - 1);
            if (!newLabelsAppeared && currentCell.position - nextCell.position < actualCellSize * 0.8f) {
                // Distance between cell is too small - remove next cell
                animatedCells.add(nextCell);
                mLabelCells.remove(nextCell);
                labelCellIndex--;
                labelsDisappeared = true;
            } else if (!labelsDisappeared && currentCell.position - nextCell.position > actualCellSize * 1.8f) {
                // Distance between cells is enough to add new cell between
                int labelPosition = currentCell.position + (nextCell.position - currentCell.position) / 2;
                String name = isExpandedPoints ? points.get(labelPosition).getExpandedName() : points.get(labelPosition).getAxisName();
                LabelCell label = new LabelCell(name, labelPosition, mLabelPaint.measureText(name));
                mLabelCells.add(labelCellIndex, label); // ?
                animatedCells.add(label);
                newLabelsAppeared = true;
                labelCellIndex--;
            } else {
                labelCellIndex--;
            }
        }

        if (!animatedCells.isEmpty()) {
            AnimatedCellsHandler animatedCellsHandler = new AnimatedCellsHandler(animatedCells, newLabelsAppeared);
            mAnimatedCellsHandlers.add(animatedCellsHandler);
            animatedCellsHandler.startAnimator(mFadingAnimationDuration, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mAnimatedCellsHandlers.remove(animatedCellsHandler);
                }
            });
        }

        // Calculate coordinates for current labels
        for (int i = 0; i < mLabelCells.size(); i++) {
            LabelCell cell = mLabelCells.get(i);
            if (i == mLabelCells.size() - 1 && bounds.getMaxXIndex() == points.size() - 1) {

            }
            cell.x = ChartUtils.calcXCoordinate(bounds, drawingRect, cell.position);
        }

    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {}

    @Override
    public void onAfterDraw(Canvas canvas, Rect drawingRect) {
        super.onAfterDraw(canvas, drawingRect);
        // Draw background
        float rectPaddingTop = (canvas.getHeight() - drawingRect.bottom) * 0.1f;
        canvas.drawRect(drawingRect.left, drawingRect.bottom + rectPaddingTop, drawingRect.right, canvas.getHeight(), mBackgroundPaint);

        float y = canvas.getHeight() - mTextSize / 2;
        for (LabelCell drawnLabel : mLabelCells) {
            int alpha = Math.min(mAlpha, drawnLabel.alpha);
            mLabelPaint.setAlpha(Math.min(alpha, MAX_LABEL_ALPHA));
            canvas.drawText(drawnLabel.text, drawnLabel.x, y, mLabelPaint);
        }
        for (AnimatedCellsHandler animatedCellsHandler : mAnimatedCellsHandlers) {
            animatedCellsHandler.draw(canvas, y);
        }
    }

    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
        mChartView.invalidate();
    }

    public void setExpandedPoints(boolean expandedPoints) {
        if (isExpandedPoints != expandedPoints) {
            isExpandedPoints = expandedPoints;
            mLabelCells.clear();
            invalidate();
            mChartView.invalidate();
        }
    }

    // Contains data about axis label which is drawn on the canvas
    private static class LabelCell {
        final String text;
        final int position;
        final float textWidth;
        float x;
        int alpha;
        LabelCell(String text, int position, float textWidth) {
            this.text = text;
            this.position = position;
            this.textWidth = textWidth;
            this.alpha = 255;
        }
    }

    /**
     * Contains animated axis labels which need to be appeared or disappeared according to visible axis size changes
     */
    private class AnimatedCellsHandler implements ValueAnimator.AnimatorUpdateListener {

        private final List<LabelCell> mAnimatedCells;

        private ValueAnimator mAnimator;

        AnimatedCellsHandler(List<LabelCell> cells, boolean appear) {
           mAnimatedCells = cells;
           mAnimator = ValueAnimator.ofInt(appear ? 0 : 255, appear ? 255 : 0);
           mAnimator.addUpdateListener(this);
        }

        void startAnimator(long duration, Animator.AnimatorListener animatorListener) {
            mAnimator.setDuration(duration);
            mAnimator.addListener(animatorListener);
            mAnimator.start();
        }

        void rebuild(ChartBounds<X, Y> bounds, Rect drawingRect) {
            // Calculate coordinates for current labels
            for (LabelCell label : mAnimatedCells) {
                label.x = ChartUtils.calcXCoordinate(bounds, drawingRect, label.position);
            }
        }

        void draw(Canvas canvas, float y) {
            for (LabelCell labelCell : mAnimatedCells) {
                int alpha = Math.min(mAlpha, labelCell.alpha);
                mLabelPaint.setAlpha(Math.min(alpha, MAX_LABEL_ALPHA));
                canvas.drawText(labelCell.text, labelCell.x, y, mLabelPaint);
            }
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            for (LabelCell labelCell : mAnimatedCells) {
                labelCell.alpha = (Integer) animation.getAnimatedValue();
            }
            mChartView.invalidate();
        }

    }

}