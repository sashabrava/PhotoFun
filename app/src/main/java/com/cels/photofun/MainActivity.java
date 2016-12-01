package com.cels.photofun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    public static final String TEMP_FOLDER = "photos";
    private static final String TEMP_FILE = "temp.mp4";
    private static final int REQUEST_IMAGE_GALLERY = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;

    private static final int FRAGMENT_PHOTO = 0;
    private static final int FRAGMENT_VIDEO = 1;

    static {
        System.loadLibrary("buildSegmented");
    }

    int fragmentType = FRAGMENT_VIDEO;
    InterfaceFragment frag;
    private SeekBar seekBar;

    public static native Bitmap buildSegmented(Bitmap bitmap, int clusterCount);

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
        Button changeFragment = (Button) findViewById(R.id.buttonChangeType);
        changeFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentType == FRAGMENT_VIDEO) {
                    File f = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
                    Bundle args = new Bundle();
                    args.putString("path", f.getPath());
                    fragmentType = FRAGMENT_PHOTO;
                    frag = new PhotoFragment();
                    frag.setArguments(args);
                    findViewById(R.id.buttonSetSegmentation).setEnabled(true);
                    findViewById(R.id.buttonSave).setEnabled(true);
                    findViewById(R.id.buttonLoad).setEnabled(true);
                    ((Button) v).setText("Video");
                    setFragment();
                } else {
                    File f = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
                    Bundle args = new Bundle();
                    args.putString("path", f.getPath());
                    fragmentType = FRAGMENT_VIDEO;
                    frag = new VideoFragment();
                    frag.setArguments(args);
                    findViewById(R.id.buttonSave).setEnabled(false);
                    //findViewById(R.id.buttonLoad).setEnabled(false);
                    ((Button) v).setText("Photo");
                    setFragment();
                }

            }
        });
        seekBar = (SeekBar) findViewById(R.id.seekBar2);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_lock_power_off);
        }
        Button photo = (Button) findViewById(R.id.buttonTakePic);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (fragmentType) {
                    case FRAGMENT_PHOTO:
                        dispatchTakePictureIntent();
                        break;
                    case FRAGMENT_VIDEO:
                        dispatchTakeVideoIntent();
                        break;
                }

            }
        });
        Button segmentation = (Button) findViewById(R.id.buttonSetSegmentation);
        segmentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frag.segment(seekBar.getProgress(), (Button) findViewById(R.id.buttonSetSegmentation));
               /* if (imageBitmap != null) {
                    imageForC = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), false);
                    MyTask myTask = new MyTask(imageForC);
                    myTask.execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Please choose the image", Toast.LENGTH_LONG).show();
                }*/

            }
        });

        Button load = (Button) findViewById(R.id.buttonLoad);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                switch (fragmentType) {
                    case FRAGMENT_PHOTO:
                        intent.setType("image/*");
                        break;
                    case FRAGMENT_VIDEO:
                        intent.setType("video/*");
                        break;
                }
                //intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select"), REQUEST_IMAGE_GALLERY);
                // newImage = true;
            }
        });
        Button save = (Button) findViewById(R.id.buttonSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saveImageFile();
                frag.save();
            }
        });
    }

    public void setFragment() {
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.add(R.id.frameLayout, frag);
        trans.commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        File f = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
        Bundle args = new Bundle();
        args.putString("path", f.getPath());
        frag = new VideoFragment();
        findViewById(R.id.buttonSave).setEnabled(false);
        //findViewById(R.id.buttonLoad).setEnabled(false);
        //frag = new PhotoFragment();
        frag.setArguments(args);
        setFragment();
        start();
    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void dispatchTakeVideoIntent() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File f = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
    }

   /* private void saveImageFile() {
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
    }*/

  /*  private void addImage() {
        if (imageBitmap == null) return;
        ImageView photoImg = (ImageView) findViewById(R.id.imageView);
        int coefWidth = imageBitmap.getWidth() / photoImg.getWidth();
        int coefHeight = imageBitmap.getHeight() / photoImg.getHeight();
        int coefScale = Math.min(coefWidth, coefHeight);
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth() / coefScale, imageBitmap.getHeight() / coefScale, false);
        photoImg.setImageBitmap(imageBitmap);

    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                File f = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
                String path = f.getPath();
                frag.setContent(path);

                break;
            case REQUEST_IMAGE_GALLERY:
                try {

                    InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                    frag.setContent(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;

            case REQUEST_VIDEO_CAPTURE:
                File f1 = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
                String path1 = f1.getPath();
                frag.setContent(path1);
                break;
            ///ImageView photoImg = (ImageView) findViewById(R.id.imageView);
                 /*BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                try {
                   Bitmap imageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                    PhotoFragment fragment = new PhotoFragment();
                   if (photoImg.getWidth() > 0 && photoImg.getHeight() > 0) {
                        addImage();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please try again. Error has occured", Toast.LENGTH_LONG).show();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }*/


        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {


        } /*else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            try {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                imageBitmap = BitmapFactory.decodeStream(inputStream);
                addImage();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }*/
    }

    static class MyTask extends AsyncTask<Integer, Void, Void> {

        private final Bitmap imageForC;

        MyTask(Bitmap imageForC) {
            this.imageForC = imageForC;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Button segment = (Button) findViewById(R.id.buttonSetSegmentation);
            // segment.setText(getApplicationContext().getString(R.string.segment_process));
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
           /* ImageView photo = (ImageView) findViewById(R.id.imageView);
            photo.setImageBitmap(imageForC);
            Toast.makeText(getApplicationContext(), "Successful segmentation", Toast.LENGTH_LONG).show();
            Button segment = (Button) findViewById(R.id.buttonSetSegmentation);
            segment.setText(getApplicationContext().getString(R.string.segment));*/
            //newImage = false;
        }
    }
}
