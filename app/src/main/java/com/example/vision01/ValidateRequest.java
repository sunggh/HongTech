package com.example.vision01;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ValidateRequest extends StringRequest{
    final static  private String URL="http://121.181.163.88/android/UserValidate.php";
    private Map<String,String> map;

    public ValidateRequest(String userID, Response.Listener<String>listener){
        super(Request.Method.POST,URL,listener,null);
        System.out.println(userID);
        map=new HashMap<>();
        map.put("user_id",userID);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
