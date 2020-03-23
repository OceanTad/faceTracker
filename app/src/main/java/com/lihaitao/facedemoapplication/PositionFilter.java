package com.lihaitao.facedemoapplication;

public class PositionFilter {

    private int resId;
    private float offX;
    private float offY;
    private float precentH;
    private float precentW;

    private float top;
    private float bottom;
    private float left;
    private float right;

    public PositionFilter(int resId, float offX, float offY, float precentW, float precentH) {
        this.resId = resId;
        this.offX = offX;
        this.offY = offY;
        this.precentW = precentW;
        this.precentH = precentH;
    }

    public void setFacePosition(float top, float bottom, float left, float right) {
        float height = (bottom - top) * precentH;
        float width = (right - left) * precentW;
        this.top = top + height * offY;
        this.bottom = bottom + height * offY;
        this.left = left + width * offX;
        this.right = right + width * offX;
//        this.top = top;
//        this.bottom = bottom;
//        this.left = left;
//        this.right = right;
    }

    public int getResId() {
        return resId;
    }

    public float getTop() {
        return top;
    }

    public float getBottom() {
        return bottom;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

}
