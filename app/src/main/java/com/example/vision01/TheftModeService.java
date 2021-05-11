package com.example.vision01;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.vision01.Device.Device;
import com.example.vision01.Device.DeviceManager;
import com.example.vision01.Sqlite.SqliteDb;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TheftModeService extends Service {
    static public ArrayList<Device> TMActivationDevices;
    SqliteDb sqliteDb = SqliteDb.getInstance();
    private int count = 0;
    private Timer T = T = new Timer();
    private Handler mHandler;
    public static Context mContext;
    //Service 에서 직접 UI 단을 업데이트 할 수 없기 때문에 핸들러를 사용하여 Toast를 작성한다.
    private class ToastRunnable implements Runnable {
        String mText;

        public ToastRunnable(String text) {
            mText = text;
        }
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_SHORT).show();
        }
    }
    public TheftModeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;

        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.e("TMService", "서비스시작");
        mContext = this;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TMService", "onStartCommand() called");

        // 서비스가 호출될 때마다 실행
        mHandler = new Handler();
        DeviceManager.devices = sqliteDb.dbDevice.getTheftDevices();
        TMActivationDevices = DeviceManager.devices;
        //1초당 한회씩 Toast를 띄운다.
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Log.e("service counter start: ", String.valueOf(count));
                count++;
                if((count % 10) == 5){
                    //mHandler.post(new ToastRunnable(count + "초 : 201724447 김태형"));
                    ((DeviceListForm)DeviceListForm.mContext).getTMRSSIList();
                }
                else if((count % 10) == 0){
                    exploreTheftDevice();
                }
            }
        }, 1000, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    private void exploreTheftDevice(){
        for(int i = 0; i < TMActivationDevices.size(); i++){
            Log.e("제품 탐색", "device" + TMActivationDevices.get(i).getName() );

            ((DeviceListForm)DeviceListForm.mContext).MatchTMDevice(TMActivationDevices.get(i));
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("TMService", "onDestroy() called");
        // 서비스가 종료될 때 실행
        T.cancel();
    }
}