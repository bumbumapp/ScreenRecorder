package com.bumbumapps.screenrecorder.Fragment;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.AlertDialog;
import android.media.MediaMetadataRetriever;
import com.bumbumapps.screenrecorder.Utills.RecordingTools;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumbumapps.screenrecorder.Adapters.AdapterMyVideoFileList;
import com.bumbumapps.screenrecorder.R;
import com.bumbumapps.screenrecorder.Utills.Constance;

import java.io.File;
import java.util.ArrayList;

public class FragmentMyRecording extends Fragment {
    Context context;
    View view;
    RecyclerView rv_myrecordinglist;
    private AdapterMyVideoFileList adapterMyVideoFileList;
    private ArrayList<File> fileArrayList;
    public static RelativeLayout rl_videonotfound;
    private final java.util.Map<String, RecordingTools.Item> itemCache = new java.util.HashMap<>();
    private TextView tvSummary;
    private EditText etSearch;
    private int sortMode = RecordingTools.SORT_NEWEST;
    private String query = "";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context contextwrapper=new ContextThemeWrapper(getActivity(),R.style.AppTheme);
        LayoutInflater layoutInflater=inflater.cloneInContext(contextwrapper);
        view = layoutInflater.inflate(R.layout.fragment_myrecording, container, false);
        context = getContext();

        rv_myrecordinglist = view.findViewById(R.id.rv_myrecordinglist);
        rl_videonotfound = view.findViewById(R.id.rl_videonotfound);

        rv_myrecordinglist.setLayoutManager(new LinearLayoutManager(context));
        tvSummary = view.findViewById(R.id.tv_summary);
        etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence c, int a, int b, int d) {
            }

            @Override
            public void onTextChanged(CharSequence c, int a, int b, int d) {
                query = c.toString();
                refresh();
            }

            @Override
            public void afterTextChanged(Editable e) {
            }
        });
        ((Button) view.findViewById(R.id.btn_sort)).setOnClickListener(this::showSortMenu);
        view.findViewById(R.id.btn_cleanup).setOnClickListener(v -> showCleanup());

        return view;
    }

    private ArrayList<RecordingTools.Item> allItems = new ArrayList<>();

    private RecordingTools.Item describe(File f) {
        String key = f.getAbsolutePath();
        RecordingTools.Item cached = itemCache.get(key);
        if (cached != null && cached.size == f.length() && cached.modified == f.lastModified()) return cached;
        long duration = 0;
        MediaMetadataRetriever r = new MediaMetadataRetriever();
        try {
            r.setDataSource(key);
            String d = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (d != null) duration = Long.parseLong(d);
        } catch (RuntimeException ignored) {
        } finally {
            try {
                r.release();
            } catch (Exception ignored) {
            }
        }
        RecordingTools.Item item = new RecordingTools.Item(f.getName(), f.length(), f.lastModified(), duration);
        itemCache.put(key, item);
        return item;
    }

    private void getAllFiles() {
        File dir = new File(Constance.PathFileDirectory);
        ArrayList<File> found = new ArrayList<>();
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdir();
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".mp4")) {
                    found.add(file);
                }
            }
        }
        allItems = new ArrayList<>();
        java.util.Map<String, File> byName = new java.util.HashMap<>();
        for (File f : found) {
            allItems.add(describe(f));
            byName.put(f.getName(), f);
        }
        java.util.List<RecordingTools.Item> shown = RecordingTools.filterAndSort(allItems, query, sortMode);
        fileArrayList = new ArrayList<>();
        for (RecordingTools.Item i : shown) fileArrayList.add(byName.get(i.name));
        adapterMyVideoFileList = new AdapterMyVideoFileList(context, fileArrayList);
        adapterMyVideoFileList.setOnDataChanged(this::refresh);
        rv_myrecordinglist.setAdapter(adapterMyVideoFileList);
        if (allItems.isEmpty()) {
            tvSummary.setText("");
        } else if (shown.size() != allItems.size()) {
            tvSummary.setText(getString(R.string.library_summary_filtered, shown.size(), allItems.size()));
        } else {
            tvSummary.setText(getString(R.string.library_summary, allItems.size(),
                    RecordingTools.formatSize(RecordingTools.totalSize(allItems)),
                    RecordingTools.formatDuration(RecordingTools.totalDuration(allItems))));
        }
    }

    private void showSortMenu(View anchor) {
        PopupMenu menu = new PopupMenu(context, anchor);
        int[] titles = {R.string.sort_newest, R.string.sort_oldest, R.string.sort_largest, R.string.sort_longest, R.string.sort_name};
        for (int i = 0; i < titles.length; i++) {
            menu.getMenu().add(0, i, i, titles[i]).setCheckable(true).setChecked(i == sortMode);
        }
        menu.setOnMenuItemClickListener(item -> {
            sortMode = item.getItemId();
            refresh();
            return true;
        });
        menu.show();
    }

    private void showCleanup() {
        final long now = System.currentTimeMillis();
        final int[] rules = {RecordingTools.CLEAN_OLDER_7, RecordingTools.CLEAN_OLDER_30, RecordingTools.CLEAN_OLDER_90, RecordingTools.CLEAN_LARGER_100MB};
        final int[] labels = {R.string.cleanup_older_7, R.string.cleanup_older_30, R.string.cleanup_older_90, R.string.cleanup_larger};
        String[] options = new String[rules.length];
        for (int i = 0; i < rules.length; i++) {
            java.util.List<RecordingTools.Item> c = RecordingTools.cleanupCandidates(allItems, rules[i], now);
            options[i] = getString(labels[i], c.size(), RecordingTools.formatSize(RecordingTools.totalSize(c)));
        }
        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom))
                .setTitle(R.string.cleanup_title)
                .setItems(options, (d, which) -> confirmCleanup(rules[which], now))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmCleanup(int rule, long now) {
        final java.util.List<RecordingTools.Item> victims = RecordingTools.cleanupCandidates(allItems, rule, now);
        if (victims.isEmpty()) {
            Toast.makeText(context, R.string.cleanup_nothing, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom))
                .setMessage(getString(R.string.cleanup_confirm, victims.size(), RecordingTools.formatSize(RecordingTools.totalSize(victims))))
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    int deleted = 0;
                    for (RecordingTools.Item i : victims) {
                        if (new File(Constance.PathFileDirectory, i.name).delete()) deleted++;
                    }
                    Toast.makeText(context, getString(R.string.cleanup_done, deleted), Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // A recording is still being finalized for a few seconds after stop; reload when MediaStore
    // reports it done, otherwise the newest entry keeps a blank thumbnail and no duration
    private final ContentObserver videosChanged = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            if (isResumed()) refresh();
        }
    };

    private void refresh() {
        if (context == null || tvSummary == null) return;
        getAllFiles();
        rl_videonotfound.setVisibility(allItems.isEmpty() ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.ll_library_tools).setVisibility(allItems.isEmpty() && query.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
        context.getContentResolver().registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, videosChanged);
    }

    @Override
    public void onPause() {
        context.getContentResolver().unregisterContentObserver(videosChanged);
        super.onPause();
    }
}
