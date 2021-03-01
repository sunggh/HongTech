package com.example.vision01.Device;

public class Device {
    String name;
    String serialNum;
    boolean isTheftMode;

    public Device(String name, String serialNum, boolean isTheftMode){
        this.name = name;
        this.serialNum = serialNum;
        this.isTheftMode = isTheftMode;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public boolean isTheftMode() {
        return isTheftMode;
    }

    public void setTheftMode(boolean theftMode) {
        isTheftMode = theftMode;
    }
}
