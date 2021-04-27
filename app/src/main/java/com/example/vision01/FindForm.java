package com.example.vision01;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
//import android.graphics.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class FindForm extends AppCompatActivity {

    private Button findButton;
//    private SurfaceView surfaceView;
//    private SurfaceHolder surfaceHolder;
//    private Camera camera;

    SurfaceView pc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation_request);

//        surfaceView = (SurfaceView)findViewById(R.id.surfaceView_cam);
//        surfaceHolder = surfaceView.getHolder();
//        surfaceHolder.addCallback(surfaceListener);

        pc = findViewById(R.id.surfaceView_cam);

        //동적퍼미션 작업
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int permissionResult= checkSelfPermission(Manifest.permission.CAMERA);
            if(permissionResult== PackageManager.PERMISSION_DENIED){
                String[] permissions= new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,10);
            }
        }else{
            pc.setVisibility(View.VISIBLE);
        }

        findButton = (Button)findViewById(R.id.button_find);
        final ImageView arrow_image = (ImageView)findViewById(R.id.right_arrow);

        findButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                arrow_image.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate);
                arrow_image.startAnimation(animation);
            }
        });
    }

//    private SurfaceHolder.Callback surfaceListener = new SurfaceHolder.Callback() {
//
//        @Override//액티비티가 비활성 상태일 때 화면에 표시X
//        public void surfaceDestroyed(SurfaceHolder holder) {
//            camera.release();
//        }
//
//        @Override
//        public void surfaceCreated(SurfaceHolder holder) {
//            //카메라 객체를 받아와 카메라로부터 영상을 받을수있도록 초기화
//            camera = Camera.open();
//            try {
//                camera.setPreviewDisplay(holder);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//        @Override//카메라 객체에서 프리뷰 영상을 표시할 영역의 크기 설정
//        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//            Camera.Parameters parameters = camera.getParameters();
//            parameters.setPreviewSize(w, h);
//            camera.startPreview();
//        }
//    };
}