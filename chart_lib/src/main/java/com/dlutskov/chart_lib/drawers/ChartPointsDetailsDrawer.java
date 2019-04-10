package com.dlutskov.chart_lib.drawers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.dlutskov.chart_lib.ChartBounds;
import com.dlutskov.chart_lib.utils.ChartUtils;
import com.dlutskov.chart_lib.ChartView;
import com.dlutskov.chart_lib.data.ChartLinesData;
import com.dlutskov.chart_lib.data.ChartPointsData;
import com.dlutskov.chart_lib.data.coordinates.ChartCoordinate;

import java.util.HashSet;
import java.util.Set;

/**
 * Draws "popup window" with details of all points which correspond to touched x line
 * @param <X> type of x axis chart coordinates
 * @param <Y> type of Y axis chart coordinates
 */
public class ChartPointsDetailsDrawer<X extends ChartCoordinate, Y extends ChartCoordinate> extends ChartDataDrawer<X, Y> implements ValueAnimator.AnimatorUpdateListener {

    public static final long DISAPPEARING_DELAY = 3000;

    private final Paint mDividerPaint;
    private final Paint mBackgroundBorderPaint;
    private final Paint mBackgroundPaint;
    private final Paint mPointCircleBackgroundPaint;
    private final Paint mPointCircleStokePaint;

    private final Paint mXLabelTextPaint;
    private final Paint mLabelTextPaint;
    private final Paint mValuesTextPaint;

    // Background rect
    private RectF mViewRect = new RectF();

    // Background corners
    private int mCornerRadius;

    // Top margin from top drawing rect
    private int mVerticalMargin;
    // Right margin from x touched point
    private int mHorizontalMargin;

    // Padding inside details window
    private int mVerticalPadding;
    private int mHorizontalPadding;

    // Vertical padding between labels inside window
    private int mLabelVerticalPadding;

    // Text size of drawn labels
    private int mTextSize;

    private float mMinWidth;

    private float mWidth; // TODO

    // Radius of circle which will reflect selected points coordinates
    private int mPointCircleRadius;

    // Maximum alpha value of window background
    private int mMaxBackgroundAlpha;

    // Chart data
    private ChartLinesData<X, Y> mData;
    private ChartBounds<X, Y> mBounds;

    // Index of selected x value of points which need to show
    private int mSelectedPointPosition = -1;

    private ValueAnimator mAnimator;
    private long mAlphaAnimDuration;

    // Alpha of all displayed object - used for appear/disappear animation
    private int mCurrentAlpha;

    // Contains ids of hidden chart lines - to not show their points also
    private Set<String> mHiddenChartLines = new HashSet<>();

    private boolean isShown;

    private Runnable mHideTask = () -> {
        if (isShown) {
            startAnimator(false);
        }
    };

    public ChartPointsDetailsDrawer(ChartView<X, Y> chartView) {
        super(chartView);

        Context ctx = chartView.getContext();
        mCornerRadius = ChartUtils.getPixelForDp(ctx, 4);
        mTextSize = ChartUtils.getPixelForDp(ctx, 12);
        mVerticalMargin = ChartUtils.getPixelForDp(ctx, 8);
        mHorizontalMargin = ChartUtils.getPixelForDp(ctx, 8);
        mVerticalPadding = ChartUtils.getPixelForDp(ctx, 6);
        mHorizontalPadding = ChartUtils.getPixelForDp(ctx, 6);
        mLabelVerticalPadding = ChartUtils.getPixelForDp(ctx, 4);
        mPointCircleRadius = ChartUtils.getPixelForDp(ctx, 3);

        mMinWidth = ChartUtils.getPixelForDp(ctx, 120);

        mDividerPaint = createPaint(Paint.Style.STROKE, Color.GRAY);
        mDividerPaint.setStrokeWidth(ChartUtils.getPixelForDp(ctx, 1));

        mBackgroundBorderPaint = createPaint(Paint.Style.STROKE, Color.BLACK);
        mBackgroundBorderPaint.setStrokeWidth(ChartUtils.getPixelForDp(ctx, 1f));

        mBackgroundPaint = createPaint(Paint.Style.FILL, Color.WHITE);

        mXLabelTextPaint = createPaint(Paint.Style.FILL_AND_STROKE, Color.BLACK);
        mXLabelTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mXLabelTextPaint.setTextSize(mTextSize);

        mLabelTextPaint = createPaint(Paint.Style.FILL_AND_STROKE, Color.BLACK);
        mLabelTextPaint.setTextSize(mTextSize);

        mValuesTextPaint = createPaint(Paint.Style.FILL_AND_STROKE, Color.BLACK);
        mValuesTextPaint.setTextSize(mTextSize);

        mPointCircleBackgroundPaint = createPaint(Paint.Style.FILL, Color.WHITE);
        mPointCircleStokePaint = createPaint(Paint.Style.STROKE, Color.BLACK);
        mPointCircleStokePaint.setStrokeWidth(ChartUtils.getPixelForDp(ctx, 2));

        mMaxBackgroundAlpha = 220;
        mAlphaAnimDuration = 300;
    }

