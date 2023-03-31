package com.bumbumapps.screenrecorder.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.bumbumapps.screenrecorder.Adapters.AdapterMyGalleryViewPager;
import com.bumbumapps.screenrecorder.Model.ModelMyGallery;
import com.bumbumapps.screenrecorder.R;
import com.bumbumapps.screenrecorder.Utills.Constance;

import java.io.File;
import java.util.ArrayList;

public class ActivityGalleryPreview extends AppCompatActivity {

    Context context;
    LinearLayout ll_shareimage, ll_deleteimage;
    ImageView iv_back;
    // ImageView iv_previewimg;
    //String pathofimg;
    ViewPager pager_singlemypic;
    ArrayList<ModelMyGallery> modelMyGalleries;
    AdapterMyGalleryViewPager adapterMyGalleryViewPager;
    LinearLayout ll_gallerypic;
    int current_click_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_preview);
        context = ActivityGalleryPreview.this;
       // foradvertise();
//        if (Constance.adType.equals("Ad Mob")) {
//            displayAdMob();
//            Log.d("ADssss", "Ad Mob");
//        }

        ll_shareimage = findViewById(R.id.ll_shareimage);
        ll_deleteimage = findViewById(R.id.ll_deleteimage);
        iv_back = findViewById(R.id.iv_back);
        //iv_previewimg=findViewById(R.id.iv_previewimg);
        pager_singlemypic = findViewById(R.id.pager_singlemypic);

        current_click_position=getIntent().getExtras().getInt("current_click_position");
        modelMyGalleries = new ArrayList<>();
        modelMyGalleries.addAll(Constance.modelMyGalleries);


        adapterMyGalleryViewPager = new AdapterMyGalleryViewPager(modelMyGalleries, context);
        pager_singlemypic.setAdapter(adapterMyGalleryViewPager);
        pager_singlemypic.setCurrentItem(current_click_position);

      /*  pathofimg=getIntent().getStringExtra("pathofimg");

        if(pathofimg!=null)
        {
            Glide.with(context)
                    .load(pathofimg)
                    .into(iv_previewimg);

        }else
        {
            Log.d("eeeee","Image path not found");
        }*/

        ll_shareimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    ll_gallerypic = pager_singlemypic.findViewWithTag("view" + pager_singlemypic.getCurrentItem()).findViewById(R.id.ll_gallerypic);
                    shareimage(ll_gallerypic);
                }catch (Exception e){
                    Log.d("Tag","Errorx"+e.getMessage());
                }

            }
        });
        ll_deleteimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteMyPhoto();
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void shareimage(View view) {

        File file = new File(modelMyGalleries.get(pager_singlemypic.getCurrentItem()).getImage_path());
        Intent share = new Intent(Intent.ACTION_SEND);
        Uri uri = FileProvider.getUriForFile(context, getString(R.string.file_provider_authority), file);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share via"));

    }

    public void DeleteMyPhoto() {
        AlertDialog diaBox = AskOption();
        diaBox.show();
    }

    private AlertDialog AskOption() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom))
                // set message, title, and icon
                .setTitle("Delete ?")
                .setMessage("Sure you want to Delete this image ?")

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        File file = new File(modelMyGalleries.get(pager_singlemypic.getCurrentItem()).getImage_path());
                        file.delete();
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(modelMyGalleries.get(pager_singlemypic.getCurrentItem()).getImage_path()))));
                        dialog.dismiss();
                        adapterMyGalleryViewPager.deletepagerforphoto(pager_singlemypic.getCurrentItem());
                        adapterMyGalleryViewPager.notifyDataSetChanged();


                        if(modelMyGalleries.size()==0)
                        {
                            // myphotosModels.clear();
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("No Data Found")
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            onBackPressed();

                                        }
                                    });

                            //Creating dialog box
                            AlertDialog alert = builder.create();
                            alert.show();

                        }
                        else
                        {
                        }
                       // onBackPressed();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        return myQuittingDialogBox;
    }

//    public void foradvertise() {
//        AdView mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                // Check the LogCat to get your test device ID
//                .addTestDevice("C04B1BFFB0774708339BC273F8A43708")
//                .build();
//
//        mAdView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//            }
//
//            @Override
//            public void onAdClosed() {
//                // Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onAdFailedToLoad(int errorCode) {
//                // Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onAdLeftApplication() {
//                // Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onAdOpened() {
//                super.onAdOpened();
//            }
//        });
//
//        mAdView.loadAd(adRequest);
//    }
//    public void displayAdMob() {
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
