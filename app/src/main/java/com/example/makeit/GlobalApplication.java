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
        KakaoSdk.init(this, getString(R.string.kakao_app_key));
        Log.d("kakao", "카카오 네이티브앱키 초기화 완료");
    }
}
