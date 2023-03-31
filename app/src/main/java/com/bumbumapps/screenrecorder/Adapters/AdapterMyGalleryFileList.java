package com.bumbumapps.screenrecorder.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumbumapps.screenrecorder.Activities.ActivityGalleryPreview;
import com.bumbumapps.screenrecorder.Activities.ActivityHome;
import com.bumbumapps.screenrecorder.Model.ModelMyGallery;
import com.bumbumapps.screenrecorder.R;
import com.bumbumapps.screenrecorder.Utills.Constance;

import java.util.ArrayList;


public class AdapterMyGalleryFileList extends RecyclerView.Adapter<AdapterMyGalleryFileList.ViewHolder> {
    private Context context;
    //private ArrayList<File> fileArrayList;
    private ArrayList<ModelMyGallery> fileArrayList;
   // File fileItem;
   /* public AdapterMyGalleryFileList(Context context, ArrayList<File> files) {
        this.context = context;
        this.fileArrayList = files;
    }*/public AdapterMyGalleryFileList(Context context, ArrayList<ModelMyGallery> files) {
        this.context = context;
        this.fileArrayList = files;
       Constance.modelMyGalleries = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(context).inflate(R.layout.item_mygallerylist, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterMyGalleryFileList.ViewHolder viewHolder, final int position) {
       // fileItem = fileArrayList.get(position);

        final String pathofimg=fileArrayList.get(position).getImage_path();
        Glide.with(context)
                .load(pathofimg)
                .into(viewHolder.iv_screenshotimg);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ActivityGalleryPreview.class);
                i.putExtra("current_click_position", position);
                context.startActivity(i);
              /*  displayAdMob();*/
              //  FragmentGallery.showInterstitial();

            }
        });
      }

//    public void displayAdMob() {
//        if (ActivityHome.getInstance().mInterstitialAd != null) {
//            if (ActivityHome.getInstance().mInterstitialAd.isLoaded()) {
//                ActivityHome.getInstance().mInterstitialAd.show();
//            } else {
//                ActivityHome.instance.stopTask();
//                ActivityHome.instance.StartTimer();
//            }
//        } else {
//            ActivityHome.instance.stopTask();
//            ActivityHome.instance.StartTimer();
//        }
//    }

    @Override
    public int getItemCount() {
        return fileArrayList == null ? 0 : fileArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView  iv_screenshotimg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_screenshotimg = itemView.findViewById(R.id.iv_screenshotimg);

        }
    }

}