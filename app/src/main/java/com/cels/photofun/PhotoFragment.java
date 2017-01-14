package com.cels.photofun;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


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
        File file = createFile("/PhotoFun/", ".jpg");
        if (file != null)
            switch (model.save(file)) {
            case BitmapExtended.SUCCESS:
                Toast.makeText(getContext(), "Successfully saved", Toast.LENGTH_LONG).show();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATA, file.getPath());
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
