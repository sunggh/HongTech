package com.example.vision01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.vision01.Device.Device;
import com.example.vision01.Device.DeviceAdpt;
import com.example.vision01.Device.DeviceManager;
import com.example.vision01.Sqlite.DbDevice;
import com.example.vision01.Sqlite.SqliteDb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DeviceListForm extends AppCompatActivity {
    SqliteDb sqliteDb = SqliteDb.getInstance();
    ArrayList<Device> devices;
    ListView lvDeviceList;
    Button addDeviceButton;
    Button deleteDeviceButton;
    DeviceAdpt deviceAdpt;

    //private DeviceAdpt.Preview mPreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_list_form);

        InitializeDevices();
        initControl();


       // mPreview = new DeviceAdpt.Preview(this);

    }
    @Override
    protected void onResume() {
        super.onResume();
        InitializeDevices();
        initControl();
    }


    public void initControl(){
        lvDeviceList = (ListView)findViewById(R.id.listView);
        addDeviceButton = (Button)findViewById(R.id.add_btn);
        deleteDeviceButton = (Button)findViewById(R.id.del_btn);
        deviceAdpt = new DeviceAdpt(this, devices);

        lvDeviceList.setAdapter(deviceAdpt);

        // add Device button 이벤트
//        addDeviceButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                addDevice(new Device("test","sn.0000000", false));
//                deviceAdpt.notifyDataSetChanged();
//            }
//        });

        addDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddDeviceForm.class);
                startActivity(intent);
            }
        });

        // delete Device button 이벤트
        deleteDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //deleteDevice(0);
                //deviceAdpt.notifyDataSetChanged();
                int count, checked ;
                count = deviceAdpt.getCount() ;

                if (count > 0) {
                    // 현재 선택된 아이템의 position 획득.
                    //checked = lvDeviceList.getCheckedItemPosition();
                    checked = 0;
                    if (checked > -1 && checked < count) {
                        Device chkDevice = devices.get(checked);
                        // 아이템 삭제
                        //items.remove(checked) ;
                        DbDevice.removeDevice(chkDevice);

                        // listview 선택 초기화.
                        lvDeviceList.clearChoices();

                        // listview 갱신.
                        deviceAdpt.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"삭제할 제품을 선택하세요.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        lvDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //lvDeviceList.item
            }
        });

    }
/*
    public void addDevice(Device device){
        devices.add(device);
    }

    public void deleteDevice(int position){
        devices.remove(position);
    }
*/
    // 추후에 내부 디비 연동해서 불러오기.
    public void InitializeDevices()
    {

        DeviceManager.devices = sqliteDb.dbDevice.getDevices();
        devices = DeviceManager.devices;

        /*devices.add(new Device("차키", "sn.1234567",true));
        devices.add(new Device("에어팟", "sn.1234568",true));
        devices.add(new Device("지갑", "sn.1234569",false));*/
        Log.e("sql log", "devices : " + DeviceManager.devices);
    }

}