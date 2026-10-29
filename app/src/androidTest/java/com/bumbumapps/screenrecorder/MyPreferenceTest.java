package com.bumbumapps.screenrecorder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.bumbumapps.screenrecorder.Utills.MyPreference;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MyPreferenceTest {
    @Test
    public void recordAudioChoiceSurvivesReopen() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        new MyPreference(context).setRecordAudio(false);
        // a new instance reads what the home screen gets after the app is reopened
        assertFalse(new MyPreference(context).isRecordAudio());
        new MyPreference(context).setRecordAudio(true);
        assertTrue(new MyPreference(context).isRecordAudio());
    }
}
