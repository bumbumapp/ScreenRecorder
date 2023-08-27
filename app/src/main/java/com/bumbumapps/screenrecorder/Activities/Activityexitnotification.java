package com.bumbumapps.screenrecorder.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.bumbumapps.screenrecorder.Fragment.FragmentHome;

import static com.bumbumapps.screenrecorder.Fragment.FragmentHome.NotificationID;

public class Activityexitnotification extends Activity {

    Context context;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = Activityexitnotification.this;


        FragmentHome.notificationManager.cancel(NotificationID);
        FragmentHome.notificationManager.cancelAll();
        finish();
       }


}
