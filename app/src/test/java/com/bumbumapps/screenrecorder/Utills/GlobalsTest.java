package com.bumbumapps.screenrecorder.Utills;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.bumbumapps.screenrecorder.Service.FloatingWidgetService;
import com.bumbumapps.screenrecorder.Service.FloatingWidgetService2;

import org.junit.Test;

public class GlobalsTest {
    @Test
    public void storagePermissionMatchesPlatform() {
        assertNull(Globals.storagePermissionFor(36));
        assertNull(Globals.storagePermissionFor(33));
        assertEquals("android.permission.READ_EXTERNAL_STORAGE", Globals.storagePermissionFor(30));
        assertEquals("android.permission.WRITE_EXTERNAL_STORAGE", Globals.storagePermissionFor(28));
    }

    @Test
    public void thumbnailFrameIsInsideTheClip() {
        assertEquals(1_000_000L, Globals.thumbnailFrameUs(8_000));
        assertEquals(400_000L, Globals.thumbnailFrameUs(800));
        assertEquals(0L, Globals.thumbnailFrameUs(0));
    }

    @Test
    public void onlyOneFloatingBubblePerState() {
        assertEquals(FloatingWidgetService2.class, Globals.floatingWidgetFor(true));
        assertEquals(FloatingWidgetService.class, Globals.floatingWidgetFor(false));
    }

    // #103: rebuilding Home during a notification-started recording stopped it.
    @Test
    public void notificationRecordNeverTogglesARunningRecording() {
        org.junit.Assert.assertTrue(Globals.autoStartRecording(true, false));
        org.junit.Assert.assertFalse(Globals.autoStartRecording(true, true));
        org.junit.Assert.assertFalse(Globals.autoStartRecording(false, false));
    }
}
