package com.bumbumapps.hbrecorder;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;

import android.os.ResultReceiver;
import android.util.Log;

import java.io.FileDescriptor;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by HBiSoft on 13 Aug 2019
 * Copyright (c) 2019 . All rights reserved.
 */

@SuppressWarnings("deprecation")
public class ScreenRecordService extends Service {

    private static final String TAG = "ScreenRecordService";

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private int mResultCode;
    private Intent mResultData;
    private boolean isVideoHD;
    private boolean isAudioEnabled;
    private String path;

    private static MediaProjection mMediaProjection;
    private static MediaRecorder mMediaRecorder;
    private static VirtualDisplay mVirtualDisplay;
    private String name;
    private int audioBitrate;
    private int audioSamplingRate;
    private static String filePath;
    private static String fileName;
    private int audioSourceAsInt;
    private int videoEncoderAsInt;
    private boolean isCustomSettingsEnabled;
    private int videoFrameRate;
    private int videoBitrate;
    private int outputFormatAsInt;
    private int orientationHint;

    public final static String BUNDLED_LISTENER = "listener";
    private Uri returnedUri = null;
    private static Intent mIntent;

    static NotificationManager manager;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        String pauseResumeAction = intent.getAction();
        //Pause Recording
        if (pauseResumeAction != null && pauseResumeAction.equals("pause")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pauseRecording();
            }
        }
        //Resume Recording
        else if (pauseResumeAction != null && pauseResumeAction.equals("resume")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resumeRecording();
            }
        }
        //Start Recording
        else {
            //Get intent extras
            notificationShow(intent);
            startmediarecorder(intent);

        }
        return Service.START_STICKY;
       // return Service.START_REDELIVER_INTENT ;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startmediarecorder(final Intent intent) {
        //Init MediaRecorder
        try {
                initRecorder();
        } catch (Exception e) {
            ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
            Bundle bundle = new Bundle();
            bundle.putString("errorReason", Log.getStackTraceString(e));
            if (receiver != null) {
                receiver.send(Activity.RESULT_OK, bundle);
            }
        }

        //Init MediaProjection
        try {
            initMediaProjection();
        } catch (Exception e) {
            ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
            Bundle bundle = new Bundle();
            bundle.putString("errorReason", Log.getStackTraceString(e));
            if (receiver != null) {
                receiver.send(Activity.RESULT_OK, bundle);
            }
        }

        //Init VirtualDisplay
        try {
            initVirtualDisplay();
        } catch (Exception e) {
            ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
            Bundle bundle = new Bundle();
            bundle.putString("errorReason", Log.getStackTraceString(e));
            if (receiver != null) {
                receiver.send(Activity.RESULT_OK, bundle);
            }
        }

        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
                Bundle bundle = new Bundle();
                bundle.putString("error", "38");
                bundle.putString("errorReason", String.valueOf(i));
                if (receiver != null) {
                    receiver.send(Activity.RESULT_OK, bundle);
                }
            }
        });

        //Start Recording
        try {
            mMediaRecorder.start();
            ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
            Bundle bundle = new Bundle();
            bundle.putString("onStart", "111");
            if (receiver != null) {
                receiver.send(Activity.RESULT_OK, bundle);
            }
        } catch (Exception e) {
            // From the tests I've done, this can happen if another application is using the mic or if an unsupported video encoder was selected
            ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
            Bundle bundle = new Bundle();
            bundle.putString("error", "38");
            bundle.putString("errorReason", Log.getStackTraceString(e));
            if (receiver != null) {
                receiver.send(Activity.RESULT_OK, bundle);
            }
        }
    }

    //Pause Recording
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseRecording() {
        Log.d("checkclick", "pauseRecording");
        mMediaRecorder.pause();


    }

    //Resume Recording
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecording() {
        Log.d("checkclick", "resumeRecording");
        mMediaRecorder.resume();


    }

    //Set output format as int based on what developer has provided
    //It is important to provide one of the following and nothing else.
    private void setOutputformatAsInt(String outputFormat) {
        switch (outputFormat) {
            case "DEFAULT":
                outputFormatAsInt = 0;
                break;
            case "THREE_GPP":
                outputFormatAsInt = 1;
                break;
            case "MPEG_4":
                outputFormatAsInt = 2;
                break;
            case "AMR_NB":
                outputFormatAsInt = 3;
                break;
            case "AMR_WB":
                outputFormatAsInt = 4;
                break;
            case "AAC_ADTS":
                outputFormatAsInt = 6;
                break;
            case "MPEG_2_TS":
                outputFormatAsInt = 8;
                break;
            case "WEBM":
                outputFormatAsInt = 9;
                break;
            case "OGG":
                outputFormatAsInt = 11;
                break;
            default:
                outputFormatAsInt = 2;
        }
    }

    //Set video encoder as int based on what developer has provided
    //It is important to provide one of the following and nothing else.
    private void setvideoEncoderAsInt(String encoder) {
        switch (encoder) {
            case "DEFAULT":
                videoEncoderAsInt = 0;
                break;
            case "H263":
                videoEncoderAsInt = 1;
                break;
            case "H264":
                videoEncoderAsInt = 2;
                break;
            case "MPEG_4_SP":
                videoEncoderAsInt = 3;
                break;
            case "VP8":
                videoEncoderAsInt = 4;
                break;
            case "HEVC":
                videoEncoderAsInt = 5;
                break;
        }
    }

    //Set audio source as int based on what developer has provided
    //It is important to provide one of the following and nothing else.
    private void setAudioSourceAsInt(String audioSource) {
        switch (audioSource) {
            case "DEFAULT":
                audioSourceAsInt = 0;
                break;
            case "MIC":
                audioSourceAsInt = 1;
                break;
            case "VOICE_UPLINK":
                audioSourceAsInt = 2;
                break;
            case "VOICE_DOWNLINK":
                audioSourceAsInt = 3;
                break;
            case "VOICE_CALL":
                audioSourceAsInt = 4;
                break;
            case "CAMCODER":
                audioSourceAsInt = 5;
                break;
            case "VOICE_RECOGNITION":
                audioSourceAsInt = 6;
                break;
            case "VOICE_COMMUNICATION":
                audioSourceAsInt = 7;
                break;
            case "REMOTE_SUBMIX":
                audioSourceAsInt = 8;
                break;
            case "UNPROCESSED":
                audioSourceAsInt = 9;
                break;
            case "VOICE_PERFORMANCE":
                audioSourceAsInt = 10;
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaProjection() {
        mMediaProjection = ( (MediaProjectionManager) Objects.requireNonNull(getSystemService(Context.MEDIA_PROJECTION_SERVICE)) ).getMediaProjection(mResultCode, mResultData);
    }

    //Return the output file path as string
    public static String getFilePath() {
        return filePath;
    }

    //Return the name of the output file
    public static String getFileName() {
        return fileName;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initRecorder() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String curTime = formatter.format(curDate).replace(" ", "");
        String videoQuality = "HD";
        if (!isVideoHD) {
            videoQuality = "SD";
        }
        if (name == null) {
            name = videoQuality + curTime;
        }

        filePath = path + "/" + name + ".mp4";

        fileName = name + ".mp4";

        mMediaRecorder = new MediaRecorder();


        if (isAudioEnabled) {
            mMediaRecorder.setAudioSource(audioSourceAsInt);
        }
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(outputFormatAsInt);

        if (orientationHint != 400) {
            mMediaRecorder.setOrientationHint(orientationHint);
        }

        if (isAudioEnabled) {
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioEncodingBitRate(audioBitrate);
            mMediaRecorder.setAudioSamplingRate(audioSamplingRate);
        }

        mMediaRecorder.setVideoEncoder(videoEncoderAsInt);


        if (returnedUri != null) {
            try {
                ContentResolver contentResolver = getContentResolver();
                FileDescriptor inputPFD = Objects.requireNonNull(contentResolver.openFileDescriptor(returnedUri, "rw")).getFileDescriptor();
                mMediaRecorder.setOutputFile(inputPFD);
            } catch (Exception e) {
                ResultReceiver receiver = mIntent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
                Bundle bundle = new Bundle();
                bundle.putString("errorReason", Log.getStackTraceString(e));
                if (receiver != null) {
                    receiver.send(Activity.RESULT_OK, bundle);
                }
            }
        } else {
            mMediaRecorder.setOutputFile(filePath);
        }
        mMediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);

        if (!isCustomSettingsEnabled) {
            if (!isVideoHD) {
                mMediaRecorder.setVideoEncodingBitRate(12000000);
                mMediaRecorder.setVideoFrameRate(30);
            } else {
                mMediaRecorder.setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight);
                mMediaRecorder.setVideoFrameRate(60); //after setVideoSource(), setOutFormat()
            }
        } else {
            mMediaRecorder.setVideoEncodingBitRate(videoBitrate);
            mMediaRecorder.setVideoFrameRate(videoFrameRate);
        }


        mMediaRecorder.prepare();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        resetAll();
        callOnComplete();
    }


    private static void callOnComplete() {
        ResultReceiver receiver = mIntent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
        Bundle bundle = new Bundle();
        bundle.putString("onComplete", "Uri was passed");
        if (receiver != null) {
            receiver.send(Activity.RESULT_OK, bundle);
        }
    }

    public static void callOnClickHome() {
        ResultReceiver receiver = mIntent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
        Bundle bundle = new Bundle();
        bundle.putString("Home_action_perform", "Home_action_perform was passed");
        if (receiver != null) {
            receiver.send(Activity.RESULT_OK, bundle);
        }

    }


    public void notificationShow(Intent intent) {
        mIntent = intent;
        byte[] notificationSmallIcon = intent.getByteArrayExtra("notificationSmallBitmap");
        String notificationTitle = intent.getStringExtra("notificationTitle");
        String notificationDescription = intent.getStringExtra("notificationDescription");
        String notificationButtonText = intent.getStringExtra("notificationButtonText");
        orientationHint = intent.getIntExtra("orientation", 400);
        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        mScreenWidth = intent.getIntExtra("width", 0);
        mScreenHeight = intent.getIntExtra("height", 0);

        if (intent.getStringExtra("mUri") != null) {
            returnedUri = Uri.parse(intent.getStringExtra("mUri"));
        }

        if (mScreenHeight == 0 || mScreenWidth == 0) {
            HBRecorderCodecInfo hbRecorderCodecInfo = new HBRecorderCodecInfo();
            hbRecorderCodecInfo.setContext(this);
            mScreenHeight = hbRecorderCodecInfo.getMaxSupportedHeight();
            mScreenWidth = hbRecorderCodecInfo.getMaxSupportedWidth();
        }

        mScreenDensity = intent.getIntExtra("density", 1);
        isVideoHD = intent.getBooleanExtra("quality", true);
        isAudioEnabled = intent.getBooleanExtra("audio", true);
        path = intent.getStringExtra("path");
        name = intent.getStringExtra("fileName");
        String audioSource = intent.getStringExtra("audioSource");
        String videoEncoder = intent.getStringExtra("videoEncoder");
        videoFrameRate = intent.getIntExtra("videoFrameRate", 30);
        videoBitrate = intent.getIntExtra("videoBitrate", 40000000);

        if (audioSource != null) {
            setAudioSourceAsInt(audioSource);
        }
        if (videoEncoder != null) {
            setvideoEncoderAsInt(videoEncoder);
        }

        filePath = name;
        audioBitrate = intent.getIntExtra("audioBitrate", 128000);
        audioSamplingRate = intent.getIntExtra("audioSamplingRate", 44100);
        String outputFormat = intent.getStringExtra("outputFormat");
        if (outputFormat != null) {
            setOutputformatAsInt(outputFormat);
        }

        isCustomSettingsEnabled = intent.getBooleanExtra("enableCustomSettings", false);

        //Set notification notification button text if developer did not
        if (notificationButtonText == null) {
            notificationButtonText = "STOP RECORDING";
        }
        //Set notification bitrate if developer did not
        if (audioBitrate == 0) {
            audioBitrate = 128000;
        }
        //Set notification sampling rate if developer did not
        if (audioSamplingRate == 0) {
            audioSamplingRate = 44100;
        }
        //Set notification title if developer did not
        if (notificationTitle == null || notificationTitle.equals("")) {
            notificationTitle = "Recording your screen";
        }
        //Set notification description if developer did not
        if (notificationDescription == null || notificationDescription.equals("")) {
            notificationDescription = "Drag down to stop the recording";
        }


        // create notification
        Pair<Integer, Notification> notification = NotificationUtils.getNotification(this);
        startForeground(notification.first, notification.second);

        if (returnedUri == null) {
            if (path == null) {
                path = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));
            }
        }

    }

    public static void callOnClickExit() {
        manager.cancelAll();
        manager.notify();
    }

    public static void callOnClickPause() {
        ResultReceiver receiver = mIntent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
        Bundle bundle = new Bundle();
        bundle.putString("Pause_action_perform", "Pause_action_perform was passed");
        if (receiver != null) {
            receiver.send(Activity.RESULT_OK, bundle);
        }
    }

    public static void callOnClickStop() {
        ResultReceiver receiver = mIntent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
        Bundle bundle = new Bundle();
        bundle.putString("Stop_action_perform", "Stop_action_perform was passed");
        if (receiver != null) {
            receiver.send(Activity.RESULT_OK, bundle);
        }

    }

    public static void callOnClickResume() {
        ResultReceiver receiver = mIntent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
        Bundle bundle = new Bundle();
        bundle.putString("Resume_action_perform", "Resume_action_perform was passed");
        if (receiver != null) {
            receiver.send(Activity.RESULT_OK, bundle);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void resetAll() {
       // stopForeground(true);
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.reset();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
