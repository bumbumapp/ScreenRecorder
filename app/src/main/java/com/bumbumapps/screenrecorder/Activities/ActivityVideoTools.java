package com.bumbumapps.screenrecorder.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumbumapps.screenrecorder.EdgeToEdgeInsets;
import com.bumbumapps.screenrecorder.R;
import com.bumbumapps.screenrecorder.Utills.MediaSaver;
import com.bumbumapps.screenrecorder.Utills.RecordingTools;
import com.bumbumapps.screenrecorder.Utills.VideoCutter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Edit one recording: trim to a range, extract the audio, or save the frame on screen as a picture. */
public class ActivityVideoTools extends AppCompatActivity {

    private static final String EXTRA_PATH = "path";

    public static void start(Context context, String path) {
        context.startActivity(new Intent(context, ActivityVideoTools.class).putExtra(EXTRA_PATH, path));
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private File source;
    private long durationMs;
    private long startMs;
    private long endMs;
    private VideoView videoView;
    private SeekBar seekPosition, seekStart, seekEnd;
    private TextView textPosition, textStart, textEnd, textLength, textStatus;
    private Button buttonPlay, buttonTrim, buttonFrame, buttonAudio;
    private boolean userSeeking;
    private boolean destroyed;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (destroyed) return;
            if (videoView.isPlaying()) {
                int pos = videoView.getCurrentPosition();
                if (pos >= endMs) {
                    videoView.pause();
                    buttonPlay.setText(R.string.tools_play);
                }
                if (!userSeeking) seekPosition.setProgress(pos);
                textPosition.setText(RecordingTools.formatDuration(pos) + " / " + RecordingTools.formatDuration(durationMs));
            }
            handler.postDelayed(this, 200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_tools);
        EdgeToEdgeInsets.apply(this);
        String path = getIntent().getStringExtra(EXTRA_PATH);
        source = path == null ? null : new File(path);
        if (source == null || !source.exists()) {
            finish();
            return;
        }
        durationMs = readDuration(source);
        if (durationMs <= 0) {
            finish();
            return;
        }
        startMs = 0;
        endMs = durationMs;

        videoView = findViewById(R.id.video_view);
        seekPosition = findViewById(R.id.seek_position);
        seekStart = findViewById(R.id.seek_start);
        seekEnd = findViewById(R.id.seek_end);
        textPosition = findViewById(R.id.text_position);
        textStart = findViewById(R.id.text_start);
        textEnd = findViewById(R.id.text_end);
        textLength = findViewById(R.id.text_length);
        textStatus = findViewById(R.id.text_status);
        buttonPlay = findViewById(R.id.button_play);
        buttonTrim = findViewById(R.id.button_trim);
        buttonFrame = findViewById(R.id.button_frame);
        buttonAudio = findViewById(R.id.button_audio);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        videoView.setVideoPath(source.getAbsolutePath());
        videoView.setOnPreparedListener(mp -> videoView.seekTo(1));
        videoView.setOnCompletionListener(mp -> buttonPlay.setText(R.string.tools_play));

        seekPosition.setMax((int) durationMs);
        seekStart.setMax((int) durationMs);
        seekEnd.setMax((int) durationMs);
        seekEnd.setProgress((int) durationMs);
        buttonAudio.setEnabled(VideoCutter.hasAudio(source));
        if (!buttonAudio.isEnabled()) textStatus.setText(R.string.tools_no_audio);

        seekPosition.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(p);
                    textPosition.setText(RecordingTools.formatDuration(p) + " / " + RecordingTools.formatDuration(durationMs));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
                userSeeking = false;
            }
        });
        seekStart.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                if (fromUser) {
                    long[] r = RecordingTools.clampRange(p, endMs, durationMs);
                    startMs = r[0];
                    endMs = r[1];
                    refreshRange();
                    videoView.seekTo((int) startMs);
                }
            }
        });
        seekEnd.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                if (fromUser) {
                    long[] r = RecordingTools.clampRange(startMs, p, durationMs);
                    startMs = r[0];
                    endMs = r[1];
                    refreshRange();
                    videoView.seekTo((int) endMs);
                }
            }
        });
        buttonPlay.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                buttonPlay.setText(R.string.tools_play);
            } else {
                int pos = videoView.getCurrentPosition();
                if (pos < startMs || pos >= endMs) videoView.seekTo((int) startMs);
                videoView.start();
                buttonPlay.setText(R.string.tools_pause);
            }
        });
        buttonTrim.setOnClickListener(v -> trim());
        buttonFrame.setOnClickListener(v -> saveFrame());
        buttonAudio.setOnClickListener(v -> extractAudio());
        refreshRange();
        handler.post(ticker);
    }

    private void refreshRange() {
        seekStart.setProgress((int) startMs);
        seekEnd.setProgress((int) endMs);
        textStart.setText(getString(R.string.tools_start_at, RecordingTools.formatDuration(startMs)));
        textEnd.setText(getString(R.string.tools_end_at, RecordingTools.formatDuration(endMs)));
        textLength.setText(getString(R.string.tools_clip_length, RecordingTools.formatDuration(endMs - startMs)));
    }

    private void setBusy(boolean busy, int messageRes) {
        buttonTrim.setEnabled(!busy);
        buttonFrame.setEnabled(!busy);
        buttonAudio.setEnabled(!busy && VideoCutter.hasAudio(source));
        textStatus.setText(busy ? getString(messageRes) : "");
    }

    private String stem() {
        String n = source.getName();
        int dot = n.lastIndexOf('.');
        return dot > 0 ? n.substring(0, dot) : n;
    }

    private Set<String> existingNames() {
        Set<String> names = new HashSet<>();
        File[] siblings = source.getParentFile() == null ? null : source.getParentFile().listFiles();
        if (siblings != null) {
            for (File f : siblings) names.add(f.getName().toLowerCase(Locale.ROOT));
        }
        return names;
    }

    private void trim() {
        videoView.pause();
        buttonPlay.setText(R.string.tools_play);
        final long s = startMs, e = endMs;
        final String name = RecordingTools.uniqueName(stem() + "_trim.mp4", existingNames());
        setBusy(true, R.string.tools_working);
        worker.execute(() -> {
            File temp = new File(getCacheDir(), "trim_" + System.currentTimeMillis() + ".mp4");
            int result;
            try {
                VideoCutter.trim(source, temp, s * 1000L, e * 1000L);
                MediaSaver.save(this, temp, name, MediaSaver.Kind.VIDEO);
                result = R.string.tools_trim_done;
            } catch (Exception ex) {
                result = R.string.tools_failed;
            } finally {
                //noinspection ResultOfMethodCallIgnored
                temp.delete();
            }
            finishWork(result, name);
        });
    }

    private void extractAudio() {
        final String name = RecordingTools.uniqueName(stem() + ".m4a", new HashSet<>());
        setBusy(true, R.string.tools_working);
        worker.execute(() -> {
            File temp = new File(getCacheDir(), "audio_" + System.currentTimeMillis() + ".m4a");
            int result;
            try {
                VideoCutter.extractAudio(source, temp);
                MediaSaver.save(this, temp, name, MediaSaver.Kind.AUDIO);
                result = R.string.tools_audio_done;
            } catch (Exception ex) {
                result = R.string.tools_failed;
            } finally {
                //noinspection ResultOfMethodCallIgnored
                temp.delete();
            }
            finishWork(result, name);
        });
    }

    private void saveFrame() {
        final long at = videoView.getCurrentPosition();
        final String name = stem() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".png";
        setBusy(true, R.string.tools_working);
        worker.execute(() -> {
            File temp = new File(getCacheDir(), "frame_" + System.currentTimeMillis() + ".png");
            int result;
            MediaMetadataRetriever r = new MediaMetadataRetriever();
            try {
                r.setDataSource(source.getAbsolutePath());
                Bitmap bmp = r.getFrameAtTime(at * 1000L, MediaMetadataRetriever.OPTION_CLOSEST);
                if (bmp == null) throw new IllegalStateException("no frame");
                try (FileOutputStream out = new FileOutputStream(temp)) {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
                MediaSaver.save(this, temp, name, MediaSaver.Kind.IMAGE);
                result = R.string.tools_frame_done;
            } catch (Exception ex) {
                result = R.string.tools_failed;
            } finally {
                try {
                    r.release();
                } catch (Exception ignored) {
                }
                //noinspection ResultOfMethodCallIgnored
                temp.delete();
            }
            finishWork(result, name);
        });
    }

    private void finishWork(int messageRes, String name) {
        handler.post(() -> {
            if (destroyed) return;
            setBusy(false, 0);
            textStatus.setText(getString(messageRes, name));
        });
    }

    private static long readDuration(File f) {
        MediaMetadataRetriever r = new MediaMetadataRetriever();
        try {
            r.setDataSource(f.getAbsolutePath());
            String d = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return d == null ? 0 : Long.parseLong(d);
        } catch (RuntimeException e) {
            return 0;
        } finally {
            try {
                r.release();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
            buttonPlay.setText(R.string.tools_play);
        }
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        handler.removeCallbacksAndMessages(null);
        worker.shutdown();
        super.onDestroy();
    }

    private abstract static class SimpleSeek implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
