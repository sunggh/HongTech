package com.example.vision01;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class TheftModeService extends Service {
    private int count = 0;
    private Timer T = new Timer();
    private Handler mHandler;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TMService", "onStartCommand() called");

        // 서비스가 호출될 때마다 실행
        mHandler = new Handler();
        //1초당 한회씩 Toast를 띄운다.
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.e("service counter start: ", String.valueOf(count));
                count++;

                mHandler.post(new ToastRunnable(count + "초 : 201724447 김태형"));
            }
        }, 1000, 1000);

        return super.onStartCommand(intent, flags, startId);
        //이전 버전
//        if (intent == null) {
//            return Service.START_STICKY; //서비스가 종료되어도 자동으로 다시 실행시켜줘!
//        } else {
//            String command = intent.getStringExtra("command");
//            String name = intent.getStringExtra("name");
//
//            //Log.d(TAG, "전달받은 데이터: " + command+ ", " +name);
//
//            try{
//                Thread.sleep(5000); //5초동안 정지
//            } catch(Exception e) {}
//
//            Intent showIntent = new Intent(getApplicationContext(), MainActivity.class);
//
//            /**
//             화면이 띄워진 상황에서 다른 액티비티를 호출하는 것은 문제가없으나,
//             지금은 따로 띄워진 화면이 없는 상태에서 백그라운드에서 실행중인 서비스가 액티비티를 호출하는 상황이다.
//             이러한 경우에는 FLAG_ACTIVITY_NEW_TASK 옵션(플래그)을 사용해줘야 한다.
//             **/
//            showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            showIntent.putExtra("command", "show");
//            showIntent.putExtra("name", name + " from service.");
//
//            // *** 이제 완성된 인텐트와 startActivity()메소드를 사용하여 MainActivity 액티비티를 호출한다. ***
//            //도난 임을 확인하게 되면 호출하면 됨 -> 알림을 줘야함
//            startActivity(showIntent);
//        }
//
//        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("TMService", "onDestroy() called");
        // 서비스가 종료될 때 실행
        T.cancel();
    }
}