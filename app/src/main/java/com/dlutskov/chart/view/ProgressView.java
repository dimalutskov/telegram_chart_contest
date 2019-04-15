package com.dlutskov.chart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.dlutskov.chart.AppDesign;
import com.dlutskov.chart_lib.utils.ChartUtils;

public class ProgressView extends View {

    private SpinnerDrawable spinnerDrawable;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float bagelPaintThickness = ChartUtils.getPixelForDp(context, 4);
        int bagelPaintColor = AppDesign.getZoomOutText(AppDesign.Theme.DAY);

        spinnerDrawable = new SpinnerDrawable(this, bagelPaintColor, bagelPaintThickness);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Square view size
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh)
            spinnerDrawable.onParentSizeChanged(w, h);
        super.onSizeChanged(w, h, oldw, oldh);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        spinnerDrawable.draw(canvas);
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        super.invalidateDrawable(drawable);
        invalidate();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (spinnerDrawable != null) {
            if (visibility == View.VISIBLE) {
                spinnerDrawable.showSpinner();
            } else {
                spinnerDrawable.hideSpinner();
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != VISIBLE && spinnerDrawable != null) {
            spinnerDrawable.hideSpinner();
        }
    }
}
