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
        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress("F8:95:EA:5A:DD:3C").build(); //F8:95:EA:5A:DD:3C, F0:08:D1:D4:F8:52
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
        double AR_RSSI,PRO_RSSI=0;
        double standard_rssi = -75;
        int far_Distance_Count = 0, near_Distance_Count = 0;
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
                            if((int)filtered_rssi < (int)standard_rssi ) {
                                //멀어지면
                                far_Distance_Count++;
                                near_Distance_Count = 0;
                                standard_rssi = filtered_rssi;
                                if(far_Distance_Count == 3) {
                                    Toast.makeText(getApplicationContext(), " 신호가 멀어지고 있습니다. :" + String.valueOf((int)(filtered_rssi)), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{   //가까워지면
                                near_Distance_Count++;
                                far_Distance_Count = 0;
                                standard_rssi = filtered_rssi;
                                if(near_Distance_Count == 3) {
                                    Toast.makeText(getApplicationContext(), " 신호가 가까워지고 있습니다. :" + String.valueOf((int)(filtered_rssi)), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else {
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
                    }
                    break;
                case AR:
                    if(AR_Mode == AR_MODE.NONE) {
                        AR_RSSI = filtered_rssi; // AR중에 젤 최소값
                        AR_Mode = AR_MODE.SEARCHING;
                    } else if(AR_Mode == AR_MODE.SEARCHING) {
                        Toast.makeText(getApplicationContext(), "RSSI :"+filtered_rssi + "| AR_RSSI : "+ AR_RSSI, Toast.LENGTH_SHORT).show();
                        if(AR_RSSI < filtered_rssi) {
                            AR_RSSI = filtered_rssi;
                            AR_Mode = AR_MODE.SEARCHED;
                        }
                    } else if (AR_Mode == AR_MODE.SEARCHED) {
                        if(AR_RSSI < filtered_rssi) {
                            AR_RSSI = filtered_rssi;
                            AR_Mode = AR_MODE.SEARCHED;
                            break;
                        }
                        if(AR_RSSI-2 < filtered_rssi) {
                            AR_RSSI = filtered_rssi;
                            AR_Mode = AR_MODE.SEARCH_FINISH;
                            break;
                        }
                    }
                    break;
                case PROGRESS:
                    if(ProgressbarForm.circleProgressBar == null) break;

                    if(control == 0) {

                        //현재 rssi에서 들어온 rssi값 빼기
                        int percent = (int)progress_rssi-(int)filtered_rssi;

                        //테스트를 위한 rssi값 표시
                        Toast.makeText(getApplicationContext(), "rssi : " + filtered_rssi,Toast.LENGTH_SHORT).show();

                        if((int)filtered_rssi < -67) {
                            Toast.makeText(getApplicationContext(), "RSSI 신호가 범위 내에 들도록 이동해 주세요.",Toast.LENGTH_SHORT).show();

                            //ProgressbarForm.test.circleProgressBar.setProgress(0);
                        }

                        else if((int)filtered_rssi >= -52) {
                            Toast.makeText(getApplicationContext(), "물건이 바로 근처에 있습니다.",Toast.LENGTH_SHORT).show();
                            ProgressbarForm.test.circleProgressBar.setProgress(100);
                        }

                        else {
                            ProgressbarForm.test.progress(percent);

                            progress_rssi = filtered_rssi;
                        }

                        control = 0;
                    }

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