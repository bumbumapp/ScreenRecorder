package com.bumbumapps.screenrecorder.Utills;

import android.Manifest;
import android.os.Build;

import com.bumbumapps.screenrecorder.Service.FloatingWidgetService;
import com.bumbumapps.screenrecorder.Service.FloatingWidgetService2;

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

    /** Thumbnail frame time in microseconds: 1 s in, or the middle of clips shorter than 2 s. */
    public static long thumbnailFrameUs(long durationMs) {
        if (durationMs <= 0) return 0;
        return Math.min(1000L, durationMs / 2) * 1000L;
    }

    /** Floating bubble to show: the stop/pause bubble while recording, the idle record bubble otherwise. */
    public static Class<?> floatingWidgetFor(boolean recording) {
        return recording ? FloatingWidgetService2.class : FloatingWidgetService.class;
    }
}
