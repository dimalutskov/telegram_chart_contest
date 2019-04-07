package com.dlutskov.chart.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.Checkable;
import android.widget.TextView;

import com.dlutskov.chart_lib.utils.ChartUtils;

/**
 * Custom checkbox with own checked/unchecked animation
 */
public class ChartCheckBox extends TextView implements Checkable, ValueAnimator.AnimatorUpdateListener {

    // Paint for checkbox icon border
    private Paint mStrokePaint;
    // Paint for checkbox icon filled background
    private Paint mFillPaint;
    // Paint for drawing checkmark
    private Paint mCheckMarkPaint;

    private RectF mCheckBoxRect = new RectF();

    private Rect mTextRect = new Rect();

    private Path mCheckMarkPath = new Path();

    // Checkbox icon rounded corners radius
    private int mCornerRadius;
    // Checkbox icon border and checkmark stroke width
    private int mStrokeWidth;
    // Padding between checkbox icon and text
    private int mTextPadding;

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
        mCornerRadius = ChartUtils.getPixelForDp(getContext(), 2);
        mStrokeWidth = ChartUtils.getPixelForDp(getContext(), 2);
        mTextPadding = ChartUtils.getPixelForDp(getContext(), 16);
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
    }

    /**
     * Sets checkbox icon's border and background colors
     */
    public void setColor(int color) {
        mStrokePaint.setColor(color);
        mFillPaint.setColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredHeight() + getMeasuredWidth() + mTextPadding;
        setMeasuredDimension(width, getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = canvas.getHeight();
        int iconSize = height - mStrokeWidth * 2;

        // Draw icon borders
        mCheckBoxRect.set(mStrokeWidth, mStrokeWidth, iconSize, iconSize);
        canvas.drawRoundRect(mCheckBoxRect, mCornerRadius, mCornerRadius, mStrokePaint);

        // Draw filled background
        if (mAnimationProgress > 0) {
            int halfSize = iconSize / 2;
            float leftTop = mStrokeWidth + halfSize * (1 - mAnimationProgress);
            float rightBottom = halfSize + halfSize * mAnimationProgress;
            mCheckBoxRect.set(leftTop, leftTop, rightBottom, rightBottom);
            canvas.drawRoundRect(mCheckBoxRect, mCornerRadius, mCornerRadius, mFillPaint);
        }

        // Draw CheckMark
        float horizontalOffset = iconSize * 0.2f;
        float verticalOffset = iconSize * 0.25f;
        float offset = iconSize * 0.45f;
        mCheckMarkPath.reset();
        mCheckMarkPath.moveTo(mStrokeWidth + horizontalOffset, iconSize - offset);
        mCheckMarkPath.lineTo(offset, iconSize - verticalOffset);
        mCheckMarkPath.lineTo(iconSize - horizontalOffset, offset);
        mCheckMarkPaint.setAlpha((int) (255 * mAnimationProgress));
        canvas.drawPath(mCheckMarkPath, mCheckMarkPaint);

        // Draw text
        String text = getText().toString();
        getPaint().getTextBounds(text, 0, text.length(), mTextRect);
        int textY = height - (height - mTextRect.height()) / 2 - mStrokeWidth / 2;
        canvas.drawText(getText().toString(), height + mTextPadding, textY, getPaint());
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            mAnimationProgress = mChecked ? 1 : 0;
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
}
