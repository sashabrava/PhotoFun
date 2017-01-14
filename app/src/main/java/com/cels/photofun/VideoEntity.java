package com.cels.photofun;


import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import org.jcodec.api.JCodecException;

import java.io.File;
import java.io.IOException;

import static com.cels.photofun.MainActivity.buildSegmented;

class VideoEntity extends AsyncTask<Integer, Integer, Void> {

    private final String path;
    private final int clusters;
    private final TableRowExtended panelTop;
    private final SeekBar seekBar;
    private final File file;

    VideoEntity(String path, File file, int clusters, TableRowExtended panelTop, SeekBar seekBar) {
        this.file = file;
        this.path = path;
        this.clusters = clusters;
        this.panelTop = panelTop;
        this.seekBar = seekBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        panelTop.disableChildren();
        seekBar.setVisibility(View.VISIBLE);
    }

    protected void onProgressUpdate(Integer... progress) {
        if (progress.length > 0) seekBar.setProgress(progress[0]);
    }

    @Override
    protected Void doInBackground(Integer... params) {
        final int MAX_SECOND_ON_ERROR = 3;
        final double STEP = 0.1;
        final File videoFile = new File(path);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        double maxSecond;
        try {
            retriever.setDataSource(path);
            Log.w("amount of", "" + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            maxSecond = Double.valueOf(time) / 1000;
        } catch (Exception e) {
            e.printStackTrace();
            maxSecond = MAX_SECOND_ON_ERROR;
        }
        SequenceEncoder enc;
        try {
            enc = new SequenceEncoder(file);
            double second = 0;
            Looper.prepare();
            Bitmap bitmap;
            while (second < maxSecond) {
                bitmap = org.jcodec.api.android.FrameGrab.getFrame(videoFile, second);
                second += STEP;
                Log.w("File", "second segmented " + second);
                buildSegmented(bitmap, clusters);
                enc.encodeImage(bitmap);
                publishProgress((int) (100 * second / maxSecond));
            }
            enc.finish();
        } catch (IOException | JCodecException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        panelTop.enableChildren();
        seekBar.setProgress(0);
        seekBar.setVisibility(View.GONE);
    }
}

