package com.bumbumapps.screenrecorder.Fragment;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.bumbumapps.hbrecorder.HBRecorder;
import com.bumbumapps.hbrecorder.HBRecorderCodecInfo;
import com.bumbumapps.hbrecorder.HBRecorderListener;
import com.bumbumapps.hbrecorder.NotificationReceiver;
import com.bumbumapps.screenrecorder.Activities.ActivityHome;
import com.bumbumapps.screenrecorder.Activities.ActivityMediaProjectionPermission;
import com.bumbumapps.screenrecorder.Activities.Activityexitnotification;
import com.bumbumapps.screenrecorder.R;
import com.bumbumapps.screenrecorder.Service.FloatingWidgetService;
import com.bumbumapps.screenrecorder.Service.FloatingWidgetService2;
import com.bumbumapps.screenrecorder.Service.ImageRecordService;
import com.bumbumapps.screenrecorder.Utills.Constance;
import com.bumbumapps.screenrecorder.Utills.MyPreference;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class FragmentHome extends Fragment implements HBRecorderListener {
    static Context context;
    View view;
    private InterstitialAd interstitialAd;
    //Notification
    RemoteViews notificationLayoutExpanded;
    private static Notification notification;
    public static NotificationManager notificationManager;
    public static int NotificationID = 1005;
    private static NotificationCompat.Builder mBuilder;
    static boolean mediarunning = false;
    private MyPreference myPreference;
    //Permissions
    private static final int DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE = 1222;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int SCREEN_RECORD_REQUEST_CODE = 777;
    private static final int REQUEST_CODE_SCREENSHOT = 100;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private boolean hasPermissions = false;

    //Declare HBRecorder
    private HBRecorder hbRecorder;

    //Start/Stop Button
    // private Button startbtn;
    private com.google.android.material.floatingactionbutton.FloatingActionButton startbtn;

    //HD/SD quality
    private RadioGroup radioGroup;

    //Should record/show audio/notification
    //  private CheckBox recordAudioCheckBox;
    private Switch recordAudioCheckBox;

    //Reference to checkboxes and radio buttons
    boolean wasHDSelected = true;
    boolean isAudioEnabled = true;

    //Should custom settings be used
    Switch custom_settings_switch, floating_settings_switch;

   /* public static FragmentHome instance = null;

    public FragmentHome() {
        instance = FragmentHome.this;
    }

    public static synchronized FragmentHome getInstance() {
        if (instance == null) {
            instance = new FragmentHome();
        }
        return instance;
    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragmenthome, container, false);
        context = getContext();
        initViews();
        setOnClickListeners();
        setRadioGroupCheckListener();
        setRecordAudioCheckBoxListener();
        RunNotification();
        getpermission();
        setUpInterstitialAd();
//        scheduleInterstitial();

        if (checkDrawOverlayPermission()) {
            startFloatingWidgetService();
            //startService(new Intent(this, PowerButtonService.class));
        }

        floating_settings_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Log.d("floating", "checked true");
                    // checkDisplayOverOtherAppsPermission();
                    if (checkDrawOverlayPermission()) {
                        startFloatingWidgetService();
                        //startService(new Intent(this, PowerButtonService.class));
                    }
                } else {
                    Constance.isfloatingswitchEnabled = false;
                    floating_settings_switch.setChecked(Constance.isfloatingswitchEnabled);
                    context.stopService(new Intent(context, FloatingWidgetService.class));
                    Log.d("floating", "checked false");
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Init HBRecorder
            hbRecorder = new HBRecorder(context, this);

            //When the user returns to the application, some UI changes might be necessary,
            //check if recording is in progress and make changes accordingly
            if (hbRecorder.isBusyRecording()) {
                // Toast.makeText(context, "recording stop click again", Toast.LENGTH_LONG).show();
                //startbtn.setText(R.string.stop_recording);
                hbRecorder.stopScreenRecording();
                mediarunning = false;
                myPreference.putBoolean(false);
                RunNotification();
                Constance.notificationrecordclick = false;
                //  startbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_close));
                startbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_recode));
            }
        }


        // Examples of how to use the HBRecorderCodecInfo class to get codec info
        HBRecorderCodecInfo hbRecorderCodecInfo = new HBRecorderCodecInfo();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int mWidth = hbRecorder.getDefaultWidth();
            int mHeight = hbRecorder.getDefaultHeight();
            String mMimeType = "video/avc";
            int mFPS = 30;
            if (hbRecorderCodecInfo.isMimeTypeSupported(mMimeType)) {
                String defaultVideoEncoder = hbRecorderCodecInfo.getDefaultVideoEncoderName(mMimeType);
                boolean isSizeAndFramerateSupported = hbRecorderCodecInfo.isSizeAndFramerateSupported(mWidth, mHeight, mFPS, mMimeType, ORIENTATION_PORTRAIT);
                Log.e("EXAMPLE", "THIS IS AN EXAMPLE OF HOW TO USE THE (HBRecorderCodecInfo) TO GET CODEC INFO:");
                Log.e("HBRecorderCodecInfo", "defaultVideoEncoder for (" + mMimeType + ") -> " + defaultVideoEncoder);
                Log.e("HBRecorderCodecInfo", "MaxSupportedFrameRate -> " + hbRecorderCodecInfo.getMaxSupportedFrameRate(mWidth, mHeight, mMimeType));
                Log.e("HBRecorderCodecInfo", "MaxSupportedBitrate -> " + hbRecorderCodecInfo.getMaxSupportedBitrate(mMimeType));
                Log.e("HBRecorderCodecInfo", "isSizeAndFramerateSupported @ Width = " + mWidth + " Height = " + mHeight + " FPS = " + mFPS + " -> " + isSizeAndFramerateSupported);
                Log.e("HBRecorderCodecInfo", "isSizeSupported @ Width = " + mWidth + " Height = " + mHeight + " -> " + hbRecorderCodecInfo.isSizeSupported(mWidth, mHeight, mMimeType));
                Log.e("HBRecorderCodecInfo", "Default Video Format = " + hbRecorderCodecInfo.getDefaultVideoFormat());

                HashMap<String, String> supportedVideoMimeTypes = hbRecorderCodecInfo.getSupportedVideoMimeTypes();
                for (Map.Entry<String, String> entry : supportedVideoMimeTypes.entrySet()) {
                    Log.e("HBRecorderCodecInfo", "Supported VIDEO encoders and mime types : " + entry.getKey() + " -> " + entry.getValue());
                }

                HashMap<String, String> supportedAudioMimeTypes = hbRecorderCodecInfo.getSupportedAudioMimeTypes();
                for (Map.Entry<String, String> entry : supportedAudioMimeTypes.entrySet()) {
                    Log.e("HBRecorderCodecInfo", "Supported AUDIO encoders and mime types : " + entry.getKey() + " -> " + entry.getValue());
                }

                ArrayList<String> supportedVideoFormats = hbRecorderCodecInfo.getSupportedVideoFormats();
                for (int j = 0; j < supportedVideoFormats.size(); j++) {
                    Log.e("HBRecorderCodecInfo", "Available Video Formats : " + supportedVideoFormats.get(j));
                }
            } else {
                Log.e("HBRecorderCodecInfo", "MimeType not supported");
            }

        }

        if (Constance.notificationrecordclick) {
            custom_settings_switch.setChecked(true);
            performclickstartbtn();

        } else {
            Log.d("mzmzmzm", "Not notification record clciked");
        }
        if (Constance.notificationscreenshotclick) {
            startScreenShot();

        } else {
            Log.d("mzmzmzm", "Not notification record clciked");
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                setUpInterstitialAd();
            }

        });
        return view;


    }
    private void setUpInterstitialAd() {

        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId("ca-app-pub-8444865753152507/9775482627");
        interstitialAd.loadAd(new AdRequest.Builder().build());




    }



    private void displayInterstitial() {

        if (interstitialAd != null) {

            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        }


    }


    //Create Folder
    //Only call this on Android 9 and lower (getExternalStoragePublicDirectory is deprecated)
    //This can still be used on Android 10> but you will have to add android:requestLegacyExternalStorage="true" in your Manifest
    private void createFolder() {
        File f1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Screen Recorder");
        if (!f1.exists()) {
            if (f1.mkdirs()) {
                Log.i("Folder ", "created");
            }
        }

    }

    //Init Views
    private void initViews() {
        myPreference=new MyPreference(context);
        startbtn = view.findViewById(R.id.button_start);
        radioGroup = view.findViewById(R.id.radio_group);
        // recordAudioCheckBox = view.findViewById(R.id.audio_check_box);
        recordAudioCheckBox = view.findViewById(R.id.record_audio);
        custom_settings_switch = view.findViewById(R.id.custom_settings_switch);
        floating_settings_switch = view.findViewById(R.id.floating_settings_switch);
    }

    //Start Button OnClickListener
    private void setOnClickListeners() {
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performclickstartbtn();

            }
        });
    }


    public void performclickstartbtn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //first check if permissions was granted
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
                hasPermissions = true;

                if (hasPermissions) {
                    //check if recording is in progress
                    //and stop it if it is
                    if (hbRecorder.isBusyRecording()) {
                        hbRecorder.stopScreenRecording();
                        displayInterstitial();
                        mediarunning = false;
                        myPreference.putBoolean(false);
                        RunNotification();
                        Constance.notificationrecordclick = false;
                        Log.d("checkclick", "ifhome");
                        startbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_close));

                    }
                    //else start recording
                    else {
                        startRecordingScreen();
                    }
                } else {
                    Log.d("hasPermissions", "hasPermissions false");
                    showLongToast("Audio Recording permission denied");
                }
            }
           /* else
            {
                Log.d("hasPermissions","permission else");
                showLongToast("Audio Recording permission denied");
            }*/

        } else {
            startbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_recode));
            showLongToast("This library requires API 21>");
        }
    }

    //Check if HD/SD Video should be recorded
    private void setRadioGroupCheckListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.hd_button:
                        //Ser HBRecorder to HD
                        wasHDSelected = true;

                        break;
                    case R.id.sd_button:
                        //Ser HBRecorder to SD
                        wasHDSelected = false;
                        break;
                }
            }
        });
    }

    //Check if audio should be recorded
    private void setRecordAudioCheckBoxListener() {
        recordAudioCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Enable/Disable audio
                isAudioEnabled = isChecked;
            }
        });
    }


    @Override
    public void HBRecorderOnStart() {
        Log.e("HBRecorder", "HBRecorderOnStart called");

    }

    @Override
    public void HBRecorderOnComplete() {
        Log.d("asasasas", "stop recording click");
        //  startbtn.setText(R.string.start_recording);
        mediarunning = false;
        myPreference.putBoolean(false);
        RunNotification();
        Constance.notificationrecordclick = false;
        startbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_recode));
        showLongToast("Recording Saved Successfully");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Update gallery depending on SDK Level
            if (hbRecorder.wasUriSet()) {
                updateGalleryUri();
            } else {
                refreshGalleryFile();
            }
        }
    }

    @Override
    public void HBRecorderOnError(int errorCode, String reason) {

        if (errorCode == 38) {
            showLongToast("Some settings are not supported by your device");
            mediarunning = false;
            myPreference.putBoolean(false);
            RunNotification();
            Constance.notificationrecordclick = false;


        } else {
           // showLongToast("HBRecorderOnError - See Log");
           // showLongToast("RecorderOnError - See Log");
            mediarunning = false;
            myPreference.putBoolean(false);
            RunNotification();
            Constance.notificationrecordclick = false;

            Log.e("HBRecorderOnError", reason);
        }

        //startbtn.setText(R.string.start_recording);
        startbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_recode));
    }

    @Override
    public void backtohome() {
        Log.d("check_intent", "backtohome");
        startActivity(new Intent(context, ActivityHome.class));
    }

    @Override
    public void backtomyrecording() {
        mediarunning = false;
        myPreference.putBoolean(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(context)) {
                context.stopService(new Intent(context, FloatingWidgetService2.class));
                startFloatingWidgetService();
            }
        }
       /* if (checkDrawOverlayPermission()) {
            context.stopService(new Intent(context, FloatingWidgetService2.class));
            startFloatingWidgetService();
        }*/
