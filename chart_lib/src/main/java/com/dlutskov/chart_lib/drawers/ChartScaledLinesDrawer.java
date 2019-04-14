package com.dlutskov.chart_lib.drawers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;
import com.dlutskov.chart_lib.utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChartScaledLinesDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartLinesDrawer<X, Y> {

    private Map<String, BoundsUpdateAnimator<X, Y>> mBoundsAnimHandlers = new HashMap<>();
    private Map<String, ChartBounds<X, Y>> mLineBounds = new HashMap<>();

    public ChartScaledLinesDrawer(ChartView<X, Y> chartView) {
        super(chartView);
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Set<String> hiddenChartPoints) {
        super.updateData(data, bounds, hiddenChartPoints);
        // Stop all running animators
        for (BoundsUpdateAnimator<X, Y> animator : mBoundsAnimHandlers.values()) {
            animator.cancel();
        }

        // Recalculate Y bounds for each points
        for (ChartPointsData<Y> pointsData : getData().getYPoints()) {
            Pair<Integer, Integer> boundsIndexes = ChartPointsData.calculateMinMaxIndexes(pointsData, bounds.getMinXIndex(), bounds.getMaxXIndex());
            Y minY = pointsData.getPoints().get(boundsIndexes.first);
            Y maxY = pointsData.getPoints().get(boundsIndexes.second);
            ChartBounds<X, Y> localTargetBounds = new ChartBounds<>(bounds);
            localTargetBounds.setMinY(minY);
            localTargetBounds.setMaxY(maxY);
            mLineBounds.put(pointsData.getId(), localTargetBounds);
        }
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (!drawingData.isVisible()) continue;

            buildLines(drawingData.mLines, pointsData.getPoints(), mLineBounds.get(pointsData.getId()), drawingRect);
        }
    }

    @Override
    public void updateBounds(ChartBounds<X, Y> currentBounds, ChartBounds<X, Y> targetBounds) {
        updateBoundsInternal(targetBounds);

        if (!mAnimateBoundsChanges) {
            // Just Recalculate Y bounds for each points
            for (ChartPointsData<Y> pointsData : getData().getYPoints()) {
                Pair<Integer, Integer> boundsIndexes = ChartPointsData.calculateMinMaxIndexes(pointsData, targetBounds.getMinXIndex(), targetBounds.getMaxXIndex());
                Y minY = pointsData.getPoints().get(boundsIndexes.first);
                Y maxY = pointsData.getPoints().get(boundsIndexes.second);
                ChartBounds<X, Y> localTargetBounds = new ChartBounds<>(targetBounds);
                localTargetBounds.setMinY(minY);
                localTargetBounds.setMaxY(maxY);
                mLineBounds.put(pointsData.getId(), localTargetBounds);
            }
            return;
        }

        // Recalculate Y bounds for each points
        for (ChartPointsData<Y> pointsData : getData().getYPoints()) {
            // Update X values for all line bounds
            ChartBounds<X, Y> localBounds = mLineBounds.get(pointsData.getId());
            localBounds.setMinXIndex(targetBounds.getMinXIndex());
            localBounds.setMaxXIndex(targetBounds.getMaxXIndex());

            // Skip invisible lines
            DrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (!drawingData.isVisible()) continue;

            // Calculate Y bounds for specific line
            Pair<Integer, Integer> boundsIndexes = ChartPointsData.calculateMinMaxIndexes(pointsData, targetBounds.getMinXIndex(), targetBounds.getMaxXIndex());
            Y minY = pointsData.getPoints().get(boundsIndexes.first);
            Y maxY = pointsData.getPoints().get(boundsIndexes.second);
            ChartBounds<X, Y> localTargetBounds = new ChartBounds<>(localBounds);
            localTargetBounds.setMinY(minY);
            localTargetBounds.setMaxY(maxY);

            BoundsUpdateAnimator<X, Y> boundsAnimHandler = mBoundsAnimHandlers.get(pointsData.getId());
            if (boundsAnimHandler != null) {
                if (boundsAnimHandler.isTargetTheSame(localTargetBounds)) {
                    // No need to update y bounds animator already running to move to same target
                    continue;
                } else {
                    // Use currently animated bounds
                    localBounds.setMinY(boundsAnimHandler.getCurrentYBounds().first);
                    localBounds.setMaxY(boundsAnimHandler.getCurrentYBounds().second);
                    // Cancel previously started animator
                    boundsAnimHandler.cancel();
                }
            }
            boundsAnimHandler = new BoundsUpdateAnimator<>(localBounds, localTargetBounds, (bounds, updateProgress) -> {
                mLineBounds.get(pointsData.getId()).setMinY(bounds.first);
                mLineBounds.get(pointsData.getId()).setMaxY(bounds.second);
                mChartView.invalidate();
                invalidate();
            });
            mBoundsAnimHandlers.put(pointsData.getId(), boundsAnimHandler);
            boundsAnimHandler.start(getAnimDuration(), new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mBoundsAnimHandlers.remove(pointsData.getId());
                }
            });
        }
    }

    @Override
    ChartBounds<X, Y> getSelectedPointsBounds(String pointsId) {
        return mLineBounds.get(pointsId);
    }
}
