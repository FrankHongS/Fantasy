package com.hon.fantasy.nowplaying;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hon.fantasy.MusicPlayer;
import com.hon.fantasy.MusicService;
import com.hon.fantasy.R;
import com.hon.fantasy.utils.FantasyUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class Fantasy1 extends BaseNowplayingFragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_fantasy1, container, false);

        setMusicStateListener();
        setSongDetails(rootView);
        initGestures(rootView.findViewById(R.id.album_art));

        return rootView;
    }

    @Override
    public void updateShuffleState() {
        if (shuffle != null && getActivity() != null) {
            MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(getActivity())
                    .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                    .setSizeDp(30);

            builder.setColor(FantasyUtils.getBlackWhiteColor(accentColor));

            shuffle.setImageDrawable(builder.build());
            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MusicPlayer.getInstance().setShuffleMode(MusicService.SHUFFLE_NORMAL);
                            MusicPlayer.getInstance().next();
                            recyclerView.scrollToPosition(MusicPlayer.getInstance().getQueuePosition());
                        }
                    }, 150);

                }
            });
        }
    }
}
