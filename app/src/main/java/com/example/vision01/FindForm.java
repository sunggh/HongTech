package com.example.vision01;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
//import android.graphics.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.Camera;
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
    private SurfaceView findView;
    private camPreview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rotation_request);

        findButton = (Button)findViewById(R.id.button_find);

        //preview = new camPreview(this);
        //setContentView(preview);

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

    public class camPreview extends Activity implements SurfaceHolder.Callback {
        private CameraDevice camera;
        private SurfaceView mCameraView;
        private SurfaceHolder mCameraHolder;
        private Camera mCamera;
        private Button mStart;
        private boolean recording = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mCameraView = (SurfaceView)findViewById(R.id.surfaceView_cam);

            init();
        }

        private void init() {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);

            mCameraHolder = mCameraView.getHolder();
            mCameraHolder.addCallback(this);
            mCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if(mCamera == null) {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                }
            } catch (IOException exception) {

            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            if(mCameraHolder.getSurface() == null) {
                return;
            }

            try {
                mCamera.stopPreview();
            } catch(Exception e) {

            }

            try {
                mCamera.setPreviewDisplay(mCameraHolder);
                mCamera.startPreview();
            } catch (Exception e) {

            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if(mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
    }
}