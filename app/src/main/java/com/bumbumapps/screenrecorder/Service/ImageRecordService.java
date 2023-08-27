package com.bumbumapps.screenrecorder.Service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.util.Pair;

import com.bumbumapps.hbrecorder.NotificationUtils;
import com.bumbumapps.screenrecorder.Activities.ActivityMediaProjectionPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Objects;

public class ImageRecordService extends Service {

    private static final String TAG = "ScreenCaptureService";
    public static final String RESULT_CODE = "RESULT_CODE";
    public static final String DATA = "DATA";
    private static final String ACTION = "ACTION";
    private static final String START = "START";
    private static final String STOP = "STOP";
    private static final String SCREENCAP_NAME = "screencap";
    File storeDirectory;
    //  private static int IMAGES_PRODUCED;

    public MediaProjection mMediaProjection;
    private String mStoreDir;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    Point windowSize;
    public Bitmap bitmap;

    //  private OrientationChangeCallback mOrientationChangeCallback;

    public static Intent getStartIntent(Context context, int resultCode, Intent data, String directory) {
        Intent intent = new Intent(context, ImageRecordService.class);
        intent.putExtra(ACTION, START);
        intent.putExtra(RESULT_CODE, resultCode);
        Log.d("screenshot_check", "getStartIntent");
        intent.putExtra(DATA, data);
        intent.putExtra("path_directory", directory);
        return intent;
    }
    public static Intent getStopIntent(Context context) {
        Intent intent = new Intent(context, ImageRecordService.class);
        intent.putExtra(ACTION, STOP);
        return intent;
    }

    /* private static boolean isStartCommand(Intent intent) {
         return intent.hasExtra(RESULT_CODE) && intent.hasExtra(DATA)
                 && intent.hasExtra(ACTION) && Objects.equals(intent.getStringExtra(ACTION), START);
     } */
    private static boolean isStartCommand(Intent intent) {
        return intent.hasExtra(RESULT_CODE) && intent.hasExtra(DATA);
    }

    private static boolean isStopCommand(Intent intent) {
        return intent.hasExtra(ACTION) && Objects.equals(intent.getStringExtra(ACTION), STOP);
    }

    private static int getVirtualDisplayFlags() {
        return DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d("IMAGE SAVED", "IMAGE SAVED not null image" + "image");

            Image image = null;
            FileOutputStream fos = null;
            bitmap = null;
            try {
                image = mImageReader.acquireLatestImage();
                Log.d("servicecheck", "image" + image);
                Log.d("imagecheck", "" + image);
                if (image != null) {
                    Log.d("servicecheck", "not null image" + image);

                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;
                    Log.d("IMAGE SAVED","IMAGE SAVED onImageAvailable");
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    Log.d("servicecheck", "not null image bitmap " + bitmap);
                    bitmap.copyPixelsFromBuffer(buffer);
                    Log.d("servicecheck", "not null image bitmap 2" + bitmap);
                    // fix the extra width from Image
                    Bitmap croppedBitmap;
                    try {
                        croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, mWidth, mHeight);
                    } catch (OutOfMemoryError e) {
                        // Timber.d(e, "Out of memory when cropping bitmap of screen size");
                        croppedBitmap = bitmap;
                    }
                    if (croppedBitmap != bitmap) {
                        bitmap.recycle();
                    }

                    // write bitmap to a file
                    Log.d("screenshot_check", "onImageAvailable :" + mStoreDir);
                    storeDirectory = new File(mStoreDir);
                    storeDirectory.mkdir();
                    Log.d("servicecheck", "fos 1 " + fos);
                    Log.d("servicecheck", "mStoreDir " + mStoreDir);
                    Log.d("servicecheck", "storeDirectory " + storeDirectory);
                    Log.d("servicecheck", "storeDirectory 2 " + storeDirectory.getAbsolutePath());

                    //  fos = new FileOutputStream(mStoreDir + "/myscreen_" + IMAGES_PRODUCED + ".png");
                    String  time =String.valueOf(System.currentTimeMillis()).replaceAll(":", ".");
                    fos = new FileOutputStream(storeDirectory.getAbsolutePath() + "/myscreen_" + time + ".png");
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    Log.d("servicecheck", "fos" + fos);

                    Log.d("servicecheck", "not null image  last bitmap 2" + bitmap);


                    fos.flush();
                    fos.close();
                    stopProjection();
                    stopSelf();
                    scanFile(getApplicationContext(), Uri.fromFile(storeDirectory));
                    Log.d("stoptaskcheck", "stopSelf");

                } else {
                    Log.d("servicecheck", "null image" + image);

                }

            } catch (Exception e) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
                Log.d("IMAGE SAVED","IMAGE SAVED " +e.getMessage());

                e.printStackTrace();
            }



        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("servicecheck", "onCreate");
        //openActivity();

   /*     // start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isStartCommand(intent)) {
            // create notification
            Pair<Integer, Notification> notification = NotificationUtils.getNotification(this);
            startForeground(notification.first, notification.second);
            int resultCode = intent.getIntExtra(RESULT_CODE, Activity.RESULT_CANCELED);
            Intent data = intent.getParcelableExtra(DATA);
            mStoreDir = intent.getStringExtra("path_directory");
            Log.d("servicecheck", "isStartCommand");
            ActivityMediaProjectionPermission.activity.finish();
            startProjection(resultCode, data);

        } else if (isStopCommand(intent)) {
            stopProjection();
            stopSelf();
        } else {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void startProjection(int resultCode, Intent data) {
        // Log.d("screenshot_check", "startProjection :" + mStoreDir);
        Log.d("servicecheck", "startProjection");

        MediaProjectionManager mpManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mMediaProjection == null) {
            Log.d("servicecheck", "resultCode" + resultCode);
            Log.d("servicecheck", "data" + data);

            mMediaProjection = mpManager.getMediaProjection(resultCode, data);

            if (mMediaProjection != null) {
                // display metrics
                mDensity = Resources.getSystem().getDisplayMetrics().densityDpi;
                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                mDisplay = windowManager.getDefaultDisplay();

                //For height and width
                windowSize = new Point();
                windowManager.getDefaultDisplay().getRealSize(windowSize);
                mWidth = windowSize.x;
                mHeight = windowSize.y;
                // create virtual display depending on device width / height
                createVirtualDisplay();


            } else {
                Log.d("media_projection_check", " mediaprojection null");
            }
        }
    }

    private void stopProjection() {

        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        if (mVirtualDisplay != null) mVirtualDisplay.release();
        if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);


    }

    @SuppressLint("WrongConstant")
    private void createVirtualDisplay() {

        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 1);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight,
                mDensity, getVirtualDisplayFlags(), mImageReader.getSurface(), null, null);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), null);
        Log.d("IMAGE SAVED","createVirtualDisplay");

    }

    private static void scanFile(Context context, Uri imageUri) {
        Log.d("IMAGE SAVED","IMAGE SAVED SCAN FILE");
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(imageUri);
        context.sendBroadcast(scanIntent);
    }

    @Override
    public void onDestroy() {
        Log.d("stoptaskcheck", "onDestroy");
        Toast.makeText(getApplicationContext(), "Screenshot captured", Toast.LENGTH_LONG).show();
        super.onDestroy();

    }

    private void openActivity() {
        Intent mainIntent = new Intent(getApplicationContext(), ActivityMediaProjectionPermission.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
    }
}