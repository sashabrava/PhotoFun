package com.cels.photofun;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.*;

import static com.cels.photofun.MainActivity.buildSegmented;

class BitmapExtended {
    static final int NO_IMAGE = 0;
    static final int SUCCESS = 1;
    static final int ERROR_WRITE_FILE = -1;
    private static final int JPEG_QUALITY = 100;
    private static final int ROTATE_DEGREE = 90;
    private Bitmap imageForC;
    private Bitmap imageBitmap;
    private ImageView photoImg;

    BitmapExtended(ImageView photoImg) {
        this.photoImg = photoImg;
    }

    void imageRotate() {
        Bitmap picture = ((BitmapDrawable) photoImg.getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate(ROTATE_DEGREE);
        picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), matrix, true);
        photoImg.setImageBitmap(picture);
    }

    boolean segment(int amountClusters, TableRowExtended panelTop) {
        if (imageBitmap != null) {
            imageForC = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), false);
            MyTask myTask = new MyTask(imageForC, panelTop);
            myTask.execute(amountClusters);
            return true;
        } else return false;
    }

    void setContent(InputStream inputStream) {
        imageBitmap = BitmapFactory.decodeStream(inputStream);
        if (imageBitmap != null) {
            int coefficientWidth = imageBitmap.getWidth() / photoImg.getWidth();
            int coefficientHeight = imageBitmap.getHeight() / photoImg.getHeight();
            int coefficientScale = Math.min(coefficientWidth, coefficientHeight);
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth() / coefficientScale, imageBitmap.getHeight() / coefficientScale, false);
        }
        photoImg.setImageBitmap(imageBitmap);
    }

    int save(File image) {
        if (imageForC == null) {
            return NO_IMAGE;
        }
        Bitmap bitmap = imageForC;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bos);
        byte[] bitmapData = bos.toByteArray();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(image);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return ERROR_WRITE_FILE;
        }
        return SUCCESS;
    }

    class MyTask extends AsyncTask<Integer, Void, Void> {
        private final Bitmap imageForC;
        private final TableRowExtended panelTop;

        MyTask(Bitmap imageForC, TableRowExtended panelTop) {
            this.imageForC = imageForC;
            this.panelTop = panelTop;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            panelTop.disableChildren();
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
            photoImg.setImageBitmap(imageForC);
            panelTop.enableChildren();
        }
    }
}
