package com.example.vision01;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vision01.Device.Device;
import com.example.vision01.Device.DeviceManager;
import com.example.vision01.Sqlite.SqliteDb;


public class AddDeviceForm extends AppCompatActivity {
    TextView name;
    TextView sn;
    Button confirmButton;
    Button cancelButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_form);
        initControl();
    }
    public void initControl() {
        name = (TextView)findViewById(R.id.device_name);
        sn = (TextView)findViewById(R.id.device_sn);
        confirmButton = (Button) findViewById(R.id.confirm_btn);
        cancelButton = (Button) findViewById(R.id.cancel_btn);

        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String device_name = name.getText().toString();
                String device_sn = sn.getText().toString();
                Device device = new Device(device_name,device_sn, false);

                SqliteDb.dbDevice.addDevice(device);
                Log.e("DB",SqliteDb.dbDevice.getDevices().toString());

                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {


                finish();
            }
        });
    }

}
