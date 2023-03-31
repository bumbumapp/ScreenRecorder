package com.bumbumapps.screenrecorder.Utills;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import com.bumbumapps.screenrecorder.Fragment.FragmentHome;

public class MyPreference {
    private SharedPreferences sharedPreferences;

    public MyPreference(Context context){
        sharedPreferences = context.getSharedPreferences("name",Context.MODE_PRIVATE);
    }

    public void putBoolean(Boolean busy){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("busy",busy);
        editor.apply();
    }

    public boolean isBoolean(){
        return sharedPreferences.getBoolean("busy",false);
    }

}
