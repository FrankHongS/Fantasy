package com.hon.fantasy.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.transition.Transition;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hon.fantasy.R;
import com.hon.fantasy.adapter.SongsListAdapter;
import com.hon.fantasy.dataloader.LastAddedLoader;
import com.hon.fantasy.dataloader.PlaylistLoader;
import com.hon.fantasy.dataloader.PlaylistSongLoader;
import com.hon.fantasy.dataloader.SongLoader;
import com.hon.fantasy.dataloader.TopTracksLoader;
import com.hon.fantasy.listeners.SimplelTransitionListener;
import com.hon.fantasy.models.Song;
import com.hon.fantasy.utils.Constants;
import com.hon.fantasy.utils.FantasyUtils;
import com.hon.fantasy.widget.DividerItemDecoration;
import com.hon.fantasy.widget.DragSortRecycler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class PlaylistDetailActivity extends BaseActivity implements ATEActivityThemeCustomizer, ATEToolbarCustomizer {

    private String action;
    private long playlistID;
    private HashMap<String, Runnable> playlistsMap = new HashMap<>();

    private AppCompatActivity mContext = PlaylistDetailActivity.this;
    private SongsListAdapter mAdapter;
    private RecyclerView recyclerView;
    private ImageView blurFrame;
    private TextView playlistname;
    private View foreground;
    private boolean animate;

    private Runnable playlistLastAdded = new Runnable() {
        public void run() {
            new loadLastAdded().execute("");
        }
    };
    private Runnable playlistRecents = new Runnable() {
        @Override
        public void run() {
            new loadRecentlyPlayed().execute("");

        }
    };
    private Runnable playlistToptracks = new Runnable() {
        @Override
        public void run() {
            new loadTopTracks().execute("");
        }
    };
    private Runnable playlistUsercreated = new Runnable() {
        @Override
        public void run() {
            new loadUserCreatedPlaylist().execute("");

        }
    };

    @TargetApi(21)
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        action = getIntent().getAction();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_LASTADDED, playlistLastAdded);
        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_RECENT, playlistRecents);
        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_TOPTRACKS, playlistToptracks);
        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_USERCREATED, playlistUsercreated);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        blurFrame = (ImageView) findViewById(R.id.blurFrame);
        playlistname = (TextView) findViewById(R.id.name);
        foreground = findViewById(R.id.foreground);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setAlbumart();

        animate = getIntent().getBooleanExtra(Constants.ACTIVITY_TRANSITION, false);
        if (animate && FantasyUtils.isLollipop()) {
            getWindow().getEnterTransition().addListener(new EnterTransitionListener());
        } else {
            setUpSongs();
        }

    }

    private void setAlbumart() {
        playlistname.setText(getIntent().getExtras().getString(Constants.PLAYLIST_NAME));
        foreground.setBackgroundColor(getIntent().getExtras().getInt(Constants.PLAYLIST_FOREGROUND_COLOR));
        loadBitmap(FantasyUtils.getAlbumArtUri(getIntent().getExtras().getLong(Constants.ALBUM_ID)).toString());
    }

    private void setUpSongs() {
        Runnable navigation = playlistsMap.get(action);
        if (navigation != null) {
            navigation.run();

            DragSortRecycler dragSortRecycler = new DragSortRecycler();
            dragSortRecycler.setViewHandleId(R.id.reorder);

            dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
                @Override
                public void onItemMoved(int from, int to) {
                    Log.d("playlist", "onItemMoved " + from + " to " + to);
                    Song song = mAdapter.getSongAt(from);
                    mAdapter.removeSongAt(from);
                    mAdapter.addSongTo(to, song);
                    mAdapter.notifyDataSetChanged();
                    MediaStore.Audio.Playlists.Members.moveItem(getContentResolver(),
                            playlistID, from, to);
                }
            });

            recyclerView.addItemDecoration(dragSortRecycler);
            recyclerView.addOnItemTouchListener(dragSortRecycler);
            recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());

        } else {
            Log.d("PlaylistDetail", "mo action specified");
        }
    }

    private void loadBitmap(String uri) {
        ImageLoader.getInstance().displayImage(uri, blurFrame,
                new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true)
                        .build());
    }

    private void setRecyclerViewAapter() {
        recyclerView.setAdapter(mAdapter);
        if (animate && FantasyUtils.isLollipop()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white));
                }
            }, 250);
        } else
            recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white));
    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ? R.style.AppTheme_FullScreen_Dark : R.style.AppTheme_FullScreen_Light;

    }

    private class loadLastAdded extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            List<Song> lastadded = LastAddedLoader.getLastAddedSongs(mContext);
            mAdapter = new SongsListAdapter(mContext, lastadded, true, animate);
            mAdapter.setPlaylistId(playlistID);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter();
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class loadRecentlyPlayed extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            TopTracksLoader loader = new TopTracksLoader(mContext, TopTracksLoader.QueryType.RecentSongs);
            List<Song> recentsongs = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());
            mAdapter = new SongsListAdapter(mContext, recentsongs, true, animate);
            mAdapter.setPlaylistId(playlistID);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter();

        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class loadTopTracks extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            TopTracksLoader loader = new TopTracksLoader(mContext, TopTracksLoader.QueryType.TopTracks);
            List<Song> toptracks = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());
            mAdapter = new SongsListAdapter(mContext, toptracks, true, animate);
            mAdapter.setPlaylistId(playlistID);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter();
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class loadUserCreatedPlaylist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            playlistID = getIntent().getExtras().getLong(Constants.PLAYLIST_ID);
            List<Song> playlistsongs = PlaylistSongLoader.getSongsInPlaylist(mContext, playlistID);
            mAdapter = new SongsListAdapter(mContext, playlistsongs, true, animate);
            mAdapter.setPlaylistId(playlistID);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter();
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class EnterTransitionListener extends SimplelTransitionListener {

        @TargetApi(21)
        public void onTransitionEnd(Transition paramTransition) {
            setUpSongs();
        }

        public void onTransitionStart(Transition paramTransition) {
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_playlist_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (action.equals(Constants.NAVIGATE_PLAYLIST_USERCREATED)) {
            menu.findItem(R.id.action_delete_playlist).setVisible(true);
            menu.findItem(R.id.action_clear_auto_playlist).setVisible(false);
        } else {
            menu.findItem(R.id.action_delete_playlist).setVisible(false);
            menu.findItem(R.id.action_clear_auto_playlist).setTitle("Clear " + playlistname.getText().toString());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_delete_playlist:
                showDeletePlaylistDialog();
                break;
            case R.id.action_clear_auto_playlist:
                clearAutoPlaylists();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeletePlaylistDialog() {
        new MaterialDialog.Builder(this)
                .title("Delete playlist?")
                .content("Are you sure you want to delete playlist " + playlistname.getText().toString() + " ?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PlaylistLoader.deletePlaylists(PlaylistDetailActivity.this, playlistID);
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void clearAutoPlaylists() {
        switch (action) {
            case Constants.NAVIGATE_PLAYLIST_LASTADDED:
                FantasyUtils.clearLastAdded(this);
                break;
            case Constants.NAVIGATE_PLAYLIST_RECENT:
                FantasyUtils.clearRecent(this);
                break;
            case Constants.NAVIGATE_PLAYLIST_TOPTRACKS:
                FantasyUtils.clearTopTracks(this);
                break;
        }
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onMetaChanged() {
        super.onMetaChanged();
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public int getToolbarColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public int getLightToolbarMode() {
        return Config.LIGHT_TOOLBAR_AUTO;
    }
}

