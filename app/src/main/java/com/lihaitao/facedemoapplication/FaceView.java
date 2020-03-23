package com.lihaitao.facedemoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class FaceView extends View {

    private long currentTime = 0;
    private List<PositionFilter> filters;

    private int width;
    private int height;

    public FaceView(Context context) {
        this(context, null);
    }

    public FaceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        filters = new ArrayList<>();
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFilter(canvas);
    }

    public void drawFilter(Canvas canvas) {
        if (canvas != null) {
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(paint);
        }
        if (filters != null && filters.size() > 0) {
            for (int i = 0; i < filters.size(); i++) {
                if (filters.get(i) != null) {
                    drawFilter(filters.get(i), canvas);
                }
            }
        }
    }

    private void drawFilter(PositionFilter filter, Canvas canvas) {
        int top = (int) filter.getTop();
        int bottom = (int) filter.getBottom();
        int left = (int) filter.getLeft();
        int right = (int) filter.getRight();
        if (filter.getResId() <= 0 || (bottom - top) <= 0 || (right - left) <= 0) {
            return;
        }
        if (canvas != null) {
            Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), filter.getResId());
            int lBmp = 0;
            int rBmp = bmp.getWidth();
            int tBmp = 0;
            int bBmp = bmp.getHeight();
            float precentX = bmp.getWidth() / (right - lBmp);
            float precentY = bmp.getHeight() / (bottom - top);
            if (top < 0) {
                tBmp = (int) (Math.abs(top) * precentY);
                top = 0;
            }
            if (left < 0) {
                lBmp = (int) (Math.abs(left) * precentX);
                left = 0;
            }
            if (right > width) {
                rBmp = bmp.getWidth() - (int) ((right - width) * precentX);
                right = width;
            }
            if (bottom > height) {
                bBmp = bmp.getHeight() - (int) ((bottom - height) * precentY);
                bottom = height;
            }
            Log.e("lhtF", "left:" + left + ",top:" + top + ",right:" + right + ",bottom:" + bottom);
            Rect src = new Rect(lBmp, tBmp, rBmp, bBmp);
            Rect dst = new Rect(left, top, right, bottom);
            canvas.drawBitmap(bmp, src, dst, null);
        }
    }

    public void drawFace(List<PositionFilter> positionFilters) {
        if (positionFilters != null && positionFilters.size() > 0) {
            currentTime = System.currentTimeMillis();
            if (filters.size() == positionFilters.size()) {
                boolean isNeed = false;
                for (int i = 0; i < filters.size(); i++) {
                    if (!(filters.get(i).getResId() == positionFilters.get(i).getResId()
                            && Math.abs(filters.get(i).getBottom() - positionFilters.get(i).getBottom()) < 5
                            && Math.abs(filters.get(i).getLeft() - positionFilters.get(i).getLeft()) < 5
                            && Math.abs(filters.get(i).getTop() - positionFilters.get(i).getTop()) < 5
                            && Math.abs(filters.get(i).getRight() - positionFilters.get(i).getRight()) < 5)) {
                        isNeed = true;
                    }
                }
                if (isNeed) {
                    filters.clear();
                    filters.addAll(positionFilters);
                    invalidate();
                }
            } else {
                filters.clear();
                filters.addAll(positionFilters);
                invalidate();
            }
        } else {
            if (filters.size() > 0) {
                if (System.currentTimeMillis() - currentTime > 500) {
                    filters.clear();
                    invalidate();
                }
            }
        }
    }

}