//        Intent i = new Intent(context, ActivityHome.class);
//        i.putExtra("check_fragmentname", "fragment_myrecording");
//        startActivity(i);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void refreshGalleryFile() {
        MediaScannerConnection.scanFile(context,
                new String[]{hbRecorder.getFilePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    private void updateGalleryUri() {
        contentValues.clear();
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
        context.getContentResolver().update(mUri, contentValues, null, null);
    }

    //Start recording screen
    //It is important to call it like this
    //hbRecorder.startScreenRecording(data); should only be called in onActivityResult
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecordingScreen() {
        if (custom_settings_switch.isChecked()) {
            //WHEN SETTING CUSTOM SETTINGS YOU MUST SET THIS!!!
            hbRecorder.enableCustomSettings();
            customSettings();
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
            startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
            // startbtn.setText(R.string.stop_recording);

        } else {
            quickSettings();
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
            startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
            //  startbtn.setText(R.string.stop_recording);
            startbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_close));

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void customSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //Is audio enabled
        boolean audio_enabled = prefs.getBoolean("key_record_audio", true);
        hbRecorder.isAudioEnabled(audio_enabled);

        //Audio Source
        String audio_source = prefs.getString("key_audio_source", null);
        if (audio_source != null) {
            switch (audio_source) {
                case "0":
                    hbRecorder.setAudioSource("DEFAULT");
                    break;
                case "1":
                    hbRecorder.setAudioSource("CAMCODER");
                    break;
                case "2":
                    hbRecorder.setAudioSource("MIC");
                    break;
            }
        }

        //Video Encoder
        String video_encoder = prefs.getString("key_video_encoder", null);
        if (video_encoder != null) {
            switch (video_encoder) {
                case "0":
                    hbRecorder.setVideoEncoder("DEFAULT");
                    break;
                case "1":
                    hbRecorder.setVideoEncoder("H264");
                    break;
                case "2":
                    hbRecorder.setVideoEncoder("H263");
                    break;
                case "3":
                    hbRecorder.setVideoEncoder("HEVC");
                    break;
                case "4":
                    hbRecorder.setVideoEncoder("MPEG_4_SP");
                    break;
                case "5":
                    hbRecorder.setVideoEncoder("VP8");
                    break;
            }
        }

        //NOTE - THIS MIGHT NOT BE SUPPORTED SIZES FOR YOUR DEVICE
        //Video Dimensions
        String video_resolution = prefs.getString("key_video_resolution", null);
        if (video_resolution != null) {
            switch (video_resolution) {
                case "0":
                    hbRecorder.setScreenDimensions(426, 240);
                    break;
                case "1":
                    hbRecorder.setScreenDimensions(640, 360);
                    break;
                case "2":
                    hbRecorder.setScreenDimensions(854, 480);
                    break;
                case "3":
                    hbRecorder.setScreenDimensions(1280, 720);
                    break;
                case "4":
                    hbRecorder.setScreenDimensions(1920, 1080);
                    break;
            }
        }

        //Video Frame Rate
        String video_frame_rate = prefs.getString("key_video_fps", null);
        if (video_frame_rate != null) {
            switch (video_frame_rate) {
                case "0":
                    hbRecorder.setVideoFrameRate(60);
                    break;
                case "1":
                    hbRecorder.setVideoFrameRate(50);
                    break;
                case "2":
                    hbRecorder.setVideoFrameRate(48);
                    break;
                case "3":
                    hbRecorder.setVideoFrameRate(30);
                    break;
                case "4":
                    hbRecorder.setVideoFrameRate(25);
                    break;
                case "5":
                    hbRecorder.setVideoFrameRate(24);
                    break;
            }
        }

        //Video Bitrate
        String video_bit_rate = prefs.getString("key_video_bitrate", null);
        if (video_bit_rate != null) {
            switch (video_bit_rate) {
                case "1":
                    hbRecorder.setVideoBitrate(12000000);
                    break;
                case "2":
                    hbRecorder.setVideoBitrate(8000000);
                    break;
                case "3":
                    hbRecorder.setVideoBitrate(7500000);
                    break;
                case "4":
                    hbRecorder.setVideoBitrate(5000000);
                    break;
                case "5":
                    hbRecorder.setVideoBitrate(4000000);
                    break;
                case "6":
                    hbRecorder.setVideoBitrate(2500000);
                    break;
                case "7":
                    hbRecorder.setVideoBitrate(1500000);
                    break;
                case "8":
                    hbRecorder.setVideoBitrate(1000000);
                    break;
            }
        }

        //Output Format
        String output_format = prefs.getString("key_output_format", null);
        if (output_format != null) {
            switch (output_format) {
                case "0":
                    hbRecorder.setOutputFormat("DEFAULT");
                    break;
                case "1":
                    hbRecorder.setOutputFormat("MPEG_4");
                    break;
                case "2":
                    hbRecorder.setOutputFormat("THREE_GPP");
                    break;
                case "3":
                    hbRecorder.setOutputFormat("WEBM");
                    break;
            }
        }

    }

    //Get/Set the selected settings
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void quickSettings() {
        hbRecorder.setAudioBitrate(128000);
        hbRecorder.setAudioSamplingRate(44100);
        hbRecorder.recordHDVideo(wasHDSelected);
        hbRecorder.isAudioEnabled(isAudioEnabled);
        //Customise Notification
        hbRecorder.setNotificationSmallIcon(drawable2ByteArray(R.drawable.icon));
        hbRecorder.setNotificationTitle("Recording your screen");
        hbRecorder.setNotificationDescription("Drag down to stop the recording");

    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(context, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    //Check if permissions was granted
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    //Handle permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
                } else {
                    hasPermissions = false;
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                }
                break;
            case PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasPermissions = true;
                    /*//Permissions was provided
                    //Start screen recording
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startRecordingScreen();
                    }*/
                } else {
                    hasPermissions = false;
                    showLongToast("No permission for " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                break;
            case REQUEST_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createscreenshotfolder();
                } else {
                    showLongToast("No permission for " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    //Set file path or Uri depending on SDK version
                    setOutputPath();
                    //Start screen recording
                    hbRecorder.startScreenRecording(data, resultCode, getActivity());
                    showLongToast("Recording Start...");
                    mediarunning = true;
                    myPreference.putBoolean(true);
                    RunNotification();
                    Constance.notificationrecordclick = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(context)) {
                            context.stopService(new Intent(context, FloatingWidgetService.class));
                            context.startService(new Intent(context, FloatingWidgetService2.class));
                        }
                    }
                  /*  if (checkDrawOverlayPermission()) {
                        context.stopService(new Intent(context, FloatingWidgetService.class));
                        context.startService(new Intent(context, FloatingWidgetService2.class));
                    }*/
                    startbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_close));

                } else {
                    Constance.notificationrecordclick = false;
                    startbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_recode));

                }
            }
            if (requestCode == REQUEST_CODE_SCREENSHOT) {
                if (resultCode == RESULT_OK) {
                    Log.d("screenshot_check", "REQUEST_CODE_SCREENSHOT :");

                    context.startService(ImageRecordService.getStartIntent(context, resultCode, data, Constance.pathScreenShotDirectory));
                    Constance.notificationscreenshotclick = false;
                } else {
                    Log.d("check_permission", "screen capture permission cancel");
                }
            }
            if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE) {
              /*  Log.d("DRAW_OVER","DRAW_OVER"+resultCode);
                //Check if the permission is granted or not.
                if (resultCode == RESULT_OK)
                {
                    Log.d("DRAW_OVER","DRAW_OVER_if"+resultCode);

                    Log.d("DRAW_OVER","if");
                    //If permission granted start floating widget service
                    startFloatingWidgetService();

                }
                else{
                    Log.d("DRAW_OVER","DRAW_OVER_else"+resultCode);

                    Log.d("DRAW_OVER","false");

                    //Permission is not available then display toast
                    showLongToast(getResources().getString(R.string.draw_other_app_permission_denied));

                }*/

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(context)) {
                        startFloatingWidgetService();
                        // startService(new Intent(this, PowerButtonService.class));
                    } else {
                        Constance.isfloatingswitchEnabled = false;
                        floating_settings_switch.setChecked(false);
                    }
                }

            }

        }
    }

    //For Android 10> we will pass a Uri to HBRecorder
    //This is not necessary - You can still use getExternalStoragePublicDirectory
    //But then you will have to add android:requestLegacyExternalStorage="true" in your Manifest
    //IT IS IMPORTANT TO SET THE FILE NAME THE SAME AS THE NAME YOU USE FOR TITLE AND DISPLAY_NAME
    ContentResolver resolver;
    ContentValues contentValues;
    Uri mUri;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setOutputPath() {
        String filename = generateFileName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver = context.getContentResolver();
            contentValues = new ContentValues();
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "Screen Recorder");
            contentValues.put(MediaStore.Video.Media.TITLE, filename);
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            mUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            //FILE NAME SHOULD BE THE SAME
            hbRecorder.setFileName(filename);
            hbRecorder.setOutputUri(mUri);
        } else {
            createFolder();
            hbRecorder.setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/Screen Recorder");
            //hbRecorder.setOutputPath(Constance.PathFileDirectory);
        }
    }

    //Generate a timestamp to be used as a file name
    private String generateFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate).replace(" ", "");
    }

    //Show Toast
    private void showLongToast(final String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    //drawable to byte[]
    private byte[] drawable2ByteArray(@DrawableRes int drawableId) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), drawableId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static WindowManager.LayoutParams generateFullScreenParams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,

                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,

                PixelFormat.TRANSLUCENT);
    }

    private void RunNotification() {

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), "notify_001");

        if (mediarunning) {
            notificationLayoutExpanded = new RemoteViews(context.getPackageName(), com.bumbumapps.hbrecorder.R.layout.notification_large2);
            //stop btn clicked
            Intent stopIntent = new Intent(context, NotificationReceiver.class);
            stopIntent.setAction("STOP_ACTION");
            PendingIntent stoppendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayoutExpanded.setOnClickPendingIntent(com.bumbumapps.hbrecorder.R.id.ll_stopbtn, stoppendingIntent);

            //Pause btn clicked
            Intent pauseIntent = new Intent(context, NotificationReceiver.class);
            pauseIntent.setAction("Pause_ACTION");
            PendingIntent pausependingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayoutExpanded.setOnClickPendingIntent(com.bumbumapps.hbrecorder.R.id.ll_pausebtn, pausependingIntent);
            //resume btn clicked
            Intent resumeIntent = new Intent(context, NotificationReceiver.class);
            resumeIntent.setAction("Resume_ACTION");
            PendingIntent resumependingIntent = PendingIntent.getBroadcast(context, 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayoutExpanded.setOnClickPendingIntent(com.bumbumapps.hbrecorder.R.id.ll_resume, resumependingIntent);

        } else {
            notificationLayoutExpanded = new RemoteViews(context.getPackageName(), com.bumbumapps.hbrecorder.R.layout.notification_large);
            //home btn clicked
            Intent homeIntent = new Intent(context, ActivityHome.class);
            PendingIntent homependingIntent = PendingIntent.getActivity(context, 0, homeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayoutExpanded.setOnClickPendingIntent(com.bumbumapps.hbrecorder.R.id.ll_notification_home, homependingIntent);

            //Record btn clicked
            Intent recordIntent = new Intent(context, ActivityHome.class);
            recordIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            recordIntent.setAction("record_intent");
            recordIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent recordpendingIntent = PendingIntent.getActivity(context, 0, recordIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE );
            notificationLayoutExpanded.setOnClickPendingIntent(com.bumbumapps.hbrecorder.R.id.ll_record, recordpendingIntent);

            //exit btn clicked
            Intent[]intents=new Intent[2];
            intents[0] = new Intent(Intent.ACTION_MAIN);
            intents[1]=new Intent(context,Activityexitnotification.class);
            intents[0].addCategory(Intent.CATEGORY_HOME);
            intents[1].setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent exit_pendingintent = PendingIntent.getActivities(context, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayoutExpanded.setOnClickPendingIntent(com.bumbumapps.hbrecorder.R.id.ll_notification_exit, exit_pendingintent);

            //Scrennshot btn clicked
            Intent ScrennshotIntent = new Intent(context, ActivityMediaProjectionPermission.class);
            ScrennshotIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //  ScrennshotIntent.setAction("Scrennshot_intent");
            // ScrennshotIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent ScrennshotpendingIntent = PendingIntent.getActivity(context, 0, ScrennshotIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayoutExpanded.setOnClickPendingIntent(com.bumbumapps.hbrecorder.R.id.ll_screenshot, ScrennshotpendingIntent);

        }

        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);


        // mBuilder.setOnlyAlertOnce(true);
        mBuilder.build().flags = Notification.FLAG_NO_CLEAR | Notification.PRIORITY_HIGH;
        mBuilder.setContent(notificationLayoutExpanded);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(channelId, "channel name", NotificationManager.IMPORTANCE_MIN);
            notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        notification = mBuilder.build();
        mBuilder.setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setVibrate(null);
        notificationManager.notify(NotificationID,notification);
    }

    public void checkDisplayOverOtherAppsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE);
        } else {
            //If permission is granted start floating widget service
            startFloatingWidgetService();
        }

    }

    /*  Start Floating widget service and finish current activity */
    private void startFloatingWidgetService() {
        Constance.isfloatingswitchEnabled = true;
        floating_settings_switch.setChecked(Constance.isfloatingswitchEnabled);
        context.startService(new Intent(context, FloatingWidgetService.class));
        // context.startActivity(new Intent(context, ActivityFloatingWidget.class));
        // finish();
    }






    public void startScreenShot() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
            hasPermissions = true;
            if (hasPermissions) {
                MediaProjectionManager mProjectionManager =
                        (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREENSHOT);

            } else {
                showLongToast("permission not granted");
            }
        } else {
            showLongToast("Permission not allowed");
        }

    }

    public void createscreenshotfolder() {
        File f2 = new File(Constance.pathScreenShotDirectory);
        if (!f2.exists()) {
            if (f2.mkdirs()) {
                Log.i("Folder ", "created");
            }
        }
    }

    public void getpermission() {
        if (( ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) && ( ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )) {
            if (( ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ) && ( ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) )) {

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        } else {
            createscreenshotfolder();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        floating_settings_switch.setChecked(Constance.isfloatingswitchEnabled);

    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }


}
