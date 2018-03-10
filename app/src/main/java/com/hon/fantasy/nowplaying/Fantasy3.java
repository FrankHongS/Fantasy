package com.hon.fantasy.nowplaying;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hon.fantasy.R;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class Fantasy3 extends BaseNowplayingFragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_fantasy3, container, false);

        setMusicStateListener();
        setSongDetails(rootView);

        initGestures(rootView.findViewById(R.id.album_art));

        return rootView;
    }
}
