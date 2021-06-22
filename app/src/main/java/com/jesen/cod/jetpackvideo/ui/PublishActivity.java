package com.jesen.cod.jetpackvideo.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.libnavannotation.ActivityDestination;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
    }
}