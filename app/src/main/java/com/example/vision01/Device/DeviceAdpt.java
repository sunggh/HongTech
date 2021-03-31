package com.example.vision01.Device;

import android.content.Context;
//import android.graphics.Camera;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;


import com.example.vision01.JoinForm;
import com.example.vision01.MainActivity;
import com.example.vision01.R;

import java.io.IOException;
import java.util.ArrayList;

public class DeviceAdpt extends BaseAdapter {
    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<Device> devices;
    Camera camera;
    SurfaceHolder mHolder=null;
    //-------------------------------------
    private Preview mPreview = null;

    class Preview extends SurfaceView implements SurfaceHolder.Callback {

        Camera mCamera;

        public Preview(Context context) {
            super(context);

            mContext = context;
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //카메라 객체를 받아와 카메라로부터 영상을 받을수있도록 초기화
            mCamera = Camera.open();
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException exception) {
                mCamera.release();
                mCamera = null;
            }
        }

        @Override//액티비티가 비활성 상태일 때 화면에 표시X
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.stopPreview();
            mCamera = null;
        }

        @Override//카메라 객체에서 프리뷰 영상을 표시할 영역의 크기 설정
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(w, h);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }
    //-------------------------------------

    public DeviceAdpt(Context context, ArrayList<Device> devices){
        this.mContext = context;
        this.devices  = devices;
        mLayoutInflater = LayoutInflater.from(mContext);
        mPreview = new Preview(this.mContext);
        //setContentView(mPreview);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Device getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.device_list_view_item, null);

        Button btnDeviceInfo = (Button)view.findViewById(R.id.btn_device_info);
        Switch isTheftMode = (Switch) view.findViewById(R.id.switch_is_theft_mode);

        btnDeviceInfo.setText(devices.get(position).name + " ( " + devices.get(position).serialNum + " )");

        btnDeviceInfo.setOnClickListener(new View.OnClickListener(){
            // list item을 클릭 했을때 나올 페이지 또는 기능을 추가하는 곳
            // 다음 액티비티 (카메라)
            @Override
            public void onClick(View v) {

                Toast.makeText(mContext,
                        getItem(position).getName(),
                        Toast.LENGTH_SHORT).show();

                // mPreview.surfaceCreated(mHolder);

            }
        });
        // 체크 이벤트
        // isTheftMode.setOnCheckedChangeListener();

        isTheftMode.setChecked(devices.get(position).isTheftMode);

        return view;
    }
}
