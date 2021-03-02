package com.example.vision01.Device;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vision01.JoinForm;
import com.example.vision01.R;

import java.util.ArrayList;

public class DeviceAdpt extends BaseAdapter {
    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<Device> devices;

    public DeviceAdpt(Context context, ArrayList<Device> devices){
        this.mContext = context;
        this.devices  = devices;
        mLayoutInflater = LayoutInflater.from(mContext);
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
            }
        });
        // 체크 이벤트
        // isTheftMode.setOnCheckedChangeListener();

        isTheftMode.setChecked(devices.get(position).isTheftMode);

        return view;
    }
}
