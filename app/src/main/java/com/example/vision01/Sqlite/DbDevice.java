package com.example.vision01.Sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.vision01.Device.Device;

import java.util.ArrayList;

public class DbDevice {
    private SqliteDb sqliteDb;
    private static SQLiteDatabase database;
    private Cursor cursor;

    public DbDevice(SqliteDb sqliteDb){
        this.sqliteDb = sqliteDb;
        database = sqliteDb.database;
        if(database == null){
            Log.e("Db","database nullnullnull!!!!!!!!");
        }
    }

    public boolean addDevice(Device device){
        if(database == null)    return false;
        try {
            Log.e("Db","Add Device!!!");
            ContentValues row = new ContentValues();
            row.put("name", device.getName());
            row.put("serialNum", device.getSerialNum());
            row.put("isTheftMode", device.isTheftMode());

            database.insertOrThrow("tbl_device", null, row);
        }catch(SQLiteException e)
        {
            Log.e("Db",e.toString());
            sqliteDb.closeDatabase();
            return false;
        }
        finally
        {
            sqliteDb.openDatabase();
        }
        return true;
    }
    public static boolean removeDevice(Device device){
        if(database == null)    return false;

        try{
            database.delete("tbl_device", "serialNum = ?", new String[]{device.getSerialNum()});
            Log.e("Db","DELETED : " + device.toString());
            return true;
        }catch(SQLiteException e) {
            Log.e("Db", e.toString());
        }
        return false;

    }
    public boolean getIsTheftMode(Device device){
        if(database == null)    return false;

        String query = "SELECT * FROM tbl_device";

        boolean isTheftMode = false;

        cursor = database.rawQuery(query, null);
        cursor.moveToFirst();

        int count = cursor.getCount() - 1;
        for(int i = 0; i < count; i++){
            isTheftMode = cursor.getInt(cursor.getColumnIndexOrThrow("isTheftMode")) == 1;

            cursor.moveToNext();
        }
        cursor.close();
        return isTheftMode;
    }

    public boolean updateName(Device device){
        if(database == null)    return false;

        try{
            ContentValues values = new ContentValues();

            values.put("name", device.getName());

            database.update("tbl_device",values,"serialNum = ? ", new String[]{device.getSerialNum()});

            return true;
        }catch(SQLiteException e) {
            Log.e("Db", e.toString());
        }
        return false;
    }

    public boolean updateIsTheftMode(Device device){
        if(database == null)    return false;

        try{
            ContentValues values = new ContentValues();

            values.put("isTheftMode", device.isTheftMode());

            database.update("tbl_device",values,"serialNum = ? ", new String[]{device.getSerialNum()});

            return true;
        }catch(SQLiteException e) {
            Log.e("Db", e.toString());
        }
        return false;
    }
    public ArrayList<Device> getDevices(){
        ArrayList<Device> devices = new ArrayList<Device>();

        if(database == null){
            Log.e("Db", "DB NULL!!!!!!!");
            return null;
        }
        String query = "SELECT * FROM tbl_device";

        cursor = database.rawQuery(query, null);
        cursor.moveToFirst();

        int count = cursor.getCount() - 1;
        for(int i = 0; i < count + 1; i++){
            Device device = new Device(
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("serialNum")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("isTheftMode"))==1);
            devices.add(device);
            cursor.moveToNext();
        }

        cursor.close();
        return devices;
    }
}