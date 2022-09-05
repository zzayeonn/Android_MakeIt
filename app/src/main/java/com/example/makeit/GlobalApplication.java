package com.example.makeit;

import android.app.Application;
import android.util.Log;

import com.kakao.sdk.common.KakaoSdk;


public class GlobalApplication extends Application {
    private static GlobalApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //카카오 네이티브 앱 키로 초기화
        Log.d("kakao", "카카오 네이티브앱키 초기화 완료");
        KakaoSdk.init(this, "74281fcaf4a42a1b48ea418bc7d2fba5");
    }
}
