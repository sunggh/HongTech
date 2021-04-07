package com.example.vision01.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import static android.provider.BaseColumns._ID;
import static com.example.vision01.Sqlite.DataBase.CreateDB.DEVICE_LIST;
import static com.example.vision01.Sqlite.DataBase.CreateDB.NAME;


public class SqlManager extends SQLiteOpenHelper {
    public static SqlManager sqlManager = null;
    private static final String DATABASE_NAME = "InnerDatabase(SQLite).db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase mDB;

    public static SqlManager getInstance(Context context){ // 싱글턴 패턴으로 구현하였다.
        if(sqlManager == null){
            sqlManager = new SqlManager(context);
        }

        return sqlManager;
    }

    private SqlManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // mDB = context.openOrCreateDatabase(DATABASE_NAME, context.MODE_PRIVATE,null);
        //mDB = this.getWritableDatabase();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("sql", "ON CREATE");
        mDB.execSQL(DataBase.CreateDB._CREATE0);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + DEVICE_LIST);
        onCreate(db);
    }

    public void close(){
        mDB.close();
    }

    //INSERT : 데이터 입력
    public long insertColumn(String name, String sn, Boolean isTM){
        ContentValues values = new ContentValues();
        values.put(DataBase.CreateDB.NAME, name);
        values.put(DataBase.CreateDB.SERIAL_NO, sn);
        values.put(DataBase.CreateDB.IS_THEFT_MODE, isTM);
        return mDB.insert(DEVICE_LIST, null, values);
    }
    //SELECT : 데이터 선택
    //1. 모든 데이블의 데이터 가져오기
    public Cursor getDevices(){
        return mDB.query(DEVICE_LIST, null, null, null, null, null, null);
    }

    public void selectAll(){
        String sql = "select * from " + DEVICE_LIST + ";";
        Cursor results = mDB.rawQuery(sql, null);

        results.moveToFirst();

        while(!results.isAfterLast()){
            int id = results.getInt(0);
            String voca = results.getString(1);
            // Toast.makeText(this, "id = "+_ID+" name="+NAME, 0).show();
            results.moveToNext();
        }
        results.close();
    }

    //2. 특정 행(row)을 선택하고 싶은 경우

    //UPDATE : 데이터 갱신
    public boolean updateColumn(long id, String name, String sn, Boolean isTM){
        ContentValues values = new ContentValues();
        values.put(DataBase.CreateDB.NAME, name);
        values.put(DataBase.CreateDB.SERIAL_NO, sn);
        values.put(DataBase.CreateDB.IS_THEFT_MODE, isTM);
        return mDB.update(DEVICE_LIST, values, "_ID=" + id, null) > 0;
    }

    //DELETE : 데이터 삭제
    public boolean deleteColumn(long id){
        return mDB.delete(DEVICE_LIST, "_ID="+id, null) > 0;
    }
}
