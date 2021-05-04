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
//    private SurfaceView surfaceView;
//    private SurfaceHolder surfaceHolder;
//    private Camera camera;
    private RadarView rv;
    private ArrayList<Integer> rssi_a = new ArrayList<Integer>();
    private int blueRssi=-120,maxRssi, x=-1,y = -1;
    private double beforeRssi=0,firstRssi , ArRssi =0;

    private int[] rssis = new int[10];
    private int rssib = 0;
    private static int soundRssi = 0;
    private boolean blue = false,isFindAble=false;
    private int ValueChangeAble = 0;
    RadarView mRadarView = null;
    public static boolean findAble = true , isSoundMode = false , isRaderMode = false , isArMode = false;
    private int findWhere=0;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;

    SurfaceView pc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation_request);
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        leScanner = bluetoothAdapter.getBluetoothLeScanner();
//        surfaceView = (SurfaceView)findViewById(R.id.surfaceView_cam);
//        surfaceHolder = surfaceView.getHolder();
//        surfaceHolder.addCallback(surfaceListener);

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

        findButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(findAble) {
                    scan();
                    arrow_image.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate);
                    arrow_image.startAnimation(animation);
                }
                else {
                    if(x==-1) {
                        findAble = true;
                        findWhere++;
                        Toast.makeText(getApplicationContext(),"찾기 재 시작", Toast.LENGTH_SHORT).show();
                    } else {
                        findAble = true;
                        //Toast.makeText(getApplicationContext(),"찾기 모드 실행", Toast.LENGTH_SHORT).show();
                        isArMode = true;
                        Intent intent = new Intent(getApplicationContext(), ARCamera.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void scan()
    {
        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress("F8:95:EA:5A:DD:3C").build();

        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();
        leScanner.startScan(filters,settings,scanCallback);
    }
//    private SurfaceHolder.Callback surfaceListener = new SurfaceHolder.Callback() {
//
//        @Override//액티비티가 비활성 상태일 때 화면에 표시X
//        public void surfaceDestroyed(SurfaceHolder holder) {
//            camera.release();
//        }
//
//        @Override
//        public void surfaceCreated(SurfaceHolder holder) {
//            //카메라 객체를 받아와 카메라로부터 영상을 받을수있도록 초기화
//            camera = Camera.open();
//            try {
//                camera.setPreviewDisplay(holder);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//        @Override//카메라 객체에서 프리뷰 영상을 표시할 영역의 크기 설정
//        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//            Camera.Parameters parameters = camera.getParameters();
//            parameters.setPreviewSize(w, h);
//            camera.startPreview();
//        }
//    };
    private ScanCallback scanCallback = new ScanCallback() {
        int i = 0;
        int j = 0, c = 0;
        double[] sampling = new double[10];
        double[] blurring = new double[10];

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            int rssi = result.getRssi();

            if (isRaderMode) {
                if ((rssi * -1) - blueRssi < 5) {
                    OpenRader.textView.setText(String.valueOf((int) (rssi * -1)));
                    // System.out.println("계속바뀜");
                    blueRssi = rssi * -1;
                    rssib = 0;
                    //blue = false;
                } else {
                    if (rssib == 4) { // 튀는 값 중복이면 튀는 값 아님
                        blueRssi = rssi * -1;
                        rssib = 0;
                    } else {
                        rssib++;
                    }
                }
            }

            if (isSoundMode) {
                if ((rssi * -1) - blueRssi < 5) {
                    //  OpenRader.textView.setText(String.valueOf((int)(rssi*-1)));
                    // System.out.println("계속바뀜");
                    blueRssi = rssi * -1;
                    rssib = 0;
                    //blue = false;
                } else {
                    if (rssib == 4) { // 튀는 값 중복이면 튀는 값 아님
                        blueRssi = rssi * -1;
                        rssib = 0;
                    } else {
                        rssib++;
                    }
                }
                   /* if(soundRssi < rssi) {
                        if(rssi - soundRssi > 8) {
                            ValueChangeAble++;
                            return;
                        }
                        if(ValueChangeAble == 5) { // 그냥 값이 커지는거임 멀어진거
                            Toast.makeText(getApplicationContext(),"멀어졌는데 뒤돌아보세요 : ", Toast.LENGTH_SHORT).show();
                            ValueChangeAble = 0;
                            soundRssi = rssi;
                            return;
                        }
                    }*/
                Toast.makeText(getApplicationContext(), "rssi : " + rssi, Toast.LENGTH_SHORT).show();
                //soundRssi = rssi;
                return;
            }
            if (!findAble) return; // 계산시 중복 방지

            if (x == -1)
                blurring[findWhere] += rssi;
            else

                blurring[4] += rssi;

            rssis[(rssi * -1) / 10]++;

            Log.i("onScanResult", "|" + "111111111111111" + "|" + result.getRssi());

            c++;
            if (c == 10) {

                findAble = false;
                c = 0;
                if (isArMode) {
                    soundRssi++;
                    blurring[4] = (blurring[4] * -1) / 10;
                    if (soundRssi==1) {
                        isArMode = false;
                        Intent intent = new Intent(getApplicationContext(), OpenRader.class);
                        startActivity(intent);
                        isRaderMode = true;
                        rv = OpenRader.mRadarView;
                        findAble = true;
                        blueRssi = rssi;
                        soundRssi = 0;
                    }
                    return;
                }
                if (x == -1) {
                    if ((rssis[5] >= 5 && (rssis[7] != 0 || rssis[8] != 0)) || (rssis[6] >= 6 && (rssis[8] != 0 || rssis[9] != 0))) {
                        Toast.makeText(getApplicationContext(), "스캔 오류 : 리스캔 시작", Toast.LENGTH_SHORT).show();
                        findAble = true;
                        blurring[findWhere] = 0;
                        rssis = new int[10];
                        return;
                    }
                    sampling[findWhere] = 0.1 * rssis[1] + 0.2 * rssis[2] + 0.3 * rssis[3] + 0.4 * rssis[4] + 0.5 * rssis[5] + 0.6 * rssis[6] + 1.0 * rssis[7] + 5 * rssis[8] + 10 * rssis[9];
                    // Log.i("onScanResult", "|" + "111111111111111" + "|" +blurring[findWhere] +"|"+ result.getRssi());
                    blurring[findWhere] = (blurring[findWhere] * -1) / 10;

                    if (!isFindAble) {
                        if (blurring[findWhere] > 75) {

                            if (beforeRssi == 1000) {
                                if (beforeRssi < blurring[findWhere]) {
                                    Toast.makeText(getApplicationContext(), "멀어지고 있습니다", Toast.LENGTH_SHORT).show();
                                } else if (beforeRssi > blurring[findWhere]) {
                                    Toast.makeText(getApplicationContext(), "가까워지고 있어요,, ㅎㅎ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "다른곳으로 이동해주십시오.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (beforeRssi > blurring[findWhere]) {
                                    Toast.makeText(getApplicationContext(), "가까워지고 있습니다", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(getApplicationContext(), "기기랑 너무 멀리 떨어져있습니다.", Toast.LENGTH_SHORT).show();
                            }

                            findAble = true;
                            beforeRssi = blurring[findWhere];
                            c = 0;
                            blurring[findWhere] = 0;
                            rssis = new int[10];
                            return;
                        } else {
                            Toast.makeText(getApplicationContext(), "스캔 시작 합니다", Toast.LENGTH_SHORT).show();
                            isFindAble = true;
                            findAble = true;
                            return;
                        }
                    }

    //System.out.println("블러링 " +findWhere+" : " + blurring[findWhere] + "샘플링 : "+sampling[findWhere]);

                    if (findWhere == 3) {
                        double temp = 10000, tems = 9999;
                        for (int a = 0; a < 4; a++) {
                            if (blurring[a] < temp) {
                                temp = blurring[a];
                                x = a;
                            } else {
                                if (blurring[a] < tems) {
                                    tems = blurring[a];
                                    y = a;
                                    firstRssi = blurring[a];
                                    beforeRssi = firstRssi;
                                }
                            }
                        }

                        Toast.makeText(getApplicationContext(), " x " + x, Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        Toast.makeText(getApplicationContext(), " 스캔완료 " + findWhere, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!isRaderMode && !isSoundMode) {
                        Intent intent = new Intent(getApplicationContext(), OpenRader.class);
                        startActivity(intent);
                        isRaderMode = true;
                        rv = OpenRader.mRadarView;
                        findAble = true;
                        blueRssi = rssi;
                        return;
                    } else if (isRaderMode && !isSoundMode) {
                        blurring[4] = (blurring[4] * -1) / 10;

                        if ((blurring[4]) < 70) {
                            soundRssi = rssi;
                            Toast.makeText(getApplicationContext(), "사운드 모드 전환.", Toast.LENGTH_SHORT).show();
                            isSoundMode = true;
                            isRaderMode = false;
                            return;
                        }
                        if (beforeRssi + 3 < blurring[4]) {
                            switch (x) {
                                case 0:
                                    if (y == 1) {
                                        OpenRader.mRadarView.target_alpha = 0;
                                        //Toast.makeText(getApplicationContext(),"오른쪽으로 가보세요", Toast.LENGTH_SHORT).show();
                                    } else if (y == 3) {
                                        OpenRader.mRadarView.target_alpha = 180;
                                        //  Toast.makeText(getApplicationContext(),"왼쪽으로 가보세요", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Toast.makeText(getApplicationContext(),"리스캔 필요.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 1:
                                    if (y == 2) {
                                        OpenRader.mRadarView.target_alpha = 0;
                                        //  Toast.makeText(getApplicationContext(),"오른쪽으로 가보세요", Toast.LENGTH_SHORT).show();
                                    } else if (y == 0) {
                                        OpenRader.mRadarView.target_alpha = 180;
                                        //  Toast.makeText(getApplicationContext(),"왼쪽으로 가보세요", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Toast.makeText(getApplicationContext(),"리스캔 필요.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 2:
                                    if (y == 3) {
                                        OpenRader.mRadarView.target_alpha = 0;
                                        // Toast.makeText(getApplicationContext(),"오른쪽으로 가보세요", Toast.LENGTH_SHORT).show();
                                    } else if (y == 1) {
                                        OpenRader.mRadarView.target_alpha = 180;
                                        //  Toast.makeText(getApplicationContext(),"왼쪽으로 가보세요", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //  Toast.makeText(getApplicationContext(),"리스캔 필요.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 3:
                                    if (y == 0) {
                                        OpenRader.mRadarView.target_alpha = 0;
                                        // Toast.makeText(getApplicationContext(),"오른쪽으로 가보세요", Toast.LENGTH_SHORT).show();
                                    } else if (y == 2) {
                                        OpenRader.mRadarView.target_alpha = 180;
                                        // Toast.makeText(getApplicationContext(),"왼쪽으로 가보세요", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "리스캔 필요.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }
                        } else {
                            OpenRader.mRadarView.target_alpha = 90;
                            //Toast.makeText(getApplicationContext(),"오! 잘가고있어!!", Toast.LENGTH_SHORT).show();
                        }
                        beforeRssi = blurring[4];
                        blurring[4] = 0;
                        OpenRader.mRadarView.startAnimation();
                    }
                    findAble = true;
                }
                rssis = new int[10];
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
                Log.i("MainActivity.java | onBatchScanResults", "33333333333333|" + result.getDevice().getName() + "|" + result.getDevice().getAddress() + "|" + result.getTxPower() + "|" + result.getRssi() + "|");
            }
        }
    };


}