package com.example.vision01;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest{
    // 서버 URL 설정(php 파일 연동)
    final static private String URL = "http://121.181.163.88/android/Register.php";
    private Map<String, String> map;

    public RegisterRequest(String user_id, String user_pw, String user_sn, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("user_id", user_id);
        map.put("user_pw", user_pw);
        map.put("user_sn", user_sn);
    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError{
        return map;
    }
}
