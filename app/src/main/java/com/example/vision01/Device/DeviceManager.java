package com.example.vision01.Device;

import com.example.vision01.Sqlite.SqliteDb;

import java.util.ArrayList;

public class DeviceManager {
    public static DeviceManager deviceManager = null;
    public static ArrayList<Device> devices;
    public static Device selectedDevice;

    public static DeviceManager getInstance(){
        if(deviceManager == null){
            deviceManager = new DeviceManager();
        }
        return deviceManager;
    }

    public void addDevice(Device device){
        devices.add(device);
    }

    public void setSelectedDevice(Device selectedDevice){
        this.selectedDevice = selectedDevice;
    }

    public Device getSelectedDevice(){
        return selectedDevice;
    }

    public void getDevices(){

    }
    public void setDevices(){

    }
}
