package com.lihaitao.facedemoapplication.gl;

public class GlPositionFilter {

    private float offX;
    private float offY;
    private float precentH;
    private float precentW;

    private float startX;
    private float startY;
    private float width;
    private float height;

    public GlPositionFilter(float offX, float offY, float precentW, float precentH) {
        this.offX = offX;
        this.offY = offY;
        this.precentW = precentW;
        this.precentH = precentH;
    }

    public void setFacePosition(float top, float bottom, float left, float right) {
        height = (bottom - top) * precentH;
        width = (right - left) * precentW;
        this.startY = top + height * offY;
        this.startX = left + width * offX;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

}
