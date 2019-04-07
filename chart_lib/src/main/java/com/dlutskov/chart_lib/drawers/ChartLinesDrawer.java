package com.dlutskov.chart_lib.drawers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.ChartUtils;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws chart's lines and handles update bounds and visibility animations
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartLinesDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartDataDrawer<X, Y>
        implements BoundsUpdateAnimator.Listener<X, Y>  {

    // Default drawing line stroke width in DP
    private static final int DEFAULT_LINE_STROKE_WIDTH = 2;

    private final List<PointsDrawingData<Y>> mDrawingData = new ArrayList<>();

    private BoundsUpdateAnimator<X, Y> mBoundsAnimHandler;

    private Map<String, ValueAnimator> mPointsAnimators = new HashMap<>();

    private long mAnimDuration;
    private int mLineStrokeWidth;

    public ChartLinesDrawer(ChartView<X, Y> chartView) {
        super(chartView);
        mAnimDuration = ChartUtils.DEFAULT_CHART_CHANGES_ANIMATION_DURATION;
        mLineStrokeWidth = ChartUtils.getPixelForDp(chartView.getContext(), DEFAULT_LINE_STROKE_WIDTH);
    }

    public void setAnimDuration(long animDuration) {
        mAnimDuration = animDuration;
    }

    public void setLineStrokeWidth(int lineStrokeWidth) {
        mLineStrokeWidth = lineStrokeWidth;
    }

    public int getLineStrokeWidth() {
        return mLineStrokeWidth;
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds) {
        super.updateData(data, bounds);
        mDrawingData.clear();
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
    public void onBoundsUpdated(ChartBounds<X, Y> bounds, float updateProgress) {
        super.updateBounds(bounds, bounds);
    }

    @Override
    public void updatePointsVisibility(final String pointsId, boolean visible) {
        final PointsDrawingData<Y> linesDrawer = findDrawingData(pointsId);
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
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        for (ChartPointsData<Y> pointsData : data.getYPoints()) {
            PointsDrawingData<Y> drawingData = findDrawingData(pointsData.getId());
            if (drawingData == null) {
                drawingData = new PointsDrawingData<>(pointsData, mLineStrokeWidth);
                mDrawingData.add(drawingData);
            }
            if (!drawingData.isVisible) continue;

            buildLines(drawingData.mLines, pointsData.getPoints(), bounds, drawingRect);
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect drawingRect) {
        for (PointsDrawingData<Y> drawingData : mDrawingData) {
            if (drawingData.isVisible) {
                canvas.drawLines(drawingData.mLines, drawingData.mPaint);
            }
        }
    }

    private void buildLines(float lines[], List<Y> yPoints, ChartBounds<X, Y> bounds, Rect drawingRect) {
        int lineIndex = 0;
        for (int i = 0; i < yPoints.size() - 1; i++) {
            float x = ChartUtils.calcXCoordinate(bounds, drawingRect, i);
            float y = ChartUtils.calcYCoordinate(bounds, drawingRect, yPoints.get(i));
            lines[lineIndex++] = x;
            lines[lineIndex++] = y;
            x = ChartUtils.calcXCoordinate(bounds, drawingRect, i + 1);
            y = ChartUtils.calcYCoordinate(bounds, drawingRect, yPoints.get(i + 1));
            lines[lineIndex++] = x;
            lines[lineIndex++] = y;
        }
    }

    private PointsDrawingData<Y> findDrawingData(String pointsId) {
        for (PointsDrawingData<Y> data : mDrawingData) {
            if (data.mId.equals(pointsId)) return data;
        }
        return null;
    }

    class LinesAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private final PointsDrawingData<Y> mPointsData;
        private final int mInitialAlpha;
        private boolean mAppear;

        LinesAnimatorUpdateListener(PointsDrawingData<Y> pointsData, boolean appear) {
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

    private static class PointsDrawingData<C extends ChartCoordinate> {

        private final String mId;

        private final Paint mPaint;

        private float[] mLines;

        private boolean isVisible = true;

        PointsDrawingData(ChartPointsData<C> pointsData, int strokeWidth) {
            mId = pointsData.getId();

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(pointsData.getColor());
            mPaint.setStrokeWidth(strokeWidth);

            mLines = new float[pointsData.getPoints().size() * 4];
        }

        public Paint getPaint() {
            return mPaint;
        }

        public void setVisible(boolean visible) {
            this.isVisible = visible;
        }
    }

}
