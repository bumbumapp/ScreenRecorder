package com.bumbumapps.screenrecorder.Adapters;

import static com.bumbumapps.screenrecorder.Utills.AdsLoader.mInterstitialAd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumbumapps.hbrecorder.HBRecorder;
import com.bumbumapps.screenrecorder.Fragment.FragmentHome;
import com.bumbumapps.screenrecorder.Utills.AdsLoader;
import com.bumbumapps.screenrecorder.Utills.Globals;
import com.bumbumapps.screenrecorder.Utills.MyPreference;
import com.bumbumapps.screenrecorder.Utills.Timers;
import com.bumptech.glide.Glide;

import com.bumbumapps.screenrecorder.Fragment.FragmentMyRecording;
import com.bumbumapps.screenrecorder.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;


import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AdapterMyVideoFileList extends RecyclerView.Adapter<AdapterMyVideoFileList.ViewHolder> {
    private Context context;
    private ArrayList<File> fileArrayList;
    File fileItem;

    private MyPreference myPreference;

    public AdapterMyVideoFileList(Context context, ArrayList<File> files) {
        this.context = context;
        this.fileArrayList = files;


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(context).inflate(R.layout.item_myvideofilelist, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterMyVideoFileList.ViewHolder viewHolder, final int position) {
        fileItem = fileArrayList.get(position);
       myPreference=new MyPreference(context);


        final String urlpath = fileItem.getAbsolutePath();
        final String filename = fileItem.getName();
        long length = fileItem.length();
        if (length < 1024){
            fileItem.delete();
        } else {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(urlpath);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if(time!=null)
            {
                long durationtime = Long.parseLong(time);
                viewHolder.tv_fileduration.setText(duration(durationtime));
            }
        }
       viewHolder.tv_filesize.setText(size(length));
        viewHolder.tv_fileName.setText(filename);
        Glide.with(context)
                .load(fileItem.getPath())
                .into(viewHolder.iv_fileimage);
        /*try {
            String extension = fileItem.getName().substring(fileItem.getName().lastIndexOf("."));
            if (extension.equals(".mp4")) {

            } else {

            }

        } catch (Exception ex) {
        }
      */

        viewHolder.iv_popupmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupMenu = new PopupMenu(context, viewHolder.iv_popupmenu);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_delete:
                                deleteFile(position);
                                return true;

                            case R.id.item_share:
                                shareVideo(context,urlpath);
                                return true;

                        }

                        popupMenu.dismiss();

                        return true;
                    }
                });

                popupMenu.show();
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!myPreference.isBoolean()){
                    if (mInterstitialAd != null && Globals.TIMER_FINISHED) {
                        mInterstitialAd.show((Activity) viewHolder.itemView.getContext());
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri uri = Uri.parse(urlpath);
                                intent.setDataAndType(uri, "video/*");
                                context.startActivity(intent);
                                Globals.TIMER_FINISHED = false;
                                Timers.timer().start();
                                AdsLoader.displayInterstitial(viewHolder.itemView.getContext());
                            }

                        });
                    }else{
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(urlpath);
                        intent.setDataAndType(uri, "video/*");
                        context.startActivity(intent);
                    }


                }else{
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(urlpath);
                    intent.setDataAndType(uri, "video/*");
                    context.startActivity(intent);
                }


            }
        });


      }



    public static void shareVideo(Context context, String filePath) {
        Uri mainUri = Uri.parse(filePath);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("video/mp4");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, mainUri);
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(Intent.createChooser(sharingIntent, "Share Video using"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Application not found to open this file", Toast.LENGTH_LONG).show();
        }
    }
    public static String duration(long seconds) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(seconds),
                TimeUnit.MILLISECONDS.toMinutes(seconds) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(seconds)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(seconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(seconds)));
    }
    public static String size(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    @Override
    public int getItemCount() {
        return fileArrayList == null ? 0 : fileArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView  iv_fileimage,iv_popupmenu;
        TextView tv_fileName,tv_filesize,tv_fileduration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_fileName = itemView.findViewById(R.id.tv_filetittle);
            tv_filesize = itemView.findViewById(R.id.tv_filesize);
            tv_fileduration = itemView.findViewById(R.id.tv_fileduration);
            iv_fileimage = itemView.findViewById(R.id.iv_fileimage);
            iv_popupmenu = itemView.findViewById(R.id.iv_popupmenu);

        }
    }
    private void deleteFile(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.AlertDialogCustom));
        builder.setTitle("Confirm Delete ?");
        builder.setMessage("Are you sure you want delete this !");
        builder.setIcon(R.drawable.ic_delete);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                fileItem.delete();
                Log.d("aaaaaaa",""+position);
                fileArrayList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, fileArrayList.size());
                if (fileArrayList.size() == 0) {
                   FragmentMyRecording.rl_videonotfound.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}