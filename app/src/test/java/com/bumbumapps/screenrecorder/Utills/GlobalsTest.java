package com.bumbumapps.screenrecorder.Utills;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class GlobalsTest {
    @Test
    public void storagePermissionMatchesPlatform() {
        assertNull(Globals.storagePermissionFor(36));
        assertNull(Globals.storagePermissionFor(33));
        assertEquals("android.permission.READ_EXTERNAL_STORAGE", Globals.storagePermissionFor(30));
        assertEquals("android.permission.WRITE_EXTERNAL_STORAGE", Globals.storagePermissionFor(28));
    }
}
