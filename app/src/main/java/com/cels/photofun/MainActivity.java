package com.cels.photofun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
    static final String TEMP_FOLDER = "photos";
    private static final String TEMP_FILE = "temp.phf";
    private static final int REQUEST_IMAGE_GALLERY = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;

    private static final int FRAGMENT_PHOTO = 0;
    private static final int FRAGMENT_VIDEO = 1;

    static {
        System.loadLibrary("buildSegmented");
    }

    private InterfaceFragment frag;
    private SeekBar seekBar;

    public static native Bitmap buildSegmented(Bitmap bitmap, int clusterCount);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickPhoto(View view) {
        switch (view.getId()) {
            case R.id.buttonChangePhotoFragment:
                setFragment(FRAGMENT_VIDEO);
                break;
            case R.id.buttonTakePic:
                dispatchTakePictureIntent();
                break;
            case R.id.buttonLoadPic:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select"), REQUEST_IMAGE_GALLERY);
                break;
            case R.id.buttonSavePhoto:
                frag.save();
                break;
            case R.id.buttonSegmentPhoto:
                frag.segment(seekBar.getProgress(), (Button) view);
                break;
        }
    }

    public void onClickVideo(View view) {
        switch (view.getId()) {
            case R.id.buttonChangeVideoFragment:
                setFragment(FRAGMENT_PHOTO);
                break;
            case R.id.buttonTakeVideo:
                dispatchTakeVideoIntent();
                break;
            case R.id.buttonLoadVideo:
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select"), REQUEST_IMAGE_GALLERY);
                break;
            case R.id.buttonSegmentVideo:
                frag.segment(seekBar.getProgress(), (Button) view);
                break;
        }
    }

    private void setFragment(int fragmentType) {
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        switch (fragmentType) {
            case FRAGMENT_VIDEO:
                frag = new VideoFragment();
                findViewById(R.id.panelTopVideo).setVisibility(View.VISIBLE);
                findViewById(R.id.panelTopPhoto).setVisibility(View.GONE);
                break;
            case FRAGMENT_PHOTO:
                frag = new PhotoFragment();
                findViewById(R.id.panelTopVideo).setVisibility(View.GONE);
                findViewById(R.id.panelTopPhoto).setVisibility(View.VISIBLE);
                break;
        }
        File f = new File(getApplicationContext().getExternalFilesDir(TEMP_FOLDER), TEMP_FILE);
        Bundle args = new Bundle();
        args.putString("path", f.getPath());
        frag.setArguments(args);
        trans.replace(R.id.frameLayout, frag);
        trans.commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        setFragment(FRAGMENT_PHOTO);
        seekBar = (SeekBar) findViewById(R.id.seekBar2);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_lock_power_off);
        }
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
        }
    }
}
