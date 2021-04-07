package com.example.vision01.Sqlite;

import android.provider.BaseColumns;

public class DataBase {
    public static final class CreateDB implements BaseColumns {
        public static final String NAME = "name";
        public static final String SERIAL_NO = "sn";
        public static final String IS_THEFT_MODE = "isTheftMode";
        public static final String DEVICE_LIST = "device_List";
        public static final String _CREATE0 = "create table if not exists "+ DEVICE_LIST+" ("
                +_ID+" integer primary key autoincrement, "
                +NAME+" text, "
                +SERIAL_NO + " text, "
                +IS_THEFT_MODE+" boolean);";
    }
}
