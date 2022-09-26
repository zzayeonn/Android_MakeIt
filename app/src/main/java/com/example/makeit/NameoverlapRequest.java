package com.example.makeit;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class NameoverlapRequest extends StringRequest {
    //서버 url 설정 (php 파일 연동)
    final static private String URL = "http://192.168.75.235/nameoverlap_makeit.php"; //"http://퍼블릭 DNS 주소/overlap_makeit.php"
    private Map<String, String> parameters;

    public NameoverlapRequest(String userName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        parameters = new HashMap<>();
        parameters.put("userName", userName);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }

}
