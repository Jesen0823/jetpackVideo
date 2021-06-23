package com.jesen.cod.jetpackvideo;

import android.app.Application;

import com.jesen.cod.libnetwork.ApiService;

public class JetVideoApp extends Application {

    private static final String BASE_URL = "http://123.56.232.18:8080/serverdemo";

    @Override
    public void onCreate() {
        super.onCreate();
        ApiService.init(BASE_URL, null);
    }


}