    private static Paint createPaint(Paint.Style style, int color) {
        Paint paint  = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(style);
        paint.setColor(color);
        return paint;
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds) {
        super.updateData(data, bounds);
        mHiddenChartLines.clear();
        isShown = false;
    }

    @Override
    public void updateBounds(ChartBounds<X, Y> oldBounds, ChartBounds<X, Y> newBounds) {
        super.updateBounds(oldBounds, newBounds);
        isShown = false;
    }

    @Override
    public void updatePointsVisibility(String pointsId, boolean visible) {
        super.updatePointsVisibility(pointsId, visible);
        if (visible) {
            mHiddenChartLines.remove(pointsId);
        } else {
            mHiddenChartLines.add(pointsId);
        }
        isShown = false;
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        mData = data;
        mBounds = bounds;

        float maxLabelWidth = 0;
        for (ChartPointsData chartPointsData : data.getYPoints()) {
            if (mHiddenChartLines.contains(chartPointsData.getId())) continue;
            float textWidth = mLabelTextPaint.measureText(chartPointsData.getName());
            if (textWidth > maxLabelWidth) {
                maxLabelWidth = textWidth;
            }
        }
        float maxValueWidth = mValuesTextPaint.measureText(bounds.getMaxY().getFullName());
        mWidth = maxLabelWidth + mHorizontalPadding * 2 + maxValueWidth;
        if (mWidth < mMinWidth) {
            mWidth = mMinWidth;
        }
    }

    @Override
    protected void onDraw(Canvas canvas, Rect drawingRect) {
        if (!isShown) return;

        float xPointsPosition = ChartUtils.calcXCoordinate(mBounds, drawingRect, mSelectedPointPosition);
        // Draw vertical line
        canvas.drawLine(xPointsPosition, drawingRect.top, xPointsPosition, drawingRect.bottom, mDividerPaint);
    }

