package com.lihaitao.facedemoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.hxrainbow.facedetect.FaceDetectUtil;

import java.util.ArrayList;
import java.util.List;

public class FaceCameraView extends FrameLayout implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;

    private Camera camera;
    private float screenProp = 1.0f;

    private int pWidth = 320, pHeight = 480;

    private SurfaceView faceView;
    private Object object;
    private HandlerThread handlerThread;
    private Handler handler;

    private int resId;
    private float offX;
    private float offY;
    private float precentW;
    private float precentH;

    private int height;
    private int width;

    public FaceCameraView(@NonNull Context context) {
        this(context, null);
    }

    public FaceCameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (surfaceView != null) {
            float widthSize = surfaceView.getMeasuredWidth();
            float heightSize = surfaceView.getMeasuredHeight();
            screenProp = heightSize / widthSize;
        } else {
            screenProp = Util.getScreenHeight() / Util.getScreenWidth();
        }
    }

    public void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.face_camera_view, this);
        surfaceView = view.findViewById(R.id.sv_camera);
        faceView = view.findViewById(R.id.fc_face);
        faceView.setZOrderOnTop(true);
        faceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(this);

        object = new Object();
        handlerThread = new HandlerThread("draw");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1000) {
                    synchronized (object) {
                        if (msg.obj != null) {
                            drawFace((List<PositionFilter>) msg.obj);
                        } else {
                            drawFace(null);
                        }
                    }
                }
            }
        };

        addFace(R.drawable.face_ic, 0.0f, 0.0f, 1.0f, 1.0f);

    }

    private void createCamera() {
        if (camera == null) {
            camera = Camera.open(0);
        }
    }

    private void openCamera(SurfaceHolder holder) {
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size previewSize = CameraParamUtil.getInstance().getPreviewSize(parameters.getSupportedPreviewSizes(), 1000, screenProp);
                Camera.Size pictureSize = CameraParamUtil.getInstance().getPictureSize(parameters.getSupportedPictureSizes(), 1200, screenProp);
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
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder);
                camera.setDisplayOrientation(0);

                pHeight = previewSize.height;
                pWidth = previewSize.width;
                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {

                        Message message = Message.obtain();
                        message.what = 1000;

                        FaceDetectUtil.Detect(data, pWidth, pHeight);
                        int lenth = FaceDetectUtil.GetResultLength();
                        Log.e("lht", "leght:" + lenth);
                        if (lenth > 0) {
                            int num = FaceDetectUtil.GetResultByIndex(0);
                            if (num > 0) {
                                List<PositionFilter> positionFilters = new ArrayList<>();
                                for (int i = 0; i < num; i++) {
                                    int right = width - FaceDetectUtil.GetResultByIndex(1 + i * 4) * width / pWidth;
                                    int top = FaceDetectUtil.GetResultByIndex(2 + i * 4) * height / pHeight;
                                    int left = width - FaceDetectUtil.GetResultByIndex(3 + i * 4) * width / pWidth;
                                    int bottom = FaceDetectUtil.GetResultByIndex(4 + i * 4) * height / pHeight;
                                    Log.e("lht", i + "---left:" + left + ",top:" + top + ",right:" + right + ",bottom:" + bottom);
                                    PositionFilter filter = new PositionFilter(resId, offX, offY, precentW, precentH);
                                    filter.setFacePosition(top, bottom, left, right);
                                    positionFilters.add(filter);
                                }
                                message.obj = positionFilters;
                            } else {
                                message.obj = null;
                            }
                        } else {
                            message.obj = null;
                        }

                        handler.removeCallbacksAndMessages(null);
                        handler.sendMessage(message);

                    }
                });
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

    public void addFace(int resId, float offX, float offY, float precentW, float precentH) {
        this.resId = resId;
        this.offX = offX;
        this.offY = offY;
        this.precentW = precentW;
        this.precentH = precentH;
    }

    private void drawFace(List<PositionFilter> filters) {
        if (surfaceView != null) {
            if (!surfaceView.getHolder().getSurface().isValid()) {
                return;
            }
            Canvas canvas = surfaceView.getHolder().lockCanvas();
            if (canvas == null) {
                return;
            }
            if (filters != null && filters.size() > 0) {
                for (int i = 0; i < filters.size(); i++) {
                    int top = (int) filters.get(i).getTop();
                    int bottom = (int) filters.get(i).getBottom();
                    int left = (int) filters.get(i).getLeft();
                    int right = (int) filters.get(i).getRight();
                    if (filters.get(i).getResId() <= 0 || (bottom - top) <= 0 || (right - left) <= 0) {
                        return;
                    }
                    Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), filters.get(i).getResId());
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
                surfaceView.getHolder().unlockCanvasAndPost(canvas);
            } else {
                Paint paint = new Paint();
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                canvas.drawPaint(paint);
                surfaceView.getHolder().unlockCanvasAndPost(canvas);
            }

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        createCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.height = height;
        this.width = width;
        openCamera(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }

}
