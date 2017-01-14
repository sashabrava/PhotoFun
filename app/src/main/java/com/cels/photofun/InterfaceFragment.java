package com.cels.photofun;

import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class InterfaceFragment extends Fragment {
    public abstract void setContent(String path);

    public abstract void setContent(InputStream inputStream);

    public abstract void save();

    public abstract void segment(int amountClusters, Button button);

    File createFile(String folder, String format) {
        String titleTemplate = "yyyy_MM_dd_HH_mm_ss";
        String timeStamp = new SimpleDateFormat(titleTemplate, Locale.getDefault()).format(System.currentTimeMillis());
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + folder);
        boolean createFolder = true, createFile = true;
        if (!storageDir.exists())
            createFolder = storageDir.mkdirs();
        File image;

        try {
            image = new File(storageDir, "Fun" + timeStamp + format);
            if (!image.exists()) {
                Log.w("File", "new file created");
                createFile = image.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (!createFolder || !createFile) {
            Toast.makeText(getContext(), "Error creating file", Toast.LENGTH_LONG).show();
            return null;
        }
        return image;
    }
}
