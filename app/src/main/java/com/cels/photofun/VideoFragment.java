package com.cels.photofun;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;
import org.jcodec.api.JCodecException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.cels.photofun.MainActivity.buildSegmented;


public class VideoFragment extends InterfaceFragment {
    String path;
    SeekBar seekBar;
    Button segmentation;
    private View fragmentView;
    private int[] buffer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_video, null);
        seekBar = (SeekBar) fragmentView.findViewById(R.id.seekBarProgress);
        return fragmentView;
    }

    @Override
    public void setContent(String path) {
        this.path = path;
        VideoView videoView = (VideoView) fragmentView.findViewById(R.id.videoView);
        videoView.setVideoPath(path);
        videoView.start();

    }

    @Override
    public void setContent(InputStream inputStream) {
        File file = null;
        try {
            file = new File(getContext().getExternalFilesDir(MainActivity.TEMP_FOLDER), "cacheFile.mp4");
            OutputStream output = new FileOutputStream(file);
            try {
                try {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    while ((read = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                } finally {
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace(); // handle exception, define IOException and others
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                setContent(file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    @Override
    public void save() {
    }

    public void save(Bitmap imageForC, double second) {
        if (imageForC == null) {
            Log.w("File", "empty file on time " + second);
            Toast.makeText(getContext(), "No image for saving", Toast.LENGTH_LONG).show();
            return;
        }
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(System.currentTimeMillis());
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/PhotoFun/");
        if (!storageDir.exists())
            storageDir.mkdirs();
        File image = null;
        try {
            image = new File(storageDir, "Fun_scr" + second + ".jpeg");
            if (!image.exists()) {
                Log.w("File", "new file created with time" + 1.0 * (((int) second * 100)) / 100);
                image.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = imageForC;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(image);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(getContext(), "Successfully saved to DCIM/PhotoFun", Toast.LENGTH_LONG).show();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, image.getPath());
        getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private int[] getBuffer(int width, int height) {
        if (buffer == null || buffer.length < width * height) {
            buffer = new int[width * height];
        }
        return buffer;
    }

    @Override
    public void segment(int amountClusters, Button segmentation) {
        MyTask myTask = new MyTask(path, amountClusters + 1);
        this.segmentation = segmentation;
        myTask.execute();

    }

    class MyTask extends AsyncTask<Integer, Integer, Void> {

        private final String path;
        private final int clusters;
        Bitmap bitmap;

        MyTask(String string, int clusters) {
            this.path = string;
            this.clusters = clusters;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //if (segmentation != null)
            segmentation.setEnabled(false);
        }

        protected void onProgressUpdate(Integer... progress) {
            seekBar.setProgress(progress[0]);
        }

        @Override
        protected Void doInBackground(Integer... params) {
            final File videoFile = new File(path);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            double maxSecond;
            try {
                retriever.setDataSource(path);
                Log.w("amounf of", "" + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                maxSecond = Double.valueOf(time) / 1000;
            } catch (Exception e) {
                e.printStackTrace();
                maxSecond = 3;
            }

            SequenceEncoder enc = null;
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/VideoFun/");
            if (!storageDir.exists())
                storageDir.mkdirs();
            String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(System.currentTimeMillis());
            try {
                enc = new SequenceEncoder(new File(storageDir, "newVideo" + timeStamp + ".mp4"));

            } catch (IOException e) {
                e.printStackTrace();
            }
            double second = 0;

            Looper.prepare();
            final SequenceEncoder finalEnc = enc;
            while (second < maxSecond) {
                try {
                    bitmap = org.jcodec.api.android.FrameGrab.getFrame(videoFile, second);
                    second += 0.1;
                    Log.w("File", "second segmented " + second);
                    buildSegmented(bitmap, clusters);
                    finalEnc.encodeImage(bitmap);
                    publishProgress((int) (100 * second / maxSecond));
                    // Toast.makeText(getContext(), "Ready " + (int)(100*second/maxSecond) + " %", Toast.LENGTH_SHORT).show();

                } catch (IOException | JCodecException e) {
                    e.printStackTrace();
                    Log.w("VIDEO", "Maybe it' the end");
                    break;
                }
            }
            try {
                //Audio audio =
                // Soun
                // finalEnc.
                //PCMMP4MuxerTrack track
                finalEnc.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // if (segmentation != null)
            segmentation.setEnabled(true);
            seekBar.setProgress(0);

        }
    }
    }



