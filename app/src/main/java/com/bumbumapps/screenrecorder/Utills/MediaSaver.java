package com.bumbumapps.screenrecorder.Utills;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Publishes a finished temporary file into the shared Movies, Music or Pictures folder of the app. */
public final class MediaSaver {

    private MediaSaver() {
    }

    public static final String FOLDER = "Screen Recorder";

    public enum Kind {
        VIDEO("video/mp4", Environment.DIRECTORY_MOVIES),
        AUDIO("audio/mp4", Environment.DIRECTORY_MUSIC),
        IMAGE("image/png", Environment.DIRECTORY_PICTURES);

        final String mime;
        final String dir;

        Kind(String mime, String dir) {
            this.mime = mime;
            this.dir = dir;
        }
    }

    /** Copies temp to the public folder as displayName and returns where it ended up (Uri on Q+, file path before). */
    public static String save(Context context, File temp, String displayName, Kind kind) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, kind.mime);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, kind.dir + "/" + FOLDER);
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            Uri collection = kind == Kind.VIDEO ? MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    : kind == Kind.AUDIO ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    : MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri uri = resolver.insert(collection, values);
            if (uri == null) throw new IOException("MediaStore insert failed");
            try (InputStream in = new FileInputStream(temp); OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null) throw new IOException("no output stream");
                copy(in, out);
            } catch (IOException e) {
                resolver.delete(uri, null, null);
                throw e;
            }
            ContentValues done = new ContentValues();
            done.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, done, null, null);
            return uri.toString();
        }
        File dir = new File(Environment.getExternalStoragePublicDirectory(kind.dir), FOLDER);
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("cannot create " + dir);
        File target = new File(dir, displayName);
        try (InputStream in = new FileInputStream(temp); OutputStream out = new FileOutputStream(target)) {
            copy(in, out);
        }
        MediaScannerConnection.scanFile(context, new String[]{target.getAbsolutePath()}, new String[]{kind.mime}, null);
        return target.getAbsolutePath();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[64 * 1024];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
    }

    /** Renames a recording; returns true on success. Tries the file first, then the MediaStore row. */
    public static boolean rename(Context context, File file, String newName) {
        File target = new File(file.getParentFile(), newName);
        if (target.exists()) return false;
        if (file.renameTo(target)) {
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath(), target.getAbsolutePath()}, null, null);
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ContentResolver resolver = context.getContentResolver();
                Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                android.database.Cursor c = resolver.query(collection, new String[]{MediaStore.MediaColumns._ID},
                        MediaStore.MediaColumns.DATA + "=?", new String[]{file.getAbsolutePath()}, null);
                if (c != null) {
                    try {
                        if (c.moveToFirst()) {
                            Uri item = android.content.ContentUris.withAppendedId(collection, c.getLong(0));
                            ContentValues v = new ContentValues();
                            v.put(MediaStore.MediaColumns.DISPLAY_NAME, newName);
                            return resolver.update(item, v, null, null) > 0;
                        }
                    } finally {
                        c.close();
                    }
                }
            } catch (RuntimeException ignored) {
            }
        }
        return false;
    }
}
