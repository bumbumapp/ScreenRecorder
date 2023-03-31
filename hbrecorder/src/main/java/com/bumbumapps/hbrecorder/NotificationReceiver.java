package com.bumbumapps.hbrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class NotificationReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
     /*   Intent service = new Intent(context, ScreenRecordService.class);
        context.stopService(service);

*/
        String action = intent.getAction();
        if ("STOP_ACTION".equals(action)) {
            // ScreenRecordService.callOnClickStop();
            Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeIntent);
            ScreenRecordService.callOnClickStop();

            //  Toast.makeText(context, "stop CALLED", Toast.LENGTH_SHORT).show();
        } else if ("Home_ACTION".equals(action)) {
            ScreenRecordService.callOnClickHome();
        }  else if ("Pause_ACTION".equals(action)) {
            Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeIntent);
            ScreenRecordService.callOnClickPause();
        } else if ("Resume_ACTION".equals(action)) {
            Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeIntent);
            ScreenRecordService.callOnClickResume();
        }
    }
}
