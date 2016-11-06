package com.cels.photofun;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TEMP_FOLDER = "photos";
    private static final String TEMP_FILE = "temp.jpg";
    private static final int PICK_IMAGE = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int ROTATE_DEGREE = 90;
    private static final int JPEG_QUALITY = 100;
    static {
        System.loadLibrary("buildSegmented");
    }

    private Bitmap imageForC;
    private Bitmap imageBitmap;
    private SeekBar seekBar;
    private boolean newImage = true;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
        start();
        if (imageForC != null) {
            ImageView photoImg = (ImageView) findViewById(R.id.imageView);
            photoImg.setImageBitmap(imageForC);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void start() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_lock_power_off);
        }
        seekBar = (SeekBar) findViewById(R.id.seekBar2);
        ImageView photoImg = (ImageView) findViewById(R.id.imageView);
        photoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageRotate();
            }
        });
        getWindow().setFormat(PixelFormat.RGBA_8888);
        if (imageBitmap != null) {
            photoImg.setImageBitmap(imageBitmap);
        }
        Button photo = (Button) findViewById(R.id.buttonTakePic);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                newImage = true;
            }
        });
        Button segmentation = (Button) findViewById(R.id.buttonSetSegmentation);
        segmentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageBitmap != null) {
                    imageForC = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), false);
                    MyTask myTask = new MyTask(imageForC);
                    myTask.execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Please choose the image", Toast.LENGTH_LONG).show();
                }

            }
        });

        Button load = (Button) findViewById(R.id.buttonLoad);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                newImage = true;
            }
        });
        Button save = (Button) findViewById(R.id.buttonSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageFile();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        start();
    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File f = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
            ImageView photoImg = (ImageView) findViewById(R.id.imageView);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                imageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                if (photoImg.getWidth() > 0 && photoImg.getHeight() > 0) {
                    addImage();
                } else {
                    Toast.makeText(getApplicationContext(), "Please try again. Error has occured", Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            try {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                imageBitmap = BitmapFactory.decodeStream(inputStream);
                addImage();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImageFile() {
        if (imageForC != null) {
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
                Bitmap bitmap = imageForC;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bos);
                byte[] bitmapData = bos.toByteArray();
                FileOutputStream fos = new FileOutputStream(image);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
                Toast.makeText(getApplicationContext(), "Successfully saved to DCIM/PhotoFun", Toast.LENGTH_LONG).show();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATA, image.getPath());
                getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "There is no segmented image", Toast.LENGTH_LONG).show();
        }
    }

    private void addImage() {
        if (imageBitmap == null) return;
        ImageView photoImg = (ImageView) findViewById(R.id.imageView);
        int coefWidth = imageBitmap.getWidth() / photoImg.getWidth();
        int coefHeight = imageBitmap.getHeight() / photoImg.getHeight();
        int coefScale = Math.min(coefWidth, coefHeight);
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth() / coefScale, imageBitmap.getHeight() / coefScale, false);
        photoImg.setImageBitmap(imageBitmap);

    }

    private void imageRotate() {
        if (imageBitmap == null) return;
        else if (imageForC != null && !newImage) {
            Matrix matrix = new Matrix();
            matrix.postRotate(ROTATE_DEGREE);
            imageForC = Bitmap.createBitmap(imageForC, 0, 0, imageForC.getWidth(), imageForC.getHeight(), matrix, true);
            ImageView photoImg = (ImageView) findViewById(R.id.imageView);
            photoImg.setImageBitmap(imageForC);
        } else {
            Matrix matrix = new Matrix();
            matrix.postRotate(ROTATE_DEGREE);
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
            ImageView photoImg = (ImageView) findViewById(R.id.imageView);
            photoImg.setImageBitmap(imageBitmap);

        }
    }

    public native Bitmap buildSegmented(Bitmap bitmap, int clusterCount);

    class MyTask extends AsyncTask<Void, Void, Void> {

        private final Bitmap imageForC;

        MyTask(Bitmap imageForC) {
            this.imageForC = imageForC;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Button segment = (Button) findViewById(R.id.buttonSetSegmentation);
            segment.setText(getApplicationContext().getString(R.string.segment_process));
        }

        @Override
        protected Void doInBackground(Void... params) {
            buildSegmented(imageForC, seekBar.getProgress() + 1);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ImageView photo = (ImageView) findViewById(R.id.imageView);
            photo.setImageBitmap(imageForC);
            Toast.makeText(getApplicationContext(), "Successful segmentation", Toast.LENGTH_LONG).show();
            Button segment = (Button) findViewById(R.id.buttonSetSegmentation);
            segment.setText(getApplicationContext().getString(R.string.segment));
            newImage = false;
        }
    }
}
