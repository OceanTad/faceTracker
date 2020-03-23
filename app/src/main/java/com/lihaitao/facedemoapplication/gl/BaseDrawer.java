package com.lihaitao.facedemoapplication.gl;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BaseDrawer {

    protected int mVertexShaderId;
    protected int mFragShaderId;
    protected final FloatBuffer mGlVertexBuffer;
    protected final FloatBuffer mGlTextureBuffer;
    protected String mVertexShader;
    protected String mFragShader;
    protected int mProgramId;
    protected int vTexture;
    protected int vMatrix;
    protected int vPosition;
    protected int vCoord;

    protected int screenHeight;
    protected int screenWidth;
    protected int startX;
    protected int startY;

    protected int[] mFrameBuffers;
    protected int[] mFBOTextures;

    public BaseDrawer(Context context,int mVertexShaderId, int mFragShaderId) {
        this.mVertexShaderId = mVertexShaderId;
        this.mFragShaderId = mFragShaderId;
        mGlVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGlVertexBuffer.clear();

//        float[] VERTEXT = {
//                -1.0f, -1.0f,
//                1.0f, -1.0f,
//                -1.0f, 1.0f,
//                1.0f, 1.0f
//        };

        float[] VERTEXT = {
                -1.0f, 1.0f,
                1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f
        };

        mGlVertexBuffer.put(VERTEXT);
        mGlTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGlTextureBuffer.clear();

//        float[] TEXTURE = {
//                0.0f, 1.0f,
//                1.0f, 1.0f,
//                0.0f, 0.0f,
//                1.0f, 0.0f,
//        };

        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };

        mGlTextureBuffer.put(TEXTURE);

        init(context);

        resetCoordinate();

    }


    private void init(Context mContext) {
        mVertexShader = GlCameraUtil.readRawShaderFile(mContext, mVertexShaderId);
        mFragShader = GlCameraUtil.readRawShaderFile(mContext, mFragShaderId);
        mProgramId = GlCameraUtil.loadProgram(mVertexShader, mFragShader);
        vPosition = GLES20.glGetAttribLocation(mProgramId, "vPosition");
        vCoord = GLES20.glGetAttribLocation(mProgramId, "vCoord");
        vMatrix = GLES20.glGetUniformLocation(mProgramId, "vMatrix");
        vTexture = GLES20.glGetUniformLocation(mProgramId, "vTexture");
    }

    public void setSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        startX = 0;
        startY = 0;
    }

    public int onDrawFrame(int textureId) {
        GLES20.glViewport(startX, startY, screenWidth, screenHeight);
        GLES20.glUseProgram(mProgramId);
        mGlVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);
        mGlTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //传如的是GL_TEXTURE_2D类型
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return textureId;
    }

    protected void loadFOB() {
        if (mFrameBuffers != null) {
            destroyFrameBuffers();
        }
        //创建FrameBuffer
        mFrameBuffers = new int[1];
        GLES20.glGenFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);
        //穿件FBO中的纹理
        mFBOTextures = new int[1];
        OpenGlUtils.glGenTextures(mFBOTextures);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOTextures[0]);
        //指定FBO纹理的输出图像的格式 RGBA
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, screenWidth, screenHeight,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        //将fbo绑定到2d的纹理上
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFBOTextures[0], 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    protected void destroyFrameBuffers() {
        //删除fbo的纹理
        if (mFBOTextures != null) {
            GLES20.glDeleteTextures(1, mFBOTextures, 0);
            mFBOTextures = null;
        }
        //删除fbo
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
    }

    protected void resetCoordinate() {
    }

    public void release() {
        GLES20.glDeleteProgram(mProgramId);
    }

}
