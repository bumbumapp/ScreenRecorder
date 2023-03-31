package com.bumbumapps.screenrecorder.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.bumbumapps.screenrecorder.Service.ImageRecordService;
import com.bumbumapps.screenrecorder.Utills.Constance;

public class ActivityMediaProjectionPermission extends Activity {

     Context context;
    private static final int REQUEST_SCREENSHOT = 59706;
    private MediaProjectionManager mgr;

    public static Activity activity;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.demo_mediaprojection);
        context = ActivityMediaProjectionPermission.this;
        activity = this;

        mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mgr.createScreenCaptureIntent(),
                REQUEST_SCREENSHOT);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SCREENSHOT) {
                if (resultCode == RESULT_OK) {
                    Log.d("servicecheck", "RESULT_OK");
                    startService(ImageRecordService.getStartIntent(context, resultCode, data, Constance.pathScreenShotDirectory));

                }

            }
        }

    }


}
