package com.bumbumapps.screenrecorder.Utills;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GlobalsTest {
    @Test
    public void storagePermissionMatchesPlatform() {
        assertEquals("android.permission.READ_MEDIA_VIDEO", Globals.storagePermissionFor(36));
        assertEquals("android.permission.READ_MEDIA_VIDEO", Globals.storagePermissionFor(33));
        assertEquals("android.permission.READ_EXTERNAL_STORAGE", Globals.storagePermissionFor(30));
        assertEquals("android.permission.WRITE_EXTERNAL_STORAGE", Globals.storagePermissionFor(28));
    }
}
