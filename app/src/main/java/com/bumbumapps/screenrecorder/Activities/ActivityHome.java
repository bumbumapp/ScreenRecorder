package com.bumbumapps.screenrecorder.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Gallery;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumbumapps.screenrecorder.Utills.AdsLoader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumbumapps.screenrecorder.Fragment.FragmentGallery;
import com.bumbumapps.screenrecorder.Fragment.FragmentHome;
import com.bumbumapps.screenrecorder.Fragment.FragmentMyRecording;
import com.bumbumapps.screenrecorder.Fragment.FragmentSetting;
import com.bumbumapps.screenrecorder.R;
import com.bumbumapps.screenrecorder.Utills.Constance;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityHome extends AppCompatActivity implements View.OnClickListener,BottomNavigationView.OnNavigationItemSelectedListener {

    Context context;
    AdView mAdView;

    public static ActivityHome instance = null;
    Timer timer;
    BottomNavigationView bottomNavigationView;
    FragmentTransaction fragmentTransaction;
    TimerTask hourlyTask;
    private final String TAG = ActivityHome.class.getSimpleName();
    LinearLayout facbook_ad_banner;
    public ActivityHome() {
        instance = ActivityHome.this;
    }

    public static synchronized ActivityHome getInstance() {
        if (instance == null) {
            instance = new ActivityHome();
        }
        return instance;
    }



    int selectedcolor, unselectedcolor;
    String check_fragmentname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        context = ActivityHome.this;
        check_fragmentname = String.valueOf(getIntent().getStringExtra("check_fragmentname"));

        init();
        AdsLoader.displayInterstitial(this);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

//        for_mInterstitialAd();
        selectedcolor = Color.parseColor("#E416A9");
        unselectedcolor = Color.parseColor("#66BC24");
        Log.d("check_intent", "homeactivity");

        loadFragment(new FragmentHome());




        if (check_fragmentname.equals("fragment_myrecording")) {
            loadFragment(new FragmentMyRecording());
        } else if (check_fragmentname.equals("FragmentGallery")) {
            Log.d("mfmfmf","ActivityHome");
            loadFragment(new FragmentGallery());
        } else {
            Log.d("aaaaaaa", "Invalid Fragment");
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);
    }



    @Override
    public void onClick(View view) {


    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_framelayout, fragment);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.hide(fragment);
        fragmentTransaction.show(fragment);
        //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }




    public void StartTimer() {
        timer = new Timer();
        hourlyTask = new TimerTask() {
            @Override
            public void run() {
                if (!Constance.isFirstTimeOpen) {
                    Constance.AllowToOpenAdvertise = true;
                    stopTask();
                } else {
                    Constance.isFirstTimeOpen = false;
                }
            }
        };

        Constance.isFirstTimeOpen = true;
        if (timer != null) {
            timer.schedule(hourlyTask, 0, 1000 * 60);
        }
    }

    public void stopTask() {
        if (hourlyTask != null) {

            Log.d("TIMER", "timer canceled");
            hourlyTask.cancel();
        }
    }

    @Override
    public void onBackPressed() {


            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
            final AlertDialog alertDialog = builder.create();
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setTitle("Close App ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finishAffinity();

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();

                        }
                    })

                    .show();
        }


    public void init() {

        bottomNavigationView=findViewById(R.id.bottomNavigationView);
        mAdView = findViewById(R.id.adView);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("mxmxmxm", "onnewintent");
        checkAction(intent);
        super.onNewIntent(intent);
    }

    private void checkAction(Intent intent) {
        String action = intent.getAction();
        Log.d("mxmxmxm", "action: " + action);
        switch (action) {
            case "record_intent":
                Constance.notificationrecordclick=true;
                loadFragment(new FragmentHome());
                Log.d("mxmxmxm", "record_intent");
                // FragmentHome.exitNotificationclick();
                break;

        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.home:
                loadFragment(new FragmentHome());


                return true;
            case R.id.record:


                loadFragment(new FragmentMyRecording());


                return true;
            case R.id.screenshot:

                loadFragment(new FragmentGallery());


                return true;

            case R.id.settings:


                // startActivity(new Intent(context, SettingsActivity.class));
                loadFragment(new FragmentSetting());




                return true;

            default:
        }
        return false;
    }
}
