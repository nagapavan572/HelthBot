package com.example.android.logindemo;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

public class chatbotapp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());
    }
}
