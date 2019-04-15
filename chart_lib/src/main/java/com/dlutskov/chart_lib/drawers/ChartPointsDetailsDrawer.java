package com.dlutskov.chart_lib.drawers;

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

    private final Paint mBackgroundBorderPaint;
    private final Paint mBackgroundPaint;

    private final Paint mXLabelTextPaint;
    private final Paint mPercentsTextPaint;
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

    // Window width excluding horizontal paddings
    private float mWidth;

    // Maximum alpha value of window background
    private int mMaxBackgroundAlpha;

    // Chart data
    private ChartLinesData<X, Y> mData;
    private ChartBounds<X, Y> mBounds;

    // Index of selected x value of points which need to show
    private int mSelectedPointPosition = -1;

    // Alpha of all displayed object - used for appear/disappear animation
    private int mCurrentAlpha;

    // Contains ids of hidden chart lines - to not show their points also
    private Set<String> mHiddenChartLines = new HashSet<>();

    private Y mZero;
    private Y mYSum;
    private String[] mPercentagesStrings;

    private boolean isShown;

    private final boolean isExpandedPoints;

    public ChartPointsDetailsDrawer(ChartView<X, Y> chartView, boolean isExpandedPoints) {
        super(chartView);

        this.isExpandedPoints = isExpandedPoints;

        Context ctx = chartView.getContext();
        mCornerRadius = ChartUtils.getPixelForDp(ctx, 4);
        mTextSize = ChartUtils.getPixelForDp(ctx, 11);
        mVerticalMargin = ChartUtils.getPixelForDp(ctx, 8);
        mHorizontalMargin = ChartUtils.getPixelForDp(ctx, 12);
        mVerticalPadding = ChartUtils.getPixelForDp(ctx, 6);
        mHorizontalPadding = ChartUtils.getPixelForDp(ctx, 6);
        mLabelVerticalPadding = ChartUtils.getPixelForDp(ctx, 4);

        mMinWidth = ChartUtils.getPixelForDp(ctx, 120);

        mBackgroundBorderPaint = createPaint(Paint.Style.STROKE, Color.BLACK);
        mBackgroundBorderPaint.setStrokeWidth(ChartUtils.getPixelForDp(ctx, 1f));

        mBackgroundPaint = createPaint(Paint.Style.FILL, Color.WHITE);

        mXLabelTextPaint = createPaint(Paint.Style.FILL_AND_STROKE, Color.BLACK);
        mXLabelTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mXLabelTextPaint.setTextSize(mTextSize);

        mLabelTextPaint = createPaint(Paint.Style.FILL_AND_STROKE, Color.BLACK);
        mLabelTextPaint.setTextSize(mTextSize);

        mPercentsTextPaint = createPaint(Paint.Style.FILL_AND_STROKE, Color.BLACK);
        mPercentsTextPaint.setTextSize(mTextSize);
        mPercentsTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        mValuesTextPaint = createPaint(Paint.Style.FILL_AND_STROKE, Color.BLACK);
        mValuesTextPaint.setTextSize(mTextSize);

        mMaxBackgroundAlpha = 220;
    }

    private static Paint createPaint(Paint.Style style, int color) {
        Paint paint  = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(style);
        paint.setColor(color);
        return paint;
    }

    @Override
    public void updateData(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Set<String> hiddenPoints) {
        super.updateData(data, bounds, hiddenPoints);
        mHiddenChartLines = new HashSet<>(hiddenPoints);

        if (mZero == null) {
            mZero = (Y) bounds.getMinY().zero();
            mYSum = (Y) mZero.clone();
        }
        mPercentagesStrings = new String[data.getYPoints().size()];
        for (int i = 0; i < data.getYPoints().size(); i++) {
            mPercentagesStrings[i] = "";
        }
    }

    @Override
    public void updatePointsVisibility(String pointsId, boolean visible) {
        super.updatePointsVisibility(pointsId, visible);
        if (visible) {
            mHiddenChartLines.remove(pointsId);
        } else {
            mHiddenChartLines.add(pointsId);
        }
    }

    @Override
    protected void rebuild(ChartLinesData<X, Y> data, ChartBounds<X, Y> bounds, Rect drawingRect) {
        mData = data;
        mBounds = bounds;

        X maxX = data.getXPoints().getMaxValue();
        String xLabelText = isExpandedPoints ? maxX.getFullName() + " " + maxX.getExpandedName() : maxX.getFullName();
        float maxLabelWidth = mXLabelTextPaint.measureText(xLabelText) + ChartUtils.getDpForPixel(mChartView.getContext(), 20);

        for (ChartPointsData chartPointsData : data.getYPoints()) {
            if (mHiddenChartLines.contains(chartPointsData.getId())) continue;
            String name = chartPointsData.getName();
            if (mData.isPercentage()) {
                name += " 100%";
            }
            float textWidth = mLabelTextPaint.measureText(name);
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
    protected void onDraw(Canvas canvas, Rect drawingRect) {}

    @Override
    public void onAfterDraw(Canvas canvas, Rect drawingRect) {
        super.onAfterDraw(canvas, drawingRect);

        if (!isShown) return;

        float xPointsPosition = ChartUtils.calcXCoordinate(mBounds, drawingRect, mSelectedPointPosition);

        int visibleLinesCount = mData.getYPoints().size() - mHiddenChartLines.size();

        int viewWidth = (int) mWidth + mHorizontalPadding * 2;
        int viewHeight = mVerticalPadding + mTextSize * (visibleLinesCount + 1) + mLabelVerticalPadding * (visibleLinesCount + 1) + mVerticalPadding;

        float xPosition = xPointsPosition < drawingRect.width() / 2
                ? xPointsPosition + mHorizontalMargin
                : xPointsPosition - viewWidth - mHorizontalMargin;
        float yPosition = mChartView.getPaddingTop() + mVerticalMargin;

        if (xPosition + viewWidth > drawingRect.right - mHorizontalMargin) {
            xPosition = drawingRect.right - mHorizontalMargin - viewWidth;
        } else if (xPosition <= 0) {
            xPosition = 0;
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
        X xPoint = mData.getXPoints().getPoints().get(mSelectedPointPosition);
        String xLabel = isExpandedPoints ? xPoint.getFullName() + " " + xPoint.getExpandedName() : xPoint.getFullName();
        canvas.drawText(xLabel, leftX, yPosition, mXLabelTextPaint);

        // Draw > glyph
//        String glyph = ">";
//        canvas.drawText(glyph, rightX - mXLabelTextPaint.measureText(glyph), yPosition, mXLabelTextPaint);

        float labelXPosition = leftX;

        mLabelTextPaint.setAlpha(mCurrentAlpha);

        if (mData.isPercentage()) {
            mPercentsTextPaint.setAlpha(mCurrentAlpha);
            labelXPosition += mLabelTextPaint.measureText("100% ");
            // Calculate local bounds
            mYSum.set(mZero);
            for (int i = 0; i < mData.getYPoints().size(); i++) {
                ChartPointsData<Y> pointsData = mData.getYPoints().get(i);
                if (mHiddenChartLines.contains(pointsData.getId())) continue;
                mYSum.add(pointsData.getPoints().get(mSelectedPointPosition), mYSum);
            }
            int percentsSum = 0;
            int lastPercents = 0;
            int lastPercentsPosition = 0;
            for (int i = 0; i < mData.getYPoints().size(); i++) {
                ChartPointsData<Y> pointsData = mData.getYPoints().get(i);
                if (mHiddenChartLines.contains(pointsData.getId())) continue;
                lastPercents = Math.round(pointsData.getPoints().get(mSelectedPointPosition).calcCoordinateRatio(mZero, mYSum) * 100);
                percentsSum += lastPercents;

                String string =  lastPercents + "%";
                if (lastPercents < 0.1f) {
                    string = "  " + string;
                }

                mPercentagesStrings[i] = string;
                lastPercentsPosition = i;
            }
            if (percentsSum != 100) {
                lastPercents = lastPercents +  (100 - percentsSum);
                String string =  lastPercents + "%";
                if (lastPercents < 0.1f) {
                    string = "  " + string;
                }
                mPercentagesStrings[lastPercentsPosition] = string;
            }
        }

        // Draw Y labels
        for (int i = 0; i < mData.getYPoints().size(); i++) {
            ChartPointsData<Y> pointsData = mData.getYPoints().get(i);

            if (mHiddenChartLines.contains(pointsData.getId())) continue;

            // Draw axis name and axis point value
            mValuesTextPaint.setColor(pointsData.getColor());
            mValuesTextPaint.setAlpha(mCurrentAlpha);

            if (mData.isPercentage()) {
                canvas.drawText(mPercentagesStrings[i], leftX, yPosition + mTextSize + mLabelVerticalPadding, mPercentsTextPaint);
            }

            String nameString = pointsData.getName();
            canvas.drawText(nameString, labelXPosition, yPosition + mTextSize + mLabelVerticalPadding, mLabelTextPaint);

            String valueString = pointsData.getPoints().get(mSelectedPointPosition).getFullName();
            canvas.drawText(valueString, rightX - mValuesTextPaint.measureText(valueString), yPosition + mTextSize + mLabelVerticalPadding, mValuesTextPaint);

            yPosition += mTextSize + mLabelVerticalPadding;
        }
    }

    public void setSelectedPointIndex(int pointPosition) {
        mSelectedPointPosition = pointPosition;
    }

    public void setAlpha(int alpha) {
        mCurrentAlpha = alpha;
    }

    public void setShown(boolean show) {
        isShown = show;
    }

    public boolean isShown() {
        return isShown;
    }

    public boolean isTouchInside(float x, float y) {
        return x >= mViewRect.left && x <= mViewRect.right && y >= mViewRect.top && y <= mViewRect.bottom;
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

    public int getBackgroundColor() {
        return mBackgroundPaint.getColor();
    }

    public void setBackgroundBorderColor(int color) {
        mBackgroundBorderPaint.setColor(color);
        mChartView.invalidate();
    }

    public int getBackgroundBorderColor() {
        return mBackgroundBorderPaint.getColor();
    }

    public void setXLabelColor(int color) {
        mXLabelTextPaint.setColor(color);
        mLabelTextPaint.setColor(color);
        mChartView.invalidate();
    }

    public int getXLabelColor() {
        return mXLabelTextPaint.getColor();
    }

}
