package com.lihaitao.facedemoapplication.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.lihaitao.facedemoapplication.R;

public class StickerDrawer extends BaseDrawer {

    private int[] mTextureId;
    private Bitmap bitmap;
    private GlPositionFilter filter;

    public StickerDrawer(Context context, int resId) {
        super(context, R.raw.screen_vert, R.raw.screen_frag);
        bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        loadFOB();
        mTextureId = new int[1];
        OpenGlUtils.glGenTextures(mTextureId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public int onDrawFrame(int textureId) {
        if (bitmap == null || filter == null) {
            return textureId;
        }

        GLES20.glViewport(0, 0, screenWidth, screenHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glUseProgram(mProgramId);
        mGlVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);
        mGlTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        onDrawStick();

        return mFBOTextures[0];
    }

    private void onDrawStick() {
        //开启混合模式
        GLES20.glEnable(GLES20.GL_BLEND);
        //设置贴图模式
        // 1：src 源图因子 ： 要画的是源  (耳朵)
        // 2: dst : 已经画好的是目标  (从其他filter来的图像)
        //画耳朵的时候  GL_ONE:就直接使用耳朵的所有像素 原本是什么样子 我就画什么样子
        // 表示用1.0减去源颜色的alpha值来作为因子
        //  耳朵不透明 (0,0 （全透明）- 1.0（不透明）) 目标图对应位置的像素就被融合掉了 不见了
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //要绘制的位置和大小，贴纸是画在耳朵上的，直接锁定人脸坐标就可以
        GLES20.glViewport((int) filter.getStartX(), (int) filter.getStartY(), (int) filter.getWidth(), (int) filter.getHeight());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glUseProgram(mProgramId);
        mGlVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);
        mGlTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    protected void resetCoordinate() {
        mGlTextureBuffer.clear();
        float[] TEXTURE = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };
        mGlTextureBuffer.put(TEXTURE);
    }

    @Override
    public void release() {
        super.release();
        if (bitmap != null) {
            bitmap.recycle();
        }
    }

    public void setFilters(GlPositionFilter filter) {
        this.filter = filter;
    }

}
