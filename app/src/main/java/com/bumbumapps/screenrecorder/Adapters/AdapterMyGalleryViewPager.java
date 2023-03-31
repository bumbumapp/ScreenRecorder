package com.bumbumapps.screenrecorder.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import com.bumbumapps.screenrecorder.Model.ModelMyGallery;
import com.bumbumapps.screenrecorder.R;

import java.util.ArrayList;

public class AdapterMyGalleryViewPager extends PagerAdapter {

    ArrayList<ModelMyGallery> myGalleries;
    Context context;
    View itemView;

    public AdapterMyGalleryViewPager(ArrayList<ModelMyGallery> myGalleries, Context context) {
        this.myGalleries = myGalleries;
        this.context = context;
    }

    @Override
    public int getCount() {
        return myGalleries.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater mLayoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        itemView = mLayoutInflater.inflate(R.layout.itemmygallerypager, container, false);
            ImageView imageView1 = itemView.findViewById(R.id.ivgalleryimg);
            if (myGalleries.get(position).getImage_name().endsWith(".png")) {
                Glide.with(context).load(myGalleries.get(position).getImage_path()).into(imageView1);
            } else {
                // Glide.with(context).load(R.drawable.lifeimage1).apply(new RequestOptions().placeholder(R.drawable.imageholder).override(200, 200)).into(holder.images);
            }

            itemView.setTag("view" + position);

        container.addView(itemView);
        return itemView;
    }
    public void deletepagerforphoto(int position)
    {
        myGalleries.remove(position);
    }

    @Override
    public int getItemPosition(Object object){
        if (myGalleries.contains(object)) {
            return myGalleries.indexOf(object);
        } else {
            return POSITION_NONE;
        }
        // return PagerAdapter.POSITION_NONE;
    }
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // super.destroyItem(container, position, object);
        container.removeView((View) object);
    }
}
