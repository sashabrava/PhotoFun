package com.cels.photofun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.io.*;


public class VideoFragment extends InterfaceFragment {
    private String path;
    private SeekBar seekBar;
    private View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_video, container, false);
        seekBar = (SeekBar) fragmentView.findViewById(R.id.seekBarProgress);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        return fragmentView;
    }

    @Override
    public void setContent(String path) {
        this.path = path;
        final VideoView videoView = (VideoView) fragmentView.findViewById(R.id.videoView);
        videoView.setVideoPath(path);
        fragmentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.start();
            }
        });
    }

    @Override
    public void setContent(InputStream inputStream) {
        File file = null;
        String cacheFile = "cacheFile.mp4";
        try {
            file = new File(getContext().getExternalFilesDir(MainActivity.TEMP_FOLDER), cacheFile);
            OutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024];
                    int read;

                    while ((read = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();

                    output.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                if (file != null) {
                    setContent(file.getPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save() {
    }

    @Override
    public void segment(int amountClusters, Button segmentation) {
        TableRowExtended panelTop = ((TableRowExtended) segmentation.getParent());
        File newFile = createFile("/VideoFun/", ".mp4");
        VideoEntity myTask = new VideoEntity(path, newFile, amountClusters + 1, panelTop, seekBar);
        myTask.execute();
    }
}



