package com.example.makeit;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class GooglenaverRequest extends StringRequest {
    //서버 url 설정 (php 파일 연동)
    final static private String URL = "http://192.168.75.235/googlenaver_makeit.php"; //"http://퍼블릭 DNS 주소/login_makeit.php"
    private Map<String, String> parameters;

    public GooglenaverRequest(String userID, String userPW, String userName, String userEmail, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        parameters = new HashMap<>();
        parameters.put("userID", userID);
        parameters.put("userPW", userPW);
        parameters.put("userName", userName);
        parameters.put("userEmail", userEmail);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }

}
