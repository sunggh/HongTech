package com.example.vision01;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class JoinForm extends AppCompatActivity {

    private EditText join_id, join_pw, join_sn;
    Button btn_join,validateButton;
    private boolean validate=false;
    private AlertDialog dialog;
    private static String IP = "121.181.163.88";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_form);

        join_id = findViewById(R.id.join_id);
        join_pw = findViewById(R.id.join_pw);
        join_sn = findViewById(R.id.join_sn);
        btn_join = findViewById(R.id.btn_join);
        validateButton = findViewById(R.id.validateButton);
//
        String url = "http://" + IP + "/login.php";
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate) {
                    AlertDialog.Builder builder=new AlertDialog.Builder( JoinForm.this );
                    dialog=builder.setMessage("아이디 중복 여부를 체크 해주세요")
                            .setPositiveButton("확인",null)
                            .create();
                    dialog.show();
                    return;
                }
                if(regalPassword(join_pw.getText().toString())) {
                    insertoToDatabase(join_id.getText().toString(),join_pw.getText().toString(), join_sn.getText().toString());
                } else {
                    return;
                }

            }
        });

        validateButton.setOnClickListener(new View.OnClickListener() {//id중복체크
            @Override
            public void onClick(View view) {
                String userID=join_id.getText().toString();
                if(validate)
                {
                    return;
                }
                if(userID.equals("")){
                    AlertDialog.Builder builder=new AlertDialog.Builder( JoinForm.this );
                    dialog=builder.setMessage("아이디는 빈 칸일 수 없습니다")
                            .setPositiveButton("확인",null)
                            .create();
                    dialog.show();
                    return;
                }
                Response.Listener<String> responseListener=new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse=new JSONObject(response);
                            boolean success=jsonResponse.getBoolean("success");
                            if(success){
                                AlertDialog.Builder builder=new AlertDialog.Builder( JoinForm.this );
                                dialog=builder.setMessage("사용할 수 있는 아이디입니다.")
                                        .setPositiveButton("확인",null)
                                        .create();
                                dialog.show();
                                join_id.setEnabled(false);
                                validate=true;
                                validateButton.setText("확인");
                            }
                            else{
                                AlertDialog.Builder builder=new AlertDialog.Builder( JoinForm.this );
                                dialog=builder.setMessage("사용할 수 없는 아이디입니다.")
                                        .setNegativeButton("확인",null)
                                        .create();
                                dialog.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                ValidateRequest validateRequest=new ValidateRequest(userID,responseListener);
                RequestQueue queue= Volley.newRequestQueue(JoinForm.this);
                queue.add(validateRequest);

            }
        });


    }
//@"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@$!%*?&]).{8,}"
    private boolean regalPassword(String pwd){
        if(!pwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&]).{8,}")){
            AlertDialog.Builder builder=new AlertDialog.Builder( JoinForm.this );
            dialog=builder.setMessage("8자리 이상의 한개 이상의 영 소/대문자, 숫자, 특수 문자를 입력해 주세요.")
                    .setNegativeButton("확인",null)
                    .create();
            dialog.show();
            return false;
        }
        return true;
    }

    private void insertoToDatabase(final String ed1, String ed2, String ed3) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(JoinForm.this, "Please Wait", null, true, true);
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }
            @Override
            protected String doInBackground(String... params) {

                try {
                    String user_id = (String) params[0];
                    String user_pw = (String) params[1];
                    String user_sn = (String) params[2];

                    String link = "http://"+IP+"/insertData.php";
                    String data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8");
                    data += "&" + URLEncoder.encode("user_pw", "UTF-8") + "=" + URLEncoder.encode(user_pw, "UTF-8");
                    data += "&" + URLEncoder.encode("user_sn", "UTF-8") + "=" + URLEncoder.encode(user_sn, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
                    outputStreamWriter.write(data);
                    outputStreamWriter.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    Log.d("tag : ", sb.toString());
                    return sb.toString();

                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(ed1,ed2,ed3);
    }


}