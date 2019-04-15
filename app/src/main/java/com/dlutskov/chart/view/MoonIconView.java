package com.dlutskov.chart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.TextView;

/**
 * Simple moon view which is drawn on the canvas as a two circles
 */
public class MoonIconView extends TextView {

    private Paint mMainPaint;
    private Paint mBackgroundPaint;

    public MoonIconView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mMainPaint = new Paint();
        mMainPaint.setAntiAlias(true);
        mMainPaint.setStyle(Paint.Style.FILL);
        mMainPaint.setColor(Color.WHITE);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.WHITE);

        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = getWidth();
        int halfSize = size / 2;

        canvas.drawCircle(halfSize, halfSize, halfSize, mMainPaint);
        canvas.drawCircle(size * 0.15f, halfSize, halfSize, mBackgroundPaint);
//        canvas.drawCircle(size * 0.75f, size * 0.3f, halfSize * 0.9f, mBackgroundPaint);
    }

    public void setMainColor(int color) {
        mMainPaint.setColor(color);
        invalidate();
    }

    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
        invalidate();
    }

}
