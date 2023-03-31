package com.bumbumapps.screenrecorder.Model;

import android.graphics.Bitmap;

public class ModelMyGallery {

    private String Image_name;
    private String Image_path;
    private Bitmap Image;

    public ModelMyGallery(String image_name, String image_path, Bitmap image) {
        Image_name = image_name;
        Image_path = image_path;
        Image = image;
    }

    public String getImage_name() {
        return Image_name;
    }

    public void setImage_name(String image_name) {
        Image_name = image_name;
    }

    public String getImage_path() {
        return Image_path;
    }

    public void setImage_path(String image_path) {
        Image_path = image_path;
    }

    public Bitmap getImage() {
        return Image;
    }

    public void setImage(Bitmap image) {
        Image = image;
    }
}
