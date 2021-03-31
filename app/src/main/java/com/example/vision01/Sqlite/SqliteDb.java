package com.example.vision01.Sqlite;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;

public class SqliteDb {
    public static SqliteDb sqliteDb;
    public static DbDevice dbDevice;
    public static SQLiteDatabase database;
    public static String DBFILE_PREFIX = "Database";

    public static SqliteDb getInstance(){
        if(sqliteDb == null){
            sqliteDb = new SqliteDb();
        }
        return sqliteDb;
    }
    private SqliteDb(){
        if(database == null)
            openDatabase();
        if(dbDevice == null){
            dbDevice = new DbDevice(this);
        }
    }

    public boolean openDatabase(){
        if(database == null){
            Log.e("openDatabase", "null이라 생성");
            File file = new File("/storage/emulated/0/Android/data/com.example.vision01/files/DatabaseUse.sqlite");
            String dbPath = file.getAbsolutePath();
            File dbFile = new File(dbPath);

            if(dbFile.exists() == false)    return false;
            Log.e("openDatabase", "생성 ok?");
            database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
        if(database == null) throw new SQLException();

        return true;
    }

    public void closeDatabase(){
        database.close();
        database = null;
    }

}
