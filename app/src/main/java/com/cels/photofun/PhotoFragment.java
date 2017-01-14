package com.cels.photofun;


import android.content.ContentValues;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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


public class PhotoFragment extends InterfaceFragment {

    private BitmapExtended model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_photo, container, false);
        ImageView photoImg = (ImageView) fragmentView.findViewById(R.id.imageView);
        if (model == null) model = new BitmapExtended(photoImg);
        photoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.imageRotate();
            }
        });
        return fragmentView;
    }



    @Override
    public void setContent(String path) {
        File f = new File(path);
        try {
            setContent(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContent(InputStream inputStream) {
        model.setContent(inputStream);
    }
    @Override
    public void save() {
        String titleTemplate = "yyyy_MM_dd_HH_mm_ss";
        String folder = "/PhotoFun/";
        String timeStamp = new SimpleDateFormat(titleTemplate, Locale.getDefault()).format(System.currentTimeMillis());
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + folder);
        boolean createFolder = true, createFile = true;
        if (!storageDir.exists())
            createFolder = storageDir.mkdirs();
        File image = null;

        try {
            image = new File(storageDir, "Fun" + timeStamp + ".jpeg");
            if (!image.exists()) {
                Log.w("File", "new file created");
                createFile = image.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!createFolder || !createFile) {
            Toast.makeText(getContext(), "Error creating file", Toast.LENGTH_LONG).show();
            return;
        }
        switch (model.save(image)) {
            case BitmapExtended.SUCCESS:
                Toast.makeText(getContext(), "Successfully saved", Toast.LENGTH_LONG).show();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATA, image.getPath());
                getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                break;
            case BitmapExtended.ERROR_WRITE_FILE:
                Toast.makeText(getContext(), "Error writing file", Toast.LENGTH_LONG).show();
                break;
            case BitmapExtended.NO_IMAGE:
                Toast.makeText(getContext(), "No image for saving", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void segment(int amountClusters, Button segmentation) {
        TableRowExtended panelTop = ((TableRowExtended) segmentation.getParent());
        if (!model.segment(amountClusters, panelTop))
            Toast.makeText(getContext(), "No image for segmenting", Toast.LENGTH_LONG).show();
    }
}
