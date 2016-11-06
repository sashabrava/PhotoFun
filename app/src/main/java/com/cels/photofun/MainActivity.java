package com.cels.photofun;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

   /* static {
        System.loadLibrary("startJNI");
    }*/

    static {
        System.loadLibrary("buildSegmented");
    }

    private Bitmap imageBitmap;
    private SeekBar seekBar;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(), "I rotated", Toast.LENGTH_LONG).show();
        start();
    }

    private void start() {
        seekBar = (SeekBar) findViewById(R.id.seekBar2);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        if (imageBitmap != null) {
            ImageView photoImg = (ImageView) findViewById(R.id.imageView);
            photoImg.setImageBitmap(imageBitmap);
        }
        Button photo = (Button) findViewById(R.id.buttonTakePic);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        Button segmentation = (Button) findViewById(R.id.buttonSetSegmentation);
        segmentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageBitmap != null) {
                    Bitmap imageForC = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), false);
                    buildSegmented(imageForC, seekBar.getProgress() + 1);
                    ImageView photo = (ImageView) findViewById(R.id.imageView);
                    //imageBitmap
                    /*KMeans kMeans = new KMeans(imageBitmap, seekBar.getProgress() + 1);
                    Toast.makeText(getApplicationContext(), "" + (seekBar.getProgress() + 1), Toast.LENGTH_LONG).show();
                    imageBitmap = kMeans.getResult();*/
                    photo.setImageBitmap(imageForC);
                    Toast.makeText(getApplicationContext(), "Successful segmentation", Toast.LENGTH_LONG).show();
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
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        start();
        //  Toast.makeText(getApplicationContext(), startNDI(), Toast.LENGTH_LONG).show();



    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f = new File(getApplicationContext().getExternalFilesDir("photos"), "temp.jpg");

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            File f = new File(getApplicationContext().getExternalFilesDir("photos"), "temp.jpg");
            ImageView photoImg = (ImageView) findViewById(R.id.imageView);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                imageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                if (photoImg.getWidth() > 0 && photoImg.getHeight() > 0) {
                    // imageBitmap = Bitmap.createScaledBitmap(imageBitmap, photoImg.getWidth(), photoImg.getHeight(), false);
                    // photoImg.setImageBitmap(imageBitmap);
                    addImage();
                } else {
                    Toast.makeText(getApplicationContext(), "Please try again, but don't turn the screen.", Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            try {
                //ImageView photoImg = (ImageView) findViewById(R.id.imageView);
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                imageBitmap = BitmapFactory.decodeStream(inputStream);
                addImage();
                // imageBitmap = Bitmap.createScaledBitmap(imageBitmap, photoImg.getWidth(), photoImg.getHeight(), false);
                // photoImg.setImageBitmap(imageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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
        photoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageRotate();
            }
        });
    }

    private void imageRotate() {
        if (imageBitmap == null) return;

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
        ImageView photoImg = (ImageView) findViewById(R.id.imageView);
        photoImg.setImageBitmap(imageBitmap);


    }

    public native Bitmap buildSegmented(Bitmap bitmap, int clusterCount);
   /* public native String startNDI();
    static{
        System.loadLibrary("startNDI");
    }*/

}
