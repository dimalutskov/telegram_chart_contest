package com.dlutskov.chart.view;

import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Implements base drawable methods and provides api to drawing {@link Drawable} objects in dx and dy
 * coordinates.
 */
public abstract class DrawableWithPosition extends Drawable {
    //the left point of drawable
    protected int dx;
    //the top point of drawable
    protected int dy;

    protected int height = -1;

    protected int width = -1;

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    @Override
    final public void draw(Canvas canvas) {
        saveDxDy(dx, dy);
        draw(canvas, dx, dy);
    }

    public abstract void draw(Canvas canvas, int dx, int dy);

    private void saveDxDy(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int getIntrinsicHeight() {
        return getHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return getWidth();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}

