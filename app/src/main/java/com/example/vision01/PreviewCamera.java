package com.example.vision01;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

import com.example.vision01.Device.DeviceAdpt;

import java.io.IOException;

public class PreviewCamera extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder mHolder;
    //Context mContext;
    Camera mCamera;

    public PreviewCamera(Context context, AttributeSet attrs) {
        super(context, attrs);

        //mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //카메라 객체를 받아와 카메라로부터 영상을 받을수있도록 초기화
        mCamera = Camera.open(0); //후면카메라 0 전면카메라 1
        try {
            mCamera.setPreviewDisplay(mHolder);

            //카메라는 무조건 가로방향임
            //프리뷰를 90도 회전
            mCamera.setDisplayOrientation(90);
        } catch (IOException exception) {

        }
    }

    @Override//카메라 객체에서 프리뷰 영상을 표시할 영역의 크기 설정
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setPreviewSize(w, h);
//        mCamera.setParameters(parameters);
//        mCamera.startPreview();
        mCamera.startPreview();
    }

    @Override//액티비티가 비활성 상태일 때 화면에 표시X
    public void surfaceDestroyed(SurfaceHolder holder) {
        //미리보기 종료
        mCamera.stopPreview();

        //카메라 닫기
        mCamera.release();
        mCamera= null;
    }



//        private CameraDevice camera;
//        private SurfaceView mCameraView;
//        private SurfaceHolder mCameraHolder;
//        private Camera mCamera;
//
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            mCameraView = (SurfaceView)findViewById(R.id.surfaceView_cam);
//
//            init();
//        }
//
//        private void init() {
//            mCamera = Camera.open();
//            mCamera.setDisplayOrientation(90);
//
//            mCameraHolder = mCameraView.getHolder();
//            mCameraHolder.addCallback(this);
//            mCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        }
//
//        public void surfaceCreated(SurfaceHolder holder) {
//            try {
//                if(mCamera == null) {
//                    mCamera.setPreviewDisplay(holder);
//                    mCamera.startPreview();
//                }
//            } catch (IOException exception) {
//
//            }
//        }
//
//        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//            if(mCameraHolder.getSurface() == null) {
//                return;
//            }
//
//            try {
//                mCamera.stopPreview();
//            } catch(Exception e) {
//
//            }
//
//            try {
//                mCamera.setPreviewDisplay(mCameraHolder);
//                mCamera.startPreview();
//            } catch (Exception e) {
//
//            }
//        }
//
//        public void surfaceDestroyed(SurfaceHolder holder) {
//            if(mCamera != null) {
//                mCamera.stopPreview();
//                mCamera.release();
//                mCamera = null;
//            }
//        }
}
