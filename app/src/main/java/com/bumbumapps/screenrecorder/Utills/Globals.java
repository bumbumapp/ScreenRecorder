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

    /**
     * Returns null on Android 13+: recordings are inserted through MediaStore and owned by the
     * app, so it can list and play them without READ_MEDIA_* (Play photo and video policy).
     */
    public static String storagePermissionFor(int sdkInt) {
        if (sdkInt >= 33) {
            return null;
        } else if (sdkInt >= 29) {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        return Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }
}
