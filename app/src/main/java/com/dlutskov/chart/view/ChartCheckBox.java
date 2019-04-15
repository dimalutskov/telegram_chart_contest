package com.dlutskov.chart.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.widget.Checkable;
import android.widget.FrameLayout;

import com.dlutskov.chart_lib.utils.ChartUtils;

/**
 * Custom checkbox with own checked/unchecked animation
 */
public class ChartCheckBox extends FrameLayout implements Checkable, ValueAnimator.AnimatorUpdateListener {

    // Paint for checkbox icon border
    private Paint mStrokePaint;
    // Paint for checkbox icon filled background
    private Paint mFillPaint;
    // Paint for drawing checkmark
    private Paint mCheckMarkPaint;

    private Paint mTextPaint;

    private RectF mBackgroundRect = new RectF();

    private Path mCheckMarkPath = new Path();

    // Checkbox icon rounded corners radius
    private int mCornerRadius;
    // Checkbox icon border and checkmark stroke width
    private int mStrokeWidth;
    // Padding between checkbox icon and text
    private int mTextPadding;
    // Left and Right padding
    private int mHorizontalPadding;
    // Width of checkmark icon (height will be same as textSize)
    private int mCheckMarkWidth;

    private int mTextSize;
    private String mText;

    private int mFillColor;
    private int mCheckedTextColor;

    private ValueAnimator mCurrentAnimator;
    private long mAnimDuration;

    /**
     *  Reflects how the checkbox icon is filled (checked)
     *  0 - not filled, only background border is shown
     *  1 - checkbox icon fully filled with background color
     */
    private float mAnimationProgress;

    private boolean mChecked;

    public ChartCheckBox(Context context) {
        super(context);
        init();
    }

    private void init() {
        mCornerRadius = ChartUtils.getPixelForDp(getContext(), 6);
        mStrokeWidth = ChartUtils.getPixelForDp(getContext(), 1);
        mTextPadding = ChartUtils.getPixelForDp(getContext(), 6);
        mAnimDuration = 320;

        mCheckMarkPaint = new Paint();
        mCheckMarkPaint.setAntiAlias(true);
        mCheckMarkPaint.setStyle(Paint.Style.STROKE);
        mCheckMarkPaint.setStrokeWidth(mStrokeWidth);
        mCheckMarkPaint.setColor(Color.WHITE);

        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(mStrokeWidth);
        mStrokePaint.setColor(Color.BLACK);

        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(Color.BLACK);

        mTextPaint = new Paint();
        mTextSize = ChartUtils.getPixelForDp(getContext(), 16);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(Color.BLACK);

        mCheckedTextColor = Color.WHITE;

        mCheckMarkWidth = ChartUtils.getPixelForDp(getContext(), 14);
        mHorizontalPadding = ChartUtils.getPixelForDp(getContext(), 4);

        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mCornerRadius = getMeasuredHeight() / 2;
        mHorizontalPadding = mCornerRadius;
        setMeasuredDimension((int) measureWidth(getMeasuredHeight()), getMeasuredHeight());
    }

    public float measureWidth(int height) {
        return mCheckMarkWidth + mTextPaint.measureText(mText) + mTextPadding + (height / 2) * 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw icon borders
        mBackgroundRect.set(mStrokeWidth, mStrokeWidth, canvas.getWidth() - mStrokeWidth, canvas.getHeight() - mStrokeWidth);
        canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mStrokePaint);

        // Draw filled background
        if (mAnimationProgress > 0) {
            int halfWidth = canvas.getWidth() / 2;
            float left = mStrokeWidth + halfWidth * (1 - mAnimationProgress);
            float right = halfWidth + halfWidth * mAnimationProgress;

            int halfHeight = canvas.getHeight() / 2;
            float top = mStrokeWidth + halfHeight * (1 - mAnimationProgress);
            float bottom = halfHeight + halfHeight * mAnimationProgress;

            mBackgroundRect.set(left, top, right, bottom);
            canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mFillPaint);
        }

        // Draw CheckMark
        float spaceBetweenTextAndRect = (canvas.getHeight() - mTextSize) * 0.75f;
        mCheckMarkPath.reset();
        mCheckMarkPath.moveTo(mHorizontalPadding + mStrokeWidth, canvas.getHeight() * 0.5f);
        mCheckMarkPath.lineTo(mHorizontalPadding + mCheckMarkWidth * 0.35f, canvas.getHeight() - spaceBetweenTextAndRect);
        mCheckMarkPath.lineTo(mHorizontalPadding + mCheckMarkWidth, spaceBetweenTextAndRect);
        mCheckMarkPaint.setAlpha((int) (255 * mAnimationProgress));
        canvas.drawPath(mCheckMarkPath, mCheckMarkPaint);

        // Draw text
        float textY = canvas.getHeight() - (canvas.getHeight() - mTextSize * 0.8f) / 2;
        float textXDeviation = ((mCheckMarkWidth + mTextPadding) / 2) * (1 - mAnimationProgress);
        canvas.drawText(mText, mHorizontalPadding + mCheckMarkWidth + mTextPadding - textXDeviation, textY, mTextPaint);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            mAnimationProgress = mChecked ? 1 : 0;
            mTextPaint.setColor(mChecked ? mCheckedTextColor : mFillColor);
            invalidate();
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        float animProgress = mAnimationProgress;
        setChecked(!mChecked);
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        mCurrentAnimator = ValueAnimator.ofFloat(animProgress, mChecked ? 1 : 0).setDuration(mAnimDuration);
        mCurrentAnimator.addUpdateListener(this);
        mCurrentAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mAnimationProgress = (float) animation.getAnimatedValue();
        invalidate();
    }

    public void setText(String text) {
        mText = text;
    }

    /**
     * Sets checkbox border, background and unchecked text colors
     */
    public void setColor(int color) {
        mFillColor = color;
        mStrokePaint.setColor(color);
        mFillPaint.setColor(color);
    }

    public void setCheckedTextColor(int color) {
        mCheckedTextColor = color;
    }
}
