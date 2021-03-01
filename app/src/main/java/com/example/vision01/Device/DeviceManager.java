package com.example.vision01.Device;

import java.util.ArrayList;

public class DeviceManager {
    ArrayList<Device> devices;
    Device selectedDevice;

    public DeviceManager(ArrayList<Device> devices, Device selectedDevice) {
        this.devices = devices;
        this.selectedDevice = selectedDevice;
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
