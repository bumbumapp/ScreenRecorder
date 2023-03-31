package com.bumbumapps.screenrecorder.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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

        return view;
    }

    private void getAllFiles() {
        fileArrayList = new ArrayList<>();
        File myrecoeding=new File(Constance.PathFileDirectory);

        if (!myrecoeding.exists()) {
            myrecoeding.mkdir();

        } else {
            Log.d("jjjjj", "Constance.FileDirectory : " + myrecoeding);
            File[] files = myrecoeding.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".mp4"))
                    {
                        fileArrayList.add(file);
                    }
                }

                for(int i=0;i<fileArrayList.size();i++){
                    if(fileArrayList.get(i).isDirectory()){
                        fileArrayList.remove(i);
                    }
                }
                adapterMyVideoFileList = new AdapterMyVideoFileList(context, fileArrayList);
                adapterMyVideoFileList.notifyDataSetChanged();
                rv_myrecordinglist.setAdapter(adapterMyVideoFileList);
            }
            else {
                Toast.makeText(context, "Data Not Found", Toast.LENGTH_LONG).show();
            }
        }


    }
    @Override
    public void onResume() {
        super.onResume();
        getAllFiles();
        if (fileArrayList.size() == 0) {
            rl_videonotfound.setVisibility(View.VISIBLE);
        } else {
            rl_videonotfound.setVisibility(View.GONE);
        }
    }
}