    @Override
    public void onAfterDraw(Canvas canvas, Rect drawingRect) {
        super.onAfterDraw(canvas, drawingRect);

        if (!isShown) return;

        float xPointsPosition = ChartUtils.calcXCoordinate(mBounds, drawingRect, mSelectedPointPosition);

        // TODO
//        // Draw point circles
//        for (int i = 0; i < mData.getYPoints().size(); i++) {
//            ChartPointsData<Y> pointsData = mData.getYPoints().get(i);
//
//            if (mHiddenChartLines.contains(pointsData.getId())) continue;
//
//            // Calculate selected points y position
//            Y pointY = pointsData.getPoints().get(mSelectedPointPosition);
//            float y = ChartUtils.calcYCoordinate(mBounds, drawingRect, pointY);
//
//            // Draw selected point background
//            mPointCircleBackgroundPaint.setAlpha(mCurrentAlpha);
//            canvas.drawCircle(xPointsPosition, y, mPointCircleRadius, mPointCircleBackgroundPaint);
//
//            // Draw selected point stroke
//            mPointCircleStokePaint.setAlpha(mCurrentAlpha);
//            mPointCircleStokePaint.setColor(pointsData.getColor());
//            canvas.drawCircle(xPointsPosition, y, mPointCircleRadius, mPointCircleStokePaint);
//        }

        int visibleLinesCount = mData.getYPoints().size() - mHiddenChartLines.size();

        int viewWidth = (int) mWidth + mHorizontalPadding * 2;
        int viewHeight = mVerticalPadding + mTextSize * (visibleLinesCount + 1) + mLabelVerticalPadding * (visibleLinesCount + 1) + mVerticalPadding;

        float xPosition = xPointsPosition + mHorizontalMargin;
        float yPosition = mVerticalMargin;

        if (xPosition + viewWidth > drawingRect.right - mHorizontalMargin) {
            xPosition =  drawingRect.right - mHorizontalMargin - viewWidth;
        }

        // Draw background
        mViewRect.set(xPosition, yPosition, xPosition + viewWidth, yPosition + viewHeight);
        mBackgroundPaint.setAlpha(Math.min(mCurrentAlpha, mMaxBackgroundAlpha));
        canvas.drawRoundRect(mViewRect, mCornerRadius, mCornerRadius, mBackgroundPaint);
        mBackgroundBorderPaint.setAlpha(mCurrentAlpha);
        canvas.drawRoundRect(mViewRect, mCornerRadius, mCornerRadius, mBackgroundBorderPaint);

        float leftX = xPosition + mHorizontalPadding;
        float rightX = leftX + viewWidth - mHorizontalPadding * 2;
        yPosition += mVerticalPadding + mTextSize;

        // Draw X label
        mXLabelTextPaint.setAlpha(mCurrentAlpha);
        String xLabel = mData.getXPoints().getPoints().get(mSelectedPointPosition).getFullName();
        canvas.drawText(xLabel, leftX, yPosition, mXLabelTextPaint);

        // Draw > glyph
        String glyph = ">";
        canvas.drawText(glyph, rightX - mXLabelTextPaint.measureText(glyph), yPosition, mXLabelTextPaint);

        mLabelTextPaint.setAlpha(mCurrentAlpha);
        // Draw Y labels
        for (int i = 0; i < mData.getYPoints().size(); i++) {
            ChartPointsData<Y> pointsData = mData.getYPoints().get(i);

            if (mHiddenChartLines.contains(pointsData.getId())) continue;

            // Draw axis name and axis point value
            mValuesTextPaint.setColor(pointsData.getColor());
            mValuesTextPaint.setAlpha(mCurrentAlpha);

            String nameString = pointsData.getName();
            canvas.drawText(nameString, leftX, yPosition + mTextSize + mLabelVerticalPadding, mLabelTextPaint);

            String valueString = pointsData.getPoints().get(mSelectedPointPosition).getFullName();
            canvas.drawText(valueString, rightX - mValuesTextPaint.measureText(valueString), yPosition + mTextSize + mLabelVerticalPadding, mValuesTextPaint);

            yPosition += mTextSize + mLabelVerticalPadding;
        }
    }

    public void showPointDetails(int xPointIndex) {
        mChartView.removeCallbacks(mHideTask);

        mSelectedPointPosition = xPointIndex;
        if (isShown) {
            mChartView.invalidate();
        } else {
            startAnimator(true);
        }
    }

    public void hidePointDetails(long delay) {
        mChartView.removeCallbacks(mHideTask);
        if (delay == 0) {
            mHideTask.run();
        } else {
            mChartView.postDelayed(mHideTask, delay);
        }
    }

    public boolean isShown() {
        return isShown;
    }

    public boolean isTouchInside(float x, float y) {
        return x >= mViewRect.left && x <= mViewRect.right && y >= mViewRect.top && y <= mViewRect.bottom;
    }

    private void startAnimator(boolean appear) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofInt(mCurrentAlpha, appear ? 255 : 0).setDuration(mAlphaAnimDuration);
        mAnimator.addUpdateListener(this);
        if (!appear) {
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isShown = false;
                }
            });
        }
        isShown = true;
        mAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mCurrentAlpha = (int) animation.getAnimatedValue();
        mChartView.invalidate();
    }

    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
        mChartView.invalidate();
    }

    public void setBackgroundBorderColor(int color) {
        mBackgroundBorderPaint.setColor(color);
        mChartView.invalidate();
    }

    public void setDividerColor(int color) {
        mDividerPaint.setColor(color);
        mChartView.invalidate();
    }

    public void setPointCircleBackground(int color) {
        mPointCircleBackgroundPaint.setColor(color);
        mChartView.invalidate();
    }

    public void setXLabelColor(int color) {
        mXLabelTextPaint.setColor(color);
        mLabelTextPaint.setColor(color);
        mChartView.invalidate();
    }

}
