package com.example.vision01;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
//import android.graphics.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FindForm extends AppCompatActivity {

    private Button findButton;
    private int first_direction=-1,second_direction = -1;
    private KalmanFilter[] Kalmans = new KalmanFilter[4];
    private double secondRssi=0,firstRssi;
    public static CUR_MODE Mode = CUR_MODE.NONE;
    public static AR_MODE AR_Mode = AR_MODE.NONE;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;

    public enum CUR_MODE {
        NONE,
        SEARCH_READY,
        SEARCH,
        SEARCHING,
        SEARCHED,
        AR,
        RADER,
        PROGRESS,
        PROGRESSING;
    }
    public enum AR_MODE {
        NONE,
        SEARCHING,
        SEARCHED,
        SEARCH_FINISH,
        FINISH;
    }



    SurfaceView pc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation_request);
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        leScanner = bluetoothAdapter.getBluetoothLeScanner();

        pc = findViewById(R.id.surfaceView_cam);

        //동적퍼미션 작업
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int permissionResult= checkSelfPermission(Manifest.permission.CAMERA);
            if(permissionResult== PackageManager.PERMISSION_DENIED){
                String[] permissions= new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,10);
            }
        }else{
            pc.setVisibility(View.VISIBLE);
        }

        findButton = (Button)findViewById(R.id.button_find);
        final ImageView arrow_image = (ImageView)findViewById(R.id.right_arrow);

        findButton.setOnClickListener(new View.OnClickListener(){ //파인드
            @Override
            public void onClick(View v) {
                switch (Mode) {
                    case NONE:
                        arrow_image.setVisibility(View.VISIBLE);
                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate);
                        arrow_image.startAnimation(animation);
                        scan();
                        break;
                    case SEARCH_READY:
                        Toast.makeText(getApplicationContext(),"찾기모드가 실행 되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ARCamera.class);
                        startActivity(intent);
                        Mode = CUR_MODE.AR;
                        break;
                    case SEARCH:
                        Toast.makeText(getApplicationContext(),"찾기 재 시작", Toast.LENGTH_SHORT).show();
                        Mode = CUR_MODE.SEARCHING;
                        break;
//                    case SEARCHED:
//                        Intent intent = new Intent(getApplicationContext(), ARCamera.class);
//                        startActivity(intent);
//                        Mode = CUR_MODE.AR;
//                        break;
                }
            }
        });
    }

    private void scan() {
        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress("F0:08:D1:D4:F8:52").build(); //F8:95:EA:5A:DD:3C, F0:08:D1:D4:F8:52
        //F0:08:D1:D4:F8:52
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();
        leScanner.startScan(filters,settings,scanCallback);
    }

    private ScanCallback scanCallback = new ScanCallback() {

        int Distance_Count = 0, increase = 0, direction = 0, control = 0;
        double[] rssi_direction = new double[10];
        KalmanFilter kalmanFilter= null;
        double progress_rssi = -65;
        double AR_RSSI;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(Mode == CUR_MODE.SEARCH || Mode == CUR_MODE.SEARCHED|| Mode == CUR_MODE.PROGRESSING || AR_Mode == AR_MODE.SEARCH_FINISH)  return;

            /* RSSI 변수 선언 */
            int rssi = result.getRssi();
            double filtered_rssi =0.0 , filtered_rssied = 0.0;
            /* 칼만 필터 */
            if(kalmanFilter == null) {
                kalmanFilter = new KalmanFilter(rssi); // 칼만 필터 생성
                return;
            } else {
                filtered_rssi = kalmanFilter.update(rssi); // 들어오는 rssi 값 칼만 필터 적용
            }
            switch(Mode) {
                case NONE:
                    if(filtered_rssi<=-73) {
                        if(filtered_rssi > -75) {
                            Toast.makeText(getApplicationContext(), " 근처에 있습니다. 조금만 앞으로 이동해주세요." + String.valueOf((int)(filtered_rssi)), Toast.LENGTH_SHORT).show();
                            break;
                        } else {
                            Distance_Count++;
                            if(Distance_Count == 15) {
                                Toast.makeText(getApplicationContext(), " 신호가 멀어지고 있습니다. :" + String.valueOf((int)(filtered_rssi)), Toast.LENGTH_SHORT).show();
                                Distance_Count=0;
                            }
                        }
                    } else {
                        if(Distance_Count == 0) {
                            if(filtered_rssi>-65) {
                                Toast.makeText(getApplicationContext(), " 이 근방에 있나봐요 바로 찾기모드로 진행됩니다.", Toast.LENGTH_SHORT).show();
                                Mode = CUR_MODE.PROGRESS;
                                Intent intent = new Intent(getApplicationContext(), ProgressbarForm.class);
                                startActivity(intent);
                                Mode = CUR_MODE.PROGRESS;
                                break;
                            }
                            Mode = CUR_MODE.SEARCH_READY;
                            Toast.makeText(getApplicationContext(), " 찾기모드가 준비 되었습니다. 버튼을 눌러 시작해주세요", Toast.LENGTH_SHORT).show();
                            kalmanFilter = null;
                            filtered_rssied=0.0f;
                            increase=0;
                        } else {
                            Distance_Count=0;
                        }
                    }
                    break;
                case SEARCHING:
                    Distance_Count++;
                    if(Distance_Count==20) {
                        Mode = CUR_MODE.SEARCH;
                        Distance_Count=0;
                        rssi_direction[direction] = filtered_rssi;
                        Kalmans[direction] = kalmanFilter;
                        Log.i("onScanResult", direction+" | " +  rssi_direction[direction]);
                        direction++;
                        if(direction == 4) { // 4방향을 다 돌았으면
                            Mode = CUR_MODE.SEARCHED;
                            /* 젤 큰 RSSI 방향과 두번째로 큰 RSSI 방향 탐색 */
                            double temp = -9999, tems = -9999;
                            for (int a = 0; a < 4; a++) {
                                if (rssi_direction[a] > temp) {
                                    temp = rssi_direction[a];
                                    first_direction = a;
                                } else {
                                    if (rssi_direction[a] > tems) {
                                        tems = rssi_direction[a];
                                        second_direction = a;
                                        firstRssi = rssi_direction[a];
                                        secondRssi = firstRssi;
                                    }
                                }
                            }
                            Toast.makeText(getApplicationContext(), "방향 : " + first_direction, Toast.LENGTH_SHORT).show();
                            Log.i("onScanResult", "|" + "젤 작은 방향" + "|" + first_direction + "| 두번째 |" + second_direction);
                            kalmanFilter = Kalmans[first_direction]; // 젤 작은 RSSI 필터 적용
                            break;
                        }
                        Toast.makeText(getApplicationContext(), "오른쪽으로 다음 방향", Toast.LENGTH_SHORT).show();
                        kalmanFilter = null;
                    }
                    break;
                case AR:
                    if(AR_Mode == AR_MODE.NONE) {
                        AR_RSSI = filtered_rssi; // AR중에 젤 최소값
                        AR_Mode = AR_MODE.SEARCHING;
                        break;
                    } else if(AR_Mode == AR_MODE.SEARCHING) {
                        if(AR_RSSI < filtered_rssi) {
                            AR_RSSI = filtered_rssi;
                        } else {
                            if(AR_RSSI-4 < filtered_rssi){
                                increase++;
                            } else {
                                if(increase > 10) {
                                    Toast.makeText(getApplicationContext(), "이전 방향으로 돌려주세요.", Toast.LENGTH_SHORT).show();
                                    AR_Mode = AR_MODE.SEARCHED;
                                    increase = 0;
                                    break;
                                }
                                increase = 0;
                            }
                        }
                        break;
                    } else if(AR_Mode == AR_MODE.SEARCHED) {
                        if(AR_RSSI-2 < filtered_rssi) {
                            AR_Mode = AR_MODE.SEARCH_FINISH;
                            break;
                        } else {
                            Toast.makeText(getApplicationContext(), "AR_RSSI : "+AR_RSSI + "| RSSI :"+filtered_rssi, Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(filtered_rssi > -65) {
                        Intent intent = new Intent(getApplicationContext(), ProgressbarForm.class);
                        startActivity(intent);
                        Mode = CUR_MODE.PROGRESS;
                    }
                    break;
                case RADER:
                    if(OpenRader.mRadarView == null || OpenRader.textView == null)  break;
                    if(control == 15) {
                        if(filtered_rssied > filtered_rssi) {
                            increase++;

                        } else {
                            filtered_rssied=filtered_rssi;
                            increase = 0;

                        }
                        if(increase == 2) {
                            switch (first_direction) {
                                case 0:
                                    if (second_direction == 1) {
                                        OpenRader.mRadarView.target_alpha = 0;
                                        Toast.makeText(getApplicationContext(), "오른쪽", Toast.LENGTH_SHORT).show();

                                    } else if (second_direction == 3) {
                                        OpenRader.mRadarView.target_alpha = 180;
                                        Toast.makeText(getApplicationContext(), "왼쪽", Toast.LENGTH_SHORT).show();
                                    } else  {
                                        Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();

                                    }
                                    break;
                                case 1:
                                    if (second_direction == 2) {
                                        OpenRader.mRadarView.target_alpha = 0;
                                        Toast.makeText(getApplicationContext(), "오른쪽", Toast.LENGTH_SHORT).show();
                                    } else if (second_direction == 0) {
                                        OpenRader.mRadarView.target_alpha = 180;
                                        Toast.makeText(getApplicationContext(), "왼쪽", Toast.LENGTH_SHORT).show();
                                    } else  {
                                        Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 2:
                                    if (second_direction == 3) {
                                        OpenRader.mRadarView.target_alpha = 0;
                                        Toast.makeText(getApplicationContext(), "오른쪽", Toast.LENGTH_SHORT).show();
                                    } else if (second_direction == 1) {
                                        OpenRader.mRadarView.target_alpha = 180;
                                        Toast.makeText(getApplicationContext(), "왼쪽", Toast.LENGTH_SHORT).show();
                                    } else  {
                                        Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 3:
                                    if (second_direction == 0) {
                                        OpenRader.mRadarView.target_alpha = 0;
                                        Toast.makeText(getApplicationContext(), "오른쪽", Toast.LENGTH_SHORT).show();
                                    } else if (second_direction == 2) {
                                        OpenRader.mRadarView.target_alpha = 180;
                                        Toast.makeText(getApplicationContext(), "왼쪽", Toast.LENGTH_SHORT).show();
                                    } else  {
                                        Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }
                        } else {
                            if(OpenRader.mRadarView == null)  break;
                            OpenRader.mRadarView.target_alpha = 90;
                        }
                        if(OpenRader.mRadarView == null && OpenRader.textView == null)  break;

                        OpenRader.mRadarView.startAnimation();
                        OpenRader.textView.setText(String.valueOf((int)(filtered_rssi)));
                        if(filtered_rssi > -56) {
                            Mode = CUR_MODE.PROGRESS;

                            Intent intent = new Intent(getApplicationContext(), ProgressbarForm.class);
                            startActivity(intent);
                        }
                        control = 0 ;
                    } else
                        control++;
                    break;
                case PROGRESS:
                    if(ProgressbarForm.circleProgressBar == null) break;

                    if(control == 10) {

//                        int tmp;
//
//                        tmp = -65 - (int)filtered_rssi;

                        Toast.makeText(getApplicationContext(), "rssi::" + filtered_rssi,Toast.LENGTH_SHORT).show();

                        if(filtered_rssi >= -70) {


                            if(filtered_rssi <= -66) {
                                ProgressbarForm.test.progress(1);
                            }

                            else if(filtered_rssi <= -64) {
                                ProgressbarForm.test.progress(2);
                            }

                            else if(filtered_rssi <= -61) {
                                ProgressbarForm.test.progress(3);
                            }

                            else if(filtered_rssi <= -58){
                                ProgressbarForm.test.progress(4);
                            }
                            else if(filtered_rssi <= -55) {
                                ProgressbarForm.test.progress(5);
                            }

                            else if(filtered_rssi <= -52) {
                                ProgressbarForm.test.progress(6);
                            }

                            else if(filtered_rssi <= -49) {
                                ProgressbarForm.test.progress(7);
                            }
                            else if(filtered_rssi <= -46) {
                                ProgressbarForm.test.progress(8);
                            }
                            else if(filtered_rssi <= -43) {
                                ProgressbarForm.test.progress(9);
                            }
                            //-40보다 가까워지면
                            else {
                                ProgressbarForm.test.progress(10);
                            }


//                            System.out.println("tmp"+tmp);
//                            if(tmp > 0) {
//
//                                a = (int)(tmp/(0.15));
//                                System.out.println("a"+a);
//
//
//                                for(int i=0; i<a; i++) {
//                                    if(ProgressbarForm.circleProgressBar.getProgress()==100) {
//                                        break;
//                                    }
//
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            ProgressbarForm.circleProgressBar.setProgress(ProgressbarForm.circleProgressBar.getProgress()+1);
//
//                                        }
//                                    });
//
//                                    try {
//                                        Thread.sleep(100);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//                            else {
//                                b = (int)(tmp/(0.15)*-1);
//                                System.out.println("b"+b);
//
//                                for(int i=0; i<b; i++) {
//                                    if(ProgressbarForm.circleProgressBar.getProgress()==0) {
//                                        break;
//                                    }
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            ProgressbarForm.circleProgressBar.setProgress(ProgressbarForm.circleProgressBar.getProgress()-1);
//                                        }
//                                    });
//
//                                    try {
//                                        Thread.sleep(100);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }

                            progress_rssi = filtered_rssi;
                        }
                        //filtered_rssi가 -65보다 더 작은 범위로 가면 예외처리
                        else {
                            Toast.makeText(getApplicationContext(), " 범위 밖입니다 rssi::" + filtered_rssi,Toast.LENGTH_SHORT).show();
                        }

                        control = 0;
                    }
                    //control == 3 이 아니면
                    else {
                        control++;
                    }
                    break;
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i("MainActivity.java | onScanFailed", "|" + "2222222222222" + "|" + errorCode);
        }


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult result : results) {
                if(result.getDevice().getAddress()=="F0:08:D1:D4:F8:52") {
                    Toast.makeText(getApplicationContext(), "2 rssi : " + result.getRssi() +" ,Tx :"+ result.getTxPower(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i("MainActivity.java | onBatchScanResults", "33333333333333|" + result.getDevice().getName() + "|" + result.getDevice().getAddress() + "|" + result.getTxPower() + "|" + result.getRssi() + "|");
            }
        }
    };


}


class KalmanFilter {

    private double Q = 0.00001;
    private double R = 0.001;
    private double X = 0, P = 1, K;

    public KalmanFilter(double initValue) {
        X = initValue;
    }

    private void measurementUpdate(){
        K = (P + Q) / (P + Q + R);
        P = R * (P + Q) / (R + P + Q);
    }

    public double update(double measurement){
        measurementUpdate();
        X = X + (measurement - X) * K;
        return X;
    }
}