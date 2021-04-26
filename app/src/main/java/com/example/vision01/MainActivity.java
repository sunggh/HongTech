package com.example.vision01;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.vision01.Utils.Preferences;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter btAdapter;
    private final static int REQUEST_ENABLE_BT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLocationPermissions();
        enableBluetooth();

        setContentView(R.layout.activity_main);
    }
    private void checkLocationPermissions(){
        // Get permission
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list,  1);
    }
    private void enableBluetooth(){
        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
}