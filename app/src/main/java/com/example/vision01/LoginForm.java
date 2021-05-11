package com.example.vision01;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.vision01.Sqlite.SqliteDb;
import com.example.vision01.Utils.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import androidx.annotation.RequiresApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import java.util.List;

public class LoginForm extends AppCompatActivity {

    private EditText login_id, login_pw;
    private Button joinButton, loginButton;
    private boolean findAble = true;
    private int findWhere=0;



    private BluetoothAdapter bluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Preferences.setContext(this);
        Preferences.initial();
        dataFileCopy();
        setContentView(R.layout.activity_login_form);

        login_id = findViewById(R.id.login_id);
        login_pw = findViewById(R.id.login_pw);

        joinButton = (Button)findViewById(R.id.btn_join_login);
        loginButton = (Button)findViewById(R.id.btn_login);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DeviceListForm.class);
                startActivity(intent);
  /*
                String user_id = login_id.getText().toString();
                String user_pw = login_pw.getText().toString();

                Intent intent = new Intent(getApplicationContext(), DeviceListForm.class);
                startActivity(intent);

                Log.e("login","로그인 버튼 클릭");

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if(success){ // 로그인 성공 시
                                String user_id = jsonObject.getString("user_id");
                                String user_pw = jsonObject.getString("user_pw");

                                Log.e("login","로그인 성공");

                                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), ARCamera.class);

                                intent.putExtra("user_id", user_id);
                                intent.putExtra("user_pw", user_pw);

                                startActivity(intent);
                            }
                            else { // 로그인 실패 시
                                Log.e("login","로그인 실패");
                                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e){
                            Log.e("login","로그인 실패");
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest(user_id, user_pw, responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginForm.this);
                queue.add(loginRequest);
*/
            }
        });
        joinButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProgressbarForm.class);
                startActivity(intent);
            }
        });
    }

    private BluetoothLeScanner leScanner;
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
    private ArrayList<Integer> rssi_a = new ArrayList<Integer>();
    private int blueRssi=-120,maxRssi;

    private int[] rssis = new int[10];

    private boolean blue = false;
    private boolean blueA = false;
    private ScanCallback scanCallback = new ScanCallback()
    {
        int i = 0;
        int j = 0,c = 0;
        double[] sampling = new double[10];
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);
            if(!findAble) return; // 계산시 중복 방지
            int rssi= result.getRssi();
            rssis[(rssi*-1)/10]++;

            Log.i("onScanResult", "|" + "111111111111111" + "|"  + result.getDevice().getAddress() +"|"+ result.getRssi());
            c++;
            System.out.println("오긴옴"+c);
            if(c == 20) {
                findAble = false;
                c = 0;
                int x=0;
                System.out.println("오긴옴"+findWhere);
                sampling[findWhere] = 0.1*rssis[1]+0.2*rssis[2]+0.3*rssis[3]+0.5*rssis[4]+0.6*rssis[5]+1.0*rssis[6]+1.5*rssis[7]+2*rssis[8]+4*rssis[9];


                if(findWhere ==3) {
                    double temp=10000;
                    for(int a=0;a<4;a++) {
                        if(sampling[a]<temp)  {
                            temp=sampling[a];
                            x= a;
                        }
                    }
                    Log.i("onScanResult", "젤 쌘 방향은 " + x);
                    Toast.makeText(getApplicationContext()," x " + x, Toast.LENGTH_SHORT).show();
                    return;
                } else{
                    Toast.makeText(getApplicationContext()," 스캔완료 " + findWhere, Toast.LENGTH_SHORT).show();
                }
                rssis = new int[10];
            }
            // Log.i("onScanResult", "|" + "111111111111111" + "|"  + result.getDevice().getAddress() +"|"+ result.getRssi());
            //Toast.makeText(getApplicationContext()," rssi는 대략 "+rssi+"db | " +j, Toast.LENGTH_SHORT).show();
        }


        @Override
        public void onScanFailed(int errorCode)
        {
            super.onScanFailed(errorCode);
            Log.i("MainActivity.java | onScanFailed", "|" + "2222222222222" + "|" + errorCode);
        }


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            super.onBatchScanResults(results);

            for (ScanResult result : results)
            {
                Log.i("MainActivity.java | onBatchScanResults", "33333333333333|" + result.getDevice().getName() + "|" + result.getDevice().getAddress() + "|"+ result.getTxPower() + "|"+ result.getRssi() + "|");
            }
        }
    };
    public void dataFileCopy(){
        try{
            AssetManager assetMgr = this.getAssets();
            String[] fileList = assetMgr.list("");
            for (String fileName : fileList) {


                if(fileName.startsWith(SqliteDb.DBFILE_PREFIX)){

                    Log.e("DB", "file name : " + fileName);
                    String fileDate = fileName.substring(SqliteDb.DBFILE_PREFIX.length()).substring(0,6);
                    String toPath = getExternalFilesDir(null).getPath().toString() + File.separator + SqliteDb.DBFILE_PREFIX + "Use.sqlite";
                    Log.e("DB", "toPath : " + toPath);
                    if(Preferences.getString(Preferences.DB_UPDATE_DATE) == null || Preferences.getString(Preferences.DB_UPDATE_DATE) == "" ){
                        Log.e("DB", "preference null!!");
                        File file = new File(toPath);
                        copyAsset(assetMgr, fileName, file.getAbsolutePath());
                        Log.e("DB", "after copy");
                        Preferences.putString(Preferences.DB_UPDATE_DATE, fileDate);
                        Log.e("DB", "Preferences DB_UPDATE_DATE : " + Preferences.getString(Preferences.DB_UPDATE_DATE));
                    }
                    else{
                        Log.e("DB", "Preferences DB_UPDATE_DATE : " + Preferences.getString(Preferences.DB_UPDATE_DATE));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath){
        InputStream input = null;
        OutputStream output = null;

        try{
            input = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            output = new FileOutputStream(toPath);

            copyFile(input, output);
            input.close();
            input = null;
            output.flush();
            output.close();
            output = null;

            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void copyFile(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];

        int read = 0;
        while((read = input.read(buffer)) > 0){
            output.write(buffer, 0, read);
        }
    }
}