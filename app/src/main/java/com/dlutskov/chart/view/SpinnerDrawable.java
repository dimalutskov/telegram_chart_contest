package com.dlutskov.chart.view;


import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.dlutskov.chart_lib.utils.ChartUtils;

/**
 * The main target of this class is to allow progress spinner directly on {@link View}
 * without adding additional {@link android.view.ViewGroup}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SpinnerDrawable extends DrawableWithPosition {
    private final boolean useParentSize;
    private ValueAnimator valueAnimator;
    private float sweepAngle;
    private float startAngle;
    private RectF bagelOuterBounds;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private final Paint bagelPaint;
    private final float bagelPaintThickness;

    /**
     * CTOR
     * @param parentView on this {@link View} will be draw progress
     * @param bagelColor progress bagel color
     * @param bagelThickness progress bagel thickness
     * @param bagelPaint progress bagel paint
     * @param width drawable width
     * @param height drawable height
     */
    private SpinnerDrawable(View parentView, int bagelColor, float bagelThickness, Paint bagelPaint, int width, int height) {
        setCallback(parentView);
        this.bagelPaintThickness = bagelThickness <= 0 ?
                ChartUtils.getPixelForDp(parentView.getContext(), 4) : bagelThickness;
        if (bagelPaint == null) {
            this.bagelPaint = new Paint();
            this.bagelPaint.setAntiAlias(true);
            this.bagelPaint.setStrokeCap(Paint.Cap.ROUND);
            this.bagelPaint.setStyle(Paint.Style.STROKE);
            this.bagelPaint.setColor(bagelColor);
            this.bagelPaint.setStrokeWidth(this.bagelPaintThickness);
        } else {
            this.bagelPaint = bagelPaint;
        }
        if (width == 0 || height == 0) {
            this.useParentSize = true;
            this.width = parentView.getWidth();
            this.height = parentView.getHeight();
        } else {
            this.width = width;
            this.height = height;
            this.useParentSize = false;
        }
        updateBitmapAndBagelBounds(width, height);
    }

    public SpinnerDrawable(View parentView, int bagelColor, float bagelThickness) {
        this(parentView, bagelColor, bagelThickness, null, 0, 0);
    }

    public SpinnerDrawable(View parentView, int bagelColor, int bagelThickness, int width, int height) {
        this(parentView, bagelColor, bagelThickness, null, width, height);
    }

    public SpinnerDrawable(View parentView, int bagelColor, int width, int height) {
        this(parentView, bagelColor, 0, null, width, height);
    }

    public SpinnerDrawable(View parent, int bagelColor) {
        this(parent, bagelColor, 0, 0);
    }

    /**
     * If you drawable size is depends on parent view size your should call it
     * from {@link View#onSizeChanged(int, int, int, int)} method.
     * @param w parent view new width
     * @param h parent view new height
     */
    public void onParentSizeChanged(int w, int h) {
        if (useParentSize) {
            width = w;
            height = h;
        }
        updateBitmapAndBagelBounds(width, height);
    }

    /**
     * Updates bagel outer bounds and recreate new bitmap canvas according to spinner width and height.
     * @param width progress spinner width
     * @param height progress spiner height
     */
    private void updateBitmapAndBagelBounds(int width, int height) {
        if (width < 1 || height < 1)
            return;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        bitmapCanvas = new Canvas(bitmap);
        bagelOuterBounds = new RectF(
                0 + bagelPaintThickness,
                0 + bagelPaintThickness,
                width - bagelPaintThickness,
                height - bagelPaintThickness
        );
    }

    /**
     * Call it when you wish to start show progress spinner.
     * Starts infinite {@link ValueAnimator}
     */
    public void showSpinner() {
        if (valueAnimator != null) return;

        valueAnimator = ValueAnimator.ofFloat(0f, 1.5f);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(animation -> updateProgress((float) animation.getAnimatedValue()));

        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.start();
    }

    /**
     * Call it when you wish to hide progress spinner.
     */
    public void hideSpinner() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
        sweepAngle = 0;
        startAngle = 0;
        invalidateSelf();
    }

    /**
     * Changes sweep angel and calls invalidate for redraw spinner progress.
     * @param progress progress value
     */
    private void updateProgress(float progress) {
        if (progress <= 0.5f) {
            sweepAngle = 360 * progress;
        } else {
            startAngle = 270 + 360 * (progress - 0.5f);
            if (startAngle >= 360)
                startAngle -= 360;
            if (progress >= 1.0f)
                sweepAngle = 180 - (360 * (progress - 1.0f));
        }

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas, int dx, int dy) {
        if (width < 1 || height < 1)
            return;
        canvas.save();
        canvas.translate(dx, dy);

        if (sweepAngle > 0f)
            canvas.drawArc(bagelOuterBounds, startAngle, sweepAngle, false, bagelPaint);

        bitmapCanvas.save();
        bitmapCanvas.translate(dx, dy);
        bitmapCanvas.drawBitmap(bitmap, 0, 0, null);
        bitmapCanvas.restore();

        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        bagelPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        bagelPaint.setColorFilter(colorFilter);
    }
}