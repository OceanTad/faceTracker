package com.lihaitao.facedemoapplication.gl;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.hxrainbow.facedetect.FaceDetectUtil;
import com.lihaitao.facedemoapplication.CameraParamUtil;
import com.lihaitao.facedemoapplication.R;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlCameraSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {

    private int textureID;
    private SurfaceTexture surfaceTexture;
    private CameraDrawer cameraDrawer;
    private ScreenDrawer screenDrawer;
    private StickerDrawer stickerDrawer;
    private DirectDrawer directDrawer;
    private Camera camera;

    private int pWidth = 320, pHeight = 480;
    private int sWidth = 320, sHeight = 480;
    private List<GlPositionFilter> filters;

    private int[] mTextures;
    private float[] mtx = new float[16];

    public GlCameraSurfaceView(Context context) {
        this(context, null);
    }

    public GlCameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        filters = new ArrayList<>();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

//        textureID = GlCameraUtil.createTextureID();
//        surfaceTexture = new SurfaceTexture(textureID);
//        surfaceTexture.setOnFrameAvailableListener(this);
//        directDrawer = new DirectDrawer(textureID, getContext());

        openCamera();
        mTextures = new int[1];
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        surfaceTexture = new SurfaceTexture(mTextures[0]);
        surfaceTexture.setOnFrameAvailableListener(this);
        cameraDrawer = new CameraDrawer(getContext());
        screenDrawer = new ScreenDrawer(getContext());
        stickerDrawer = new StickerDrawer(getContext(), R.drawable.face_ic);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        Log.e("lht", "width:" + i + ",height:" + i1);
        sWidth = i;
        sHeight = i1;
        startPreview(surfaceTexture);
        cameraDrawer.setSize(i, i1);
        screenDrawer.setSize(i, i1);
        stickerDrawer.setSize(i, i1);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {

//        GLES20.glClearColor(0, 0, 0, 0);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        surfaceTexture.updateTexImage(); //SurfaceTexture的关键方法
//        float[] mtx = new float[16];
//        surfaceTexture.getTransformMatrix(mtx); //SurfaceTexture的关键方法
//        directDrawer.draw(mtx);

        int textureId;
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mtx);
        cameraDrawer.setMatrix(mtx);
        textureId = cameraDrawer.onDrawFrame(mTextures[0]);
        if (filters != null && filters.size() > 0) {
            stickerDrawer.setFilters(filters.get(0));
            textureId = stickerDrawer.onDrawFrame(textureId);
            stickerDrawer.setFilters(filters.get(1));
            textureId = stickerDrawer.onDrawFrame(textureId);
        }
        screenDrawer.onDrawFrame(textureId);

    }

    private void openCamera() {
        if (camera == null) {
            camera = Camera.open(0);
        }
    }

    private void startPreview(SurfaceTexture surfaceTexture) {
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size previewSize = CameraParamUtil.getInstance().getPreviewSize(parameters.getSupportedPreviewSizes(), 1000, 1.0f);
                Camera.Size pictureSize = CameraParamUtil.getInstance().getPictureSize(parameters.getSupportedPictureSizes(), 1200, 1.0f);
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
                parameters.setJpegQuality(100);
                if (CameraParamUtil.getInstance().isSupportedFocusMode(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                if (CameraParamUtil.getInstance().isSupportedPictureFormats(parameters.getSupportedPictureFormats(), ImageFormat.JPEG)) {
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    parameters.setJpegQuality(100);
                }
                pHeight = previewSize.height;
                pWidth = previewSize.width;
                camera.setParameters(parameters);
                camera.setPreviewTexture(surfaceTexture);
                camera.setDisplayOrientation(0);
                camera.setPreviewCallback(this);
                camera.startPreview();
            } catch (Exception e) {

            }
        }
    }

    private void closeCamera() {
        if (camera != null) {
            try {
                camera.setPreviewCallback(null);
            } catch (Exception e) {
                Log.e("aliyun", "camera error : " + e.getClass() + "---" + e.getMessage());
            }
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        faceTrack(data);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        closeCamera();
        if (cameraDrawer != null) {
            cameraDrawer.release();
        }
        if (stickerDrawer != null) {
            stickerDrawer.release();
        }
        if (screenDrawer != null) {
            screenDrawer.release();
        }
    }

    private void faceTrack(byte[] data) {
        FaceDetectUtil.Detect(data, pWidth, pHeight);
        int lenth = FaceDetectUtil.GetResultLength();
        Log.e("lht", "leght:" + lenth);
        if (lenth > 0) {
            int num = FaceDetectUtil.GetResultByIndex(0);
            if (num > 0) {
                filters.clear();
                for (int i = 0; i < num; i++) {
                    int right = sWidth - FaceDetectUtil.GetResultByIndex(1 + i * 4) * sWidth / pWidth;
                    int top = FaceDetectUtil.GetResultByIndex(2 + i * 4) * sHeight / pHeight;
                    int left = sWidth - FaceDetectUtil.GetResultByIndex(3 + i * 4) * sWidth / pWidth;
                    int bottom = FaceDetectUtil.GetResultByIndex(4 + i * 4) * sHeight / pHeight;
                    Log.e("lhtF", i + "---left:" + left + ",top:" + top + ",right:" + right + ",bottom:" + bottom);
                    GlPositionFilter filter = new GlPositionFilter(0.0f, 0.0f, 1.0f, 1.0f);
                    filter.setFacePosition(top, bottom, left, right);
                    filters.add(filter);
                }
            } else {
                filters.clear();
            }
        } else {
            filters.clear();
        }
    }

}
