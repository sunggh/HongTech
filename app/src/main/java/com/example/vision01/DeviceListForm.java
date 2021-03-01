package com.example.vision01;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.vision01.Device.Device;
import com.example.vision01.Device.DeviceAdpt;

import java.util.ArrayList;

public class DeviceListForm extends AppCompatActivity {
    ArrayList<Device> devices;

    ListView lvDeviceList;
    Button addDeviceButton;
    Button deleteDeviceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list_form);
        InitializeDevices();
        initControl();


    }

    public void initControl(){
        lvDeviceList = (ListView)findViewById(R.id.listView);
        addDeviceButton = (Button)findViewById(R.id.add_btn);
        deleteDeviceButton = (Button)findViewById(R.id.del_btn);
        final DeviceAdpt deviceAdpt = new DeviceAdpt(this, devices);

        lvDeviceList.setAdapter(deviceAdpt);


        // add Device button 이벤트
        addDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addDevice(new Device("test","sn.0000000", false));
                deviceAdpt.notifyDataSetChanged();
            }
        });

        // delete Device button 이벤트
        deleteDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deleteDevice(0);
                deviceAdpt.notifyDataSetChanged();
            }
        });

        lvDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // list item을 클릭 했을때 나올 페이지 또는 기능을 추가하는 곳
                // 다음 액티비티 (카메라)
                Toast.makeText(getApplicationContext(),
                        deviceAdpt.getItem(position).getName(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void addDevice(Device device){
        devices.add(device);
    }

    public void deleteDevice(int position){
        devices.remove(position);
    }

    // 추후에 내부 디비 연동해서 불러오기.
    public void InitializeDevices()
    {
        devices = new ArrayList<Device>();

        devices.add(new Device("차키", "sn.1234567",true));
        devices.add(new Device("에어팟", "sn.1234568",true));
        devices.add(new Device("지갑", "sn.1234569",false));
    }
}