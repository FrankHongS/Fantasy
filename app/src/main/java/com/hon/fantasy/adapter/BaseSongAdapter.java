package com.hon.fantasy.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.cast.framework.CastSession;
import com.hon.fantasy.MusicPlayer;
import com.hon.fantasy.activities.BaseActivity;
import com.hon.fantasy.cast.FantasyCastHelper;
import com.hon.fantasy.models.Song;
import com.hon.fantasy.utils.FantasyUtils;
import com.hon.fantasy.utils.NavigationUtils;

import java.util.List;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class BaseSongAdapter<V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {

    @Override
    public V onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(V holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        public ItemHolder(View view) {
            super(view);
        }

    }

    public void playAll(final Activity context, final long[] list, int position,
                        final long sourceId, final FantasyUtils.IdType sourceType,
                        final boolean forceShuffle, final Song currentSong, boolean navigateNowPlaying) {

        if (context instanceof BaseActivity) {
            CastSession castSession = ((BaseActivity) context).getCastSession();
            if (castSession != null) {
                navigateNowPlaying = false;
                FantasyCastHelper.startCasting(castSession, currentSong);
            } else {
                MusicPlayer.playAll(context, list, position, -1, FantasyUtils.IdType.NA, false);
            }
        } else {
            MusicPlayer.playAll(context, list, position, -1, FantasyUtils.IdType.NA, false);
        }

        if (navigateNowPlaying) {
            NavigationUtils.navigateToNowplaying(context, true);
        }


    }
    public void removeSongAt(int i){}
    public void updateDataSet(List<Song> arraylist) {}

}

