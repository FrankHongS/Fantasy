package com.hon.fantasy.fragments;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hon.fantasy.R;
import com.hon.fantasy.adapter.ArtistAdapter;
import com.hon.fantasy.dataloader.ArtistLoader;
import com.hon.fantasy.models.Artist;
import com.hon.fantasy.utils.PreferencesUtility;
import com.hon.fantasy.utils.SortOrder;
import com.hon.fantasy.widget.BaseRecyclerView;
import com.hon.fantasy.widget.DividerItemDecoration;
import com.hon.fantasy.widget.FastScroller;

import java.util.List;

/**
 * Created by Frank on 2018/3/5.
 * E-mail:frank_hon@foxmail.com
 */

public class ArtistFragment extends Fragment {

    private ArtistAdapter mAdapter;
    private BaseRecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private PreferencesUtility mPreferences;
    private boolean isGrid;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(getActivity());
        isGrid = mPreferences.isArtistsInGrid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_recyclerview, container, false);

        recyclerView =  rootView.findViewById(R.id.recyclerview);
        FastScroller fastScroller = rootView.findViewById(R.id.fastscroller);
        fastScroller.setRecyclerView(recyclerView);
        recyclerView.setEmptyView(getActivity(), rootView.findViewById(R.id.list_empty), "No media found");

        setLayoutManager();

        if (getActivity() != null)
            new loadArtists().execute("");
        return rootView;
    }

    private void setLayoutManager() {
        if (isGrid) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
        } else {
            layoutManager = new GridLayoutManager(getActivity(), 1);
        }
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setItemDecoration() {
        if (isGrid) {
            int spacingInPixels = getActivity().getResources().getDimensionPixelSize(R.dimen.spacing_card_album_grid);
            itemDecoration = new SpacesItemDecoration(spacingInPixels);
        } else {
            itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        }
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void updateLayoutManager(int column) {
        recyclerView.removeItemDecoration(itemDecoration);
        recyclerView.setAdapter(new ArtistAdapter(getActivity(), ArtistLoader.getAllArtists(getActivity())));
        layoutManager.setSpanCount(column);
        layoutManager.requestLayout();
        setItemDecoration();
    }

    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                List<Artist> artistList = ArtistLoader.getAllArtists(getActivity());
                mAdapter.updateDataSet(artistList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.artist_sort_by, menu);
        inflater.inflate(R.menu.menu_show_as, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_za:
                mPreferences.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_Z_A);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_number_of_songs:
                mPreferences.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_SONGS);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_number_of_albums:
                mPreferences.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_ALBUMS);
                reloadAdapter();
                return true;
            case R.id.menu_show_as_list:
                mPreferences.setArtistsInGrid(false);
                isGrid = false;
                updateLayoutManager(1);
                return true;
            case R.id.menu_show_as_grid:
                mPreferences.setArtistsInGrid(true);
                isGrid = true;
                updateLayoutManager(2);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class loadArtists extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null)
                mAdapter = new ArtistAdapter(getActivity(), ArtistLoader.getAllArtists(getActivity()));
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if (mAdapter != null) {
                mAdapter.setHasStableIds(true);
                recyclerView.setAdapter(mAdapter);
            }
            if (getActivity() != null) {
                setItemDecoration();
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.top = space;
            outRect.right = space;
            outRect.bottom = space;

        }
    }

}

