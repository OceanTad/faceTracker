package com.lihaitao.facedemoapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;


import com.google.android.exoplayer2.ui.PlayerView;
import com.hxrainbow.facedetect.FaceDetectUtil;
import com.lihaitao.facedemoapplication.gl.GlCameraSurfaceView;

public class MainActivity extends AppCompatActivity {
    private boolean permission = false;

//    private FaceCameraView faceCameraView;
//    private CameraFaceView faceCameraView;
    private GlCameraSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FaceDetectUtil.Init(this);
        setContentView(R.layout.activity_main);
        checkPermission();
    }

    private void checkPermission() {
        String[] needPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        boolean result = true;
        for (int i = 0; i < needPermission.length; i++) {
            boolean permissionResult = checkCallingOrSelfPermission(needPermission[i]) == PackageManager.PERMISSION_GRANTED;
            result = result & permissionResult;
        }
        if (result) {
            initView();
        } else {
            ActivityCompat.requestPermissions(this, needPermission, 1001);
        }
    }

    private void initView() {
//        faceCameraView = findViewById(R.id.fcv_camera);
//        faceCameraView.initView();

//        PlayerView playerView = findViewById(R.id.player);
//        playerView.setUseController(false);
//        PlayerHelp.getInstance().initPlayer(this, playerView, "http://hjh-ys.wanbawanba.com/JYGY/task/yZweX7EBAdTZpBYcCPSHKynBsnkHrc45.mp4", new PlayerHelp.IPlayerStateListener() {
//            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//
//            }
//
//            @Override
//            public void onError() {
//
//            }
//        });
//        PlayerHelp.getInstance().start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission = true;
        if (requestCode == 1001) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    permission = false;
                    break;
                }
            }
            if (permission) {
                initView();
            } else {
                Log.e("lht", "**********");
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
//        surfaceView.bringToFront();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        FaceDetectUtil.UnInit();
        super.onDestroy();
    }

}
