package com.example.vision01;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoginForm extends AppCompatActivity {

    private EditText login_id, login_pw;
    private Button joinButton, loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //dataFileCopy();
        setContentView(R.layout.activity_login_form);

        login_id = findViewById(R.id.login_id);
        login_pw = findViewById(R.id.login_pw);

        joinButton = (Button)findViewById(R.id.btn_join_login);
        loginButton = (Button)findViewById(R.id.btn_login);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ARCamera.class);
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
                Intent intent = new Intent(getApplicationContext(), JoinForm.class);
                startActivity(intent);
            }
        });
    }
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

                    File file = new File(toPath);
                    copyAsset(assetMgr, fileName, file.getAbsolutePath());
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