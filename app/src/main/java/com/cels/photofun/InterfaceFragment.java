package com.cels.photofun;

import android.support.v4.app.Fragment;
import android.widget.Button;

import java.io.InputStream;

/**
 * Created by Sasha on 19.11.2016.
 */
public abstract class InterfaceFragment extends Fragment {
    public abstract void setContent(String path);

    public abstract void setContent(InputStream inputStream);

    public abstract void save();

    public abstract void segment(int amountClusters, Button button);

}
