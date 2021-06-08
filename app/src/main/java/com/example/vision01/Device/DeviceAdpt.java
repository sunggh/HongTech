package com.example.vision01.Device;

import android.content.Context;
//import android.graphics.Camera;
import android.content.Intent;
import android.hardware.Camera;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.vision01.DeviceListForm;
import com.example.vision01.FindForm;
import com.example.vision01.R;
import com.example.vision01.Sqlite.SqliteDb;
import com.example.vision01.TheftModeService;

import java.io.IOException;
import java.util.ArrayList;

public class DeviceAdpt extends BaseAdapter {
    SqliteDb sqliteDb = SqliteDb.getInstance();
    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<Device> devices;
    public TextView menu;
    Camera camera;
    SurfaceHolder mHolder=null;

    public DeviceAdpt(Context context, ArrayList<Device> devices){
        this.mContext = context;
        this.devices  = devices;
        mLayoutInflater = LayoutInflater.from(mContext);
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
        // menu = (TextView)view.findViewById(R.id.popup_menu_text);
        btnDeviceInfo.setText(devices.get(position).name + " ( " + devices.get(position).serialNum + " )");
        isTheftMode.setChecked(devices.get(position).isTheftMode);

        btnDeviceInfo.setOnClickListener(new View.OnClickListener(){
            // list item을 클릭 했을때 나올 페이지 또는 기능을 추가하는 곳
            // 다음 액티비티 (카메라)
            @Override
            public void onClick(View v) {
                Device selectedDevice = new Device(devices.get(position).id,devices.get(position).name,devices.get(position).serialNum, false, position);
                Toast.makeText(mContext,
                        getItem(position).getName() + "( " + getItem(position).getSerialNum() + " )",
                        Toast.LENGTH_SHORT).show();
                boolean flag = ((DeviceListForm)DeviceListForm.mContext).ConfirmState(selectedDevice);
                if(flag)
                    DeviceListForm.dlf.getFindForm(getItem(position).getSerialNum());
            }
        });
        // 체크 이벤트
        isTheftMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Device selectedDevice = new Device(devices.get(position).id,devices.get(position).name,devices.get(position).serialNum, isChecked, position);
                Log.e("isTheft", isChecked + "," + devices.get(position).getID() + " " + devices.get(position).getName());
                //Log.e("isTheft", isChecked + "," + devices.get(position).isTheftMode());
                sqliteDb.dbDevice.updateIsTheftMode(selectedDevice);


                DeviceManager.devices = sqliteDb.dbDevice.getTheftDevices();
                TheftModeService.TMActivationDevices = DeviceManager.devices;
                /*
                //실행되고 있는 서비스가 있는지 확인 후
                //서비스 스타트
                if(!(TheftModeService.TMActivationDevices.isEmpty())){
                    if(((DeviceListForm)DeviceListForm.mContext).isServiceRunningCheck(TheftModeService.class))
                        ((TheftModeService)TheftModeService.mContext).onDestroy();
                    ((DeviceListForm)DeviceListForm.mContext).startService();
                }
                else{
                    if(((DeviceListForm)DeviceListForm.mContext).isServiceRunningCheck(TheftModeService.class))
                        ((TheftModeService)TheftModeService.mContext).onDestroy();
                }
                */
            }
        });

        return view;
    }

}

