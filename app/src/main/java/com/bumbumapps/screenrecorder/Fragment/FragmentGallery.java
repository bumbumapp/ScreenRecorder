package com.bumbumapps.screenrecorder.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.ads.InterstitialAd;
import com.bumbumapps.screenrecorder.Activities.ActivityHome;
import com.bumbumapps.screenrecorder.Adapters.AdapterMyGalleryFileList;
import com.bumbumapps.screenrecorder.Model.ModelMyGallery;
import com.bumbumapps.screenrecorder.R;
import com.bumbumapps.screenrecorder.Utills.Constance;

import java.io.File;
import java.util.ArrayList;

public class FragmentGallery extends Fragment {


    Context context;
    View view;
    RecyclerView rv_mygallerylist;
    private AdapterMyGalleryFileList adapterMyGalleryFileList;
    //private ArrayList<File> fileArrayList;
    public static RelativeLayout rl_screenshotnotfound;
    ArrayList<ModelMyGallery> fileList;
    ArrayList<ModelMyGallery> allFileList;
    public static InterstitialAd mInterstitialAd;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_gallery, container, false);
        context = getContext();
        rv_mygallerylist = view.findViewById(R.id.rv_mygallerylist);
        rl_screenshotnotfound = view.findViewById(R.id.rl_screenshotnotfound);
        rv_mygallerylist.setHasFixedSize(true);
        rv_mygallerylist.setLayoutManager(new GridLayoutManager(context,2));

        fileList=new ArrayList<>();
        allFileList=new ArrayList<>();
        return view;
    }
    private void getAllFiles() {
        fileList.clear();
        allFileList.clear();
       // fileArrayList = new ArrayList<>();
          File myGallery=new File(Constance.pathScreenShotDirectory);
            if (!myGallery.exists())
            {
                myGallery.mkdir();
            }
            else {
                Log.d("myGallery", "Constance.FileDirectory : " + myGallery);
                File[] files = myGallery.listFiles();
                if (files != null) {
                    rl_screenshotnotfound.setVisibility(View.GONE);

                    for (int i = 0 ; i < files.length; i++) {
                        // Toast.makeText(context,"file size"+files.length,Toast.LENGTH_LONG).show();
                        if (files[i].getName().endsWith(".png"))
                        {
                            String file_name = files[i].getName();
                            String file_path = files[i].getPath();
                            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(file_path, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);                // you can store name to arraylist and use it later
                            fileList.add(new ModelMyGallery(file_name, file_path, thumb));

                           // fileArrayList.add(file);
                        }
                    }
                    allFileList.addAll(fileList);

                    if (allFileList.size() != 0) {
                       // rv_myphotos.setVisibility(View.VISIBLE);
                        adapterMyGalleryFileList = new AdapterMyGalleryFileList(context, allFileList);
                        adapterMyGalleryFileList.notifyDataSetChanged();
                        rv_mygallerylist.setAdapter(adapterMyGalleryFileList);

                    }
                    else {
                        rl_screenshotnotfound.setVisibility(View.VISIBLE);
                        rv_mygallerylist.setVisibility(View.GONE);
                    }

                  /*  for (File file : files) {
                        if (file.getName().endsWith(".png"))
                        {
                            String file_name = files[i].getName();
                            String file_path = files[i].getPath();

                            fileArrayList.add(file);
                        }
                    }*/
                  /*  for(int i=0;i<allFileList.size();i++){
                        if(allFileList.get(i).isDirectory()){
                            allFileList.remove(i);
                        }
                    }*/
                       }
                else {
                    rl_screenshotnotfound.setVisibility(View.VISIBLE);
                   // Toast.makeText(context, "Screenshot Not Found", Toast.LENGTH_LONG).show();
                }
            }





    }
    @Override
    public void onResume() {
        super.onResume();
        getAllFiles();
        if (allFileList.isEmpty()) {
            rl_screenshotnotfound.setVisibility(View.VISIBLE);
        } else {

            rl_screenshotnotfound.setVisibility(View.GONE);
        }
    }
//    public static void showInterstitial() {
//
//        if (Constance.adType.equals("Ad Mob")) {
//            displayAdMob();
//            Log.d("ADssss", "Ad Mob");
//        }
//
//    }

//    public static void displayAdMob() {
//        if (ActivityHome.getInstance().mInterstitialAd != null) {
//            if (ActivityHome.getInstance().mInterstitialAd.isLoaded()) {
//                Log.d("shsjks","sdhsjkhd");
//                ActivityHome.getInstance().mInterstitialAd.show();
//            } else {
//            }
//        } else {
//
//        }
//    }


}
