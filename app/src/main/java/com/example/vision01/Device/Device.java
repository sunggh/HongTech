package com.example.vision01.Device;

public class Device {
    String id;
    String name;
    String serialNum;
    boolean isTheftMode;
    boolean hasTheftOccurs;
    int position;

    public Device(String name, String serialNum, boolean isTheftMode){
        this.name = name;
        this.serialNum = serialNum;
        this.isTheftMode = isTheftMode;
        hasTheftOccurs = false;
    }

    public Device(String id, String name, String serialNum, boolean isTheftMode){
        this.id = id;
        this.name = name;
        this.serialNum = serialNum;
        this.isTheftMode = isTheftMode;
        hasTheftOccurs = false;
    }
    public Device(String id, String name, String serialNum, boolean isTheftMode, int position){
        this.id = id;
        this.name = name;
        this.serialNum = serialNum;
        this.isTheftMode = isTheftMode;
        this.position = position;
        hasTheftOccurs = false;
    }

    public boolean hasTheftOccurs() {
        return hasTheftOccurs;
    }

    public void setTheftOccurs(boolean hasTheftOccurs) {
        this.hasTheftOccurs = hasTheftOccurs;
    }
    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
