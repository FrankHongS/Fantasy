package com.hon.fantasy.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hon.fantasy.R;
import com.hon.fantasy.adapter.ArtistSongAdapter;
import com.hon.fantasy.dataloader.ArtistSongLoader;
import com.hon.fantasy.models.Song;
import com.hon.fantasy.utils.Constants;
import com.hon.fantasy.widget.DividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class ArtistMusicFragment extends Fragment {

    public static RecyclerView songsRecyclerview;
    private long artistID = -1;
    private ArtistSongAdapter mSongAdapter;

    public static ArtistMusicFragment newInstance(long id) {
        ArtistMusicFragment fragment = new ArtistMusicFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ARTIST_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artistID = getArguments().getLong(Constants.ARTIST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_artist_music, container, false);

        songsRecyclerview = (RecyclerView) rootView.findViewById(R.id.recycler_view_songs);

        setUpSongs();


        return rootView;
    }


    private void setUpSongs() {
        songsRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        ArrayList<Song> songList;
        songList = ArtistSongLoader.getSongsForArtist(getActivity(), artistID);

        // adding one dummy song to top of arraylist
        //there will be albums header at this position in recyclerview
        songList.add(0, new Song(-1, -1, -1, "dummy", "dummy", "dummy", -1, -1));

        mSongAdapter = new ArtistSongAdapter(getActivity(), songList, artistID);
        songsRecyclerview.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        songsRecyclerview.setAdapter(mSongAdapter);
    }
}
