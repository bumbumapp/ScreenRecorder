package com.bumbumapps.screenrecorder.Utills;

import android.Manifest;
import android.os.Build;

public class Globals {
    /**
     * Storage permission needed to list/save recordings on this device. Computed statically: it
     * used to be assigned only in ActivitySplash, so it was null (crash) when the process was
     * recreated directly into ActivityHome, e.g. from the recording notification.
     */
    public static final String PERMISSIONS = storagePermissionFor(Build.VERSION.SDK_INT);

    public static String storagePermissionFor(int sdkInt) {
        if (sdkInt >= 33) {
            return Manifest.permission.READ_MEDIA_VIDEO;
        } else if (sdkInt >= 29) {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        return Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }
}
