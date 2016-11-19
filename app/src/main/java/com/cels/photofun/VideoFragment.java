package com.cels.photofun;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import java.io.InputStream;

import static com.cels.photofun.MainActivity.buildSegmented;


public class VideoFragment extends InterfaceFragment {
    private View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_video, null);
        return fragmentView;
    }

    @Override
    public void setContent(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //img.setImageBitmap(retriever.getFrameAtTime(1000,MediaMetadataRetriever.OPTION_CLOSEST));
        retriever.setDataSource(path);
        //retriever.get
        VideoView videoView = (VideoView) fragmentView.findViewById(R.id.videoView);
        videoView.setVideoPath(path);
        videoView.start();
        //videoView.ret
        //Video
        // videoView.getVide
        //videoView.save

    }

    @Override
    public void setContent(InputStream inputStream) {

    }

    @Override
    public void save() {

    }

    @Override
    public void segment(int amountClusters) {
        class MyTask extends AsyncTask<Integer, Void, Void> {

            private final Bitmap imageForC;

            MyTask(Bitmap imageForC) {
                this.imageForC = imageForC;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Integer... params) {
                int amountClusters = params[0] + 1;
                buildSegmented(imageForC, amountClusters);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);


            }
        }
    }
}
