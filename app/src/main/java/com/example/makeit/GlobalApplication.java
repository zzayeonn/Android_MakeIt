package com.example.makeit;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "74281fcaf4a42a1b48ea418bc7d2fba5");
    }
}
