package com.cels.photofun;


import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.cels.photofun.MainActivity.buildSegmented;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoFragment extends InterfaceFragment {
    private static final int JPEG_QUALITY = 100;
    private static final int ROTATE_DEGREE = 90;
    private Bitmap imageForC;
    private Bitmap imageBitmap;
    private View fragmentView;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            Log.w("main", "Got message");

            String message = intent.getStringExtra("message");

        }
    };

    public PhotoFragment() {
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       /* String path =	this.getArguments().getString("path");
        File f = new File(path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            imageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        fragmentView = inflater.inflate(R.layout.fragment_photo, null);
        ImageView photoImg = (ImageView) fragmentView.findViewById(R.id.imageView);
        photoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageRotate();
            }
        });
        // photoImg.setImageBitmap(imageBitmap);
        return fragmentView;
    }

    private void imageRotate() {
        if (imageBitmap == null) return;
        else if (imageForC != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(ROTATE_DEGREE);
            imageForC = Bitmap.createBitmap(imageForC, 0, 0, imageForC.getWidth(), imageForC.getHeight(), matrix, true);
            ImageView photoImg = (ImageView) fragmentView.findViewById(R.id.imageView);
            photoImg.setImageBitmap(imageForC);
        } else {
            Matrix matrix = new Matrix();
            matrix.postRotate(ROTATE_DEGREE);
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
            ImageView photoImg = (ImageView) fragmentView.findViewById(R.id.imageView);
            photoImg.setImageBitmap(imageBitmap);

        }
    }

    @Override
    public void setContent(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        File f = new File(path);
        try {
            setContent(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContent(InputStream inputStream) {
        imageBitmap = BitmapFactory.decodeStream(inputStream);
        if (imageBitmap != null) {
            ImageView photoImg = (ImageView) fragmentView.findViewById(R.id.imageView);
            int coefWidth = imageBitmap.getWidth() / photoImg.getWidth();
            int coefHeight = imageBitmap.getHeight() / photoImg.getHeight();
            int coefScale = Math.min(coefWidth, coefHeight);
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth() / coefScale, imageBitmap.getHeight() / coefScale, false);
        }
        //needs to have cutted size!!
        ImageView photoImg = (ImageView) fragmentView.findViewById(R.id.imageView);
        photoImg.setImageBitmap(imageBitmap);

    }

    @Override
    public void save() {
        if (imageForC == null) {
            Toast.makeText(getContext(), "No image for saving", Toast.LENGTH_LONG).show();
            return;
        }
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(System.currentTimeMillis());
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/PhotoFun/");
        if (!storageDir.exists())
            storageDir.mkdirs();
        File image = null;
        try {
            image = new File(storageDir, "Fun" + timeStamp + ".jpeg");
            if (!image.exists()) {
                Log.w("File", "new file created");
                image.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = imageForC;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bos);
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

    @Override
    public void segment(int amountClusters, Button segmentation) {
        imageForC = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), false);
        MyTask myTask = new MyTask(imageForC);
        myTask.execute(amountClusters);

    }

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
            ImageView photoImg = (ImageView) fragmentView.findViewById(R.id.imageView);
            photoImg.setImageBitmap(imageForC);
        }
    }
}
