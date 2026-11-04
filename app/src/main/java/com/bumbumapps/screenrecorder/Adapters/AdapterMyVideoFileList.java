package com.bumbumapps.screenrecorder.Adapters;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.bumbumapps.screenrecorder.AdsHelper;
import com.bumbumapps.screenrecorder.Utills.Globals;
import com.bumbumapps.screenrecorder.Utills.MyPreference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;

import com.bumbumapps.screenrecorder.Fragment.FragmentMyRecording;
import com.bumbumapps.screenrecorder.R;


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


    private MyPreference myPreference;

    private Runnable onDataChanged;

    public AdapterMyVideoFileList(Context context, ArrayList<File> files) {
        this.context = context;
        this.fileArrayList = files;
    }

    /** Called after a file was renamed or deleted so the screen can reload its list and summary. */
    public void setOnDataChanged(Runnable r) {
        this.onDataChanged = r;
    }

    private void changed() {
        if (onDataChanged != null) onDataChanged.run();
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
        final File fileItem = fileArrayList.get(position);
       myPreference=new MyPreference(context);


        final String urlpath = fileItem.getAbsolutePath();
        final String filename = fileItem.getName();
        long length = fileItem.length();
        long durationMs = 0;
        if (length < 1024){
            fileItem.delete();
        } else {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(urlpath);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if(time!=null)
            {
                long durationtime = Long.parseLong(time);
                durationMs = durationtime;
                viewHolder.tv_fileduration.setText(duration(durationtime));
            }
        }
       viewHolder.tv_filesize.setText(size(length));
        viewHolder.tv_fileName.setText(filename);
        // Glide's default frame (-1, "representative") comes back empty for some short HBRecorder
        // files on Android 15+, leaving a blank tile; ask for a real frame and fall back to the
        // closest decodable one
        final ImageView thumb = viewHolder.iv_fileimage;
        Glide.with(context)
                .load(fileItem.getPath())
                .apply(RequestOptions.frameOf(Globals.thumbnailFrameUs(durationMs)))
                .signature(new ObjectKey(fileItem.lastModified() + ":" + fileItem.length()))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        thumb.post(() -> thumb.setImageBitmap(closestFrame(urlpath)));
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(thumb);
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
                final int pos = viewHolder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || pos >= fileArrayList.size()) return;
                final File target = fileArrayList.get(pos);
                final PopupMenu popupMenu = new PopupMenu(context, viewHolder.iv_popupmenu);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.item_delete) {
                            deleteFile(target);
                        } else if (id == R.id.item_share) {
                            shareVideo(context, target.getAbsolutePath());
                        } else if (id == R.id.item_edit) {
                            com.bumbumapps.screenrecorder.Activities.ActivityVideoTools.start(context, target.getAbsolutePath());
                        } else if (id == R.id.item_rename) {
                            renameFile(target);
                        } else if (id == R.id.item_details) {
                            showDetails(target);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context c = viewHolder.itemView.getContext();
                int clicked = viewHolder.getBindingAdapterPosition();
                if (clicked == RecyclerView.NO_POSITION || clicked >= fileArrayList.size()) return;
                final String urlpath = fileArrayList.get(clicked).getAbsolutePath();
                if (!myPreference.isBoolean() && c instanceof Activity) {
                    // Capped interstitial (natural break), then open the video.
                    AdsHelper.showInterstitialIfReady((Activity) c, c.getString(R.string.interstitial_id),
                            () -> openVideo(context, urlpath));
                } else {
                    openVideo(context, urlpath);
                }
            }
        });


      }



    /** content:// Uri for a recording (a bare path or file:// Uri cannot be opened by other apps). */
    static Uri contentUri(Context context, String filePath) {
        return androidx.core.content.FileProvider.getUriForFile(context,
                context.getString(R.string.file_provider_authority), new java.io.File(filePath));
    }

    public static void openVideo(Context context, String filePath) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri(context, filePath), "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            Toast.makeText(context, "Application not found to open this file", Toast.LENGTH_LONG).show();
        }
    }

    public static void shareVideo(Context context, String filePath) {
        Uri mainUri;
        try {
            mainUri = contentUri(context, filePath);
        } catch (IllegalArgumentException e) {
            Toast.makeText(context, "Application not found to open this file", Toast.LENGTH_LONG).show();
            return;
        }
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
    private static Bitmap closestFrame(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            return retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (RuntimeException e) {
            return null;
        } finally {
            try { retriever.release(); } catch (Exception ignored) { }
        }
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
    private void deleteFile(final File target) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
        builder.setTitle("Confirm Delete ?");
        builder.setMessage("Are you sure you want delete this !");
        builder.setIcon(R.drawable.ic_delete);
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            //noinspection ResultOfMethodCallIgnored
            target.delete();
            changed();
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }

    private void renameFile(final File target) {
        final android.widget.EditText input = new android.widget.EditText(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
        String current = target.getName();
        if (current.toLowerCase(java.util.Locale.ROOT).endsWith(".mp4")) current = current.substring(0, current.length() - 4);
        input.setText(current);
        input.setSelectAllOnFocus(true);
        input.setSingleLine(true);
        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom))
                .setTitle(R.string.rename_title)
                .setView(input)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = com.bumbumapps.screenrecorder.Utills.RecordingTools.sanitizeFileName(input.getText().toString(), "mp4");
            if (name == null) {
                input.setError(context.getString(R.string.rename_invalid));
                return;
            }
            if (name.equals(target.getName())) {
                dialog.dismiss();
                return;
            }
            File sibling = new File(target.getParentFile(), name);
            if (sibling.exists()) {
                input.setError(context.getString(R.string.rename_exists));
                return;
            }
            if (com.bumbumapps.screenrecorder.Utills.MediaSaver.rename(context, target, name)) {
                dialog.dismiss();
                changed();
            } else {
                Toast.makeText(context, R.string.rename_failed, Toast.LENGTH_LONG).show();
            }
        }));
        dialog.show();
    }

    private void showDetails(File target) {
        String duration = "-";
        String resolution = "-";
        MediaMetadataRetriever r = new MediaMetadataRetriever();
        try {
            r.setDataSource(target.getAbsolutePath());
            String d = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (d != null) duration = com.bumbumapps.screenrecorder.Utills.RecordingTools.formatDuration(Long.parseLong(d));
            String w = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String h = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            if (w != null && h != null) resolution = w + " x " + h;
        } catch (RuntimeException ignored) {
        } finally {
            try {
                r.release();
            } catch (Exception ignored) {
            }
        }
        String saved = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(target.lastModified()));
        String body = context.getString(R.string.details_body, target.getName(),
                com.bumbumapps.screenrecorder.Utills.RecordingTools.formatSize(target.length()), duration, resolution, saved);
        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom))
                .setTitle(R.string.details_title)
                .setMessage(body)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
