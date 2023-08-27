package com.bumbumapps.screenrecorder.Activities;

import static com.bumbumapps.screenrecorder.Utills.Globals.PERMISSIONS;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.bumbumapps.screenrecorder.R;

public class ActivitySplash extends AppCompatActivity {

    Handler handler;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context=ActivitySplash.this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            PERMISSIONS = Manifest.permission.READ_MEDIA_VIDEO;
        }else {
            PERMISSIONS = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        }
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(context, ActivityHome.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}
