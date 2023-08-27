package com.bumbumapps.screenrecorder.Utills;

import android.os.Environment;

import com.bumbumapps.screenrecorder.Model.ModelMyGallery;

import java.util.ArrayList;

public class Constance {
    public static boolean AllowToOpenAdvertise = false;
    public static boolean isFirstTimeOpen = true;
    public static boolean notificationrecordclick = false;
    public static boolean notificationscreenshotclick = false;
    public static boolean isfloatingswitchEnabled = false;

    //public static String adType = "facebook";
    public static String adType = "Ad Mob";

    public static ArrayList<ModelMyGallery> modelMyGalleries;

    public static String PolicyUrl = "https://www.google.com/";

    public static String shareapp_url = "Check out the App at: https://play.google.com/store/apps/details?id=";

    public static String rateapp = "http://play.google.com/store/apps/details?id=";

    public static String aboutUs = "Your content should be the voice of your brand. And, at Yasza Media, we make sure that we surpass your expectations while meeting your audience’s needs. We believe content which is engaging, relevant, and informative is the core to establishing your brand’s reputation online.\n" +
            "This is why we invest time in researching your target audience. We take into consideration several factors such as individual preferences, demographics, platform usage, and trends to build a content plan that yields results.\n"+
            "\n"+
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.\n"+
            "\n"+
            "It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";

    // public static File FileDirectory = Environment.getExternalStoragePublicDirectory("Screen Recorder" + "/");
    public static String PathFileDirectory=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/Screen Recorder";
    public static String pathScreenShotDirectory=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/DemoScreenShots/";

}
