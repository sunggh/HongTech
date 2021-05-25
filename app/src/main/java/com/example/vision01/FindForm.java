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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.TextView;
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
    public ImageView level1;
    public ImageView level2;
    public ImageView level3;
    public TextView guide;
    public TextView level_tx;

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

        level1=findViewById(R.id.rssi_level1);
        level2=findViewById(R.id.rssi_level2);
        level3=findViewById(R.id.rssi_level3);
        guide=findViewById(R.id.textView_guide);
        level_tx=findViewById(R.id.textView_level);

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

        findButton.setOnClickListener(new View.OnClickListener(){ //파인드
            @Override
            public void onClick(View v) {
                switch (Mode) {
                    case NONE:
                        scan();
                        break;
                    case SEARCH_READY:
                        setLevel0();
                        Toast.makeText(getApplicationContext(),"AR찾기모드가 실행 되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ARCamera.class);
                        startActivity(intent);
                        Mode = CUR_MODE.AR;
                        break;
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
        //leScanner.startScan(scanCallback);
    }

    private ScanCallback scanCallback = new ScanCallback() {

        int Distance_Count = 0, increase = 0, direction = 0, control = 0;
        double[] rssi_direction = new double[10];
        KalmanFilter kalmanFilter= null;
        double progress_rssi = -65;
        double AR_RSSI,PRO_RSSI=0;
        double standard_rssi = -75;
        int far_Distance_Count = 0, near_Distance_Count = 0;
        int current_Level = 0;
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
                    Log.e("CheckRSSI","value - " + filtered_rssi);
                    current_Level = checkLevel(filtered_rssi);
                    if(current_Level == 3){
                        guide.setText("물건 찾기 버튼을 눌러주세요! AR화면으로 전환됩니다.");
                        Mode = CUR_MODE.SEARCH_READY;
                        //Toast.makeText(getApplicationContext(), " 찾기모드가 준비 되었습니다. 버튼을 눌러 시작해주세요", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if(filtered_rssi > -75) {
                        //Toast.makeText(getApplicationContext(), " 근처에 있습니다. 조금만 앞으로 이동해주세요." + String.valueOf((int)(filtered_rssi)), Toast.LENGTH_SHORT).show();
                        guide.setText("물건이 근처에 있습니다. 조금 더 앞으로 이동해주세요.");
                        break;
                    } else {
                        if(standard_rssi == 0) {
                            standard_rssi = (int) filtered_rssi;
                            break;
                        }
                        if((int)filtered_rssi < standard_rssi ) {
                            //멀어지면
                            Log.e("Far",filtered_rssi + "," + standard_rssi +"-" + far_Distance_Count );
                            far_Distance_Count++;
                            near_Distance_Count = 0;
                            standard_rssi = (int)filtered_rssi;
                            if(far_Distance_Count == 3) {
                                Log.d("ToastFar","멀어지고 있습니다.");
                                //Toast.makeText(getApplicationContext(), " 신호가 멀어지고 있습니다. :" + String.valueOf((int)(filtered_rssi)), Toast.LENGTH_SHORT).show();
//                                    if(mToast != null) mToast.cancel(); //다른 토스트가 실시간으로 올라올때 바로바로 지워지게 하는 방법
//                                    mToast = Toast.makeText(getApplicationContext(), " 신호가 멀어지고 있습니다. :" + String.valueOf((int)(filtered_rssi)), Toast.LENGTH_SHORT);
//                                    mToast.show();
                                far_Distance_Count = 0;
                                guide.setText("물건과 멀어지고 있습니다. 다시 돌아가 주세요.");
                            }
                        }
                        else{   //가까워지면
                            Log.e("Near",filtered_rssi + "," + standard_rssi+"-" + near_Distance_Count);
                            near_Distance_Count++;
                            far_Distance_Count = 0;
                            standard_rssi = (int)filtered_rssi;
                            if(near_Distance_Count == 3) {
                                Log.d("ToastFar","가까워지고 있습니다.");
                                //Toast.makeText(getApplicationContext(), " 신호가 가까워지고 있습니다. :" + String.valueOf((int)(filtered_rssi)), Toast.LENGTH_SHORT).show();
//                                    if(mToast != null) mToast.cancel();
//                                    mToast = Toast.makeText(getApplicationContext(), " 신호가 가까워지고 있습니다. :" + String.valueOf((int)(filtered_rssi)), Toast.LENGTH_SHORT);
//                                    mToast.show();
                                near_Distance_Count = 0;
                                guide.setText("물건과 가까워지고 있습니다. 좀 더 앞으로 가주세요.");
                            }
                        }
                    }
                    break;
                case AR:
                    if(AR_Mode == AR_MODE.NONE) {
                        if(control == 0) {
                            kalmanFilter = new KalmanFilter(rssi); // 칼만 필터 생성
                        }
                        if(control != 20) {
                            //  Toast.makeText(getApplicationContext(), "최적화 중입니다 잠시만 기다려주세요. ("+control+"/10)", Toast.LENGTH_SHORT).show();
                            AR_RSSI = filtered_rssi;
                            control++;
                            break;
                        }
                        control=0;
                        AR_RSSI = filtered_rssi; // AR중에 젤 최소값
                        AR_Mode = AR_MODE.SEARCHING;
                    } else if(AR_Mode == AR_MODE.SEARCHING) {
                        if(rssi<filtered_rssi-4) {
                            return;
                        }
                        if(AR_RSSI < rssi) {
                            AR_RSSI = rssi;
                            AR_Mode = AR_MODE.SEARCHED;
                        }
                    } else if (AR_Mode == AR_MODE.SEARCHED) {
                        if(rssi<filtered_rssi-4) {
                            return;
                        }
                        if(AR_RSSI < rssi) {
                            AR_RSSI = rssi;
                            AR_Mode = AR_MODE.SEARCHED;
                            break;
                        }
                        if(AR_RSSI-2 > rssi) {
                            AR_Mode = AR_MODE.SEARCH_FINISH;
                            break;
                        }
                    } else if(AR_Mode == AR_MODE.FINISH) {
                        if(filtered_rssi >=-70) {
                            Intent intent = new Intent(getApplicationContext(), ProgressbarForm.class);
                            startActivity(intent);
                            Mode=CUR_MODE.PROGRESS;
                        }
                    }
                    Toast.makeText(getApplicationContext(), "rssi : " + rssi,Toast.LENGTH_SHORT).show();
                    break;
                case PROGRESS:

//                    if(PRO_RSSI == 0 ) {
//                        PRO_RSSI = filtered_rssi;
//                        break;
//                    }
//                    if(PRO_RSSI-1 > filtered_rssi) {
//                        if(increase == 3) {
//                            increase = 0;
//                            PRO_RSSI = filtered_rssi;
//                        } else {
//                            increase++;
//                        }
//                        break;
//                    } else {
//                        PRO_RSSI = filtered_rssi;
//                        increase = 0;
//                    }

                    if(ProgressbarForm.circleProgressBar == null) break;

                    if(control == 0) {

                        //현재 rssi에서 들어온 rssi값 빼기
                        int percent = (int)progress_rssi-(int)filtered_rssi;

                        //테스트를 위한 rssi값 표시
                        Toast.makeText(getApplicationContext(), "rssi : " + filtered_rssi,Toast.LENGTH_SHORT).show();

                        if((int)filtered_rssi < -70) {
                            Toast.makeText(getApplicationContext(), "RSSI 신호가 범위 내에 들도록 이동해 주세요.",Toast.LENGTH_SHORT).show();
                            ProgressbarForm.test.circleProgressBar.setProgress(0);
                        }

                        else if((int)filtered_rssi >= -50) {
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

    public int checkLevel(double filtered_rssi){
        int check = 0;
        //Level1 (-82 < filtered_rssi < -77)
        //Level2 (-77 < filtered_rssi < -72)
        //Level3 (filtered_rssi > -72) : 3단계에 달성하면 3단계로 고정되고 물건찾기 버튼 활성화
        if(filtered_rssi < -82){
            level_tx.setText("0단계");
            setLevel0();
        }
        else if(filtered_rssi < -77){
            level_tx.setText("1단계");
            check = 1;
            setLevel1();
        }
        else if(filtered_rssi < -74){
            level_tx.setText("2단계");
            check = 2;
            setLevel2();
        }
        else{
            level_tx.setText("3단계");
            check = 3;
            setLevel3();
            //findButton.setBackgroundColor();
        }
        return check;
    }
    public void setLevel1(){
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_60);
        drawable.setColor(Color.rgb(205,255,204));  //#CDFFCC
        level1.setImageDrawable(drawable);

        drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_80);
        drawable.setColor(Color.WHITE);
        level2.setImageDrawable(drawable);

        drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_100);
        drawable.setColor(Color.WHITE);
        level3.setImageDrawable(drawable);
    }
    public void setLevel2(){
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_60);
        drawable.setColor(Color.rgb(205,255,204));  //#CDFFCC
        level1.setImageDrawable(drawable);

        drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_80);
        drawable.setColor(Color.rgb(205,255,204));  //#CDFFCC
        level2.setImageDrawable(drawable);

        drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_100);
        drawable.setColor(Color.WHITE);
        level3.setImageDrawable(drawable);
    }
    public void setLevel3(){
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_60);
        drawable.setColor(Color.rgb(205,255,204));  //#CDFFCC
        level1.setImageDrawable(drawable);

        drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_80);
        drawable.setColor(Color.rgb(205,255,204));  //#CDFFCC
        level2.setImageDrawable(drawable);

        drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_100);
        drawable.setColor(Color.rgb(205,255,204));  //#CDFFCC
        level3.setImageDrawable(drawable);
    }
    public void setLevel0(){
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_60);
        drawable.setColor(Color.WHITE);  //#CDFFCC
        level1.setImageDrawable(drawable);

        drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_80);
        drawable.setColor(Color.WHITE);  //#CDFFCC
        level2.setImageDrawable(drawable);

        drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.shape_100);
        drawable.setColor(Color.WHITE);  //#CDFFCC
        level3.setImageDrawable(drawable);
    }
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