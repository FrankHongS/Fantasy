package com.hon.fantasy.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hon.fantasy.MusicPlayer;
import com.hon.fantasy.R;
import com.hon.fantasy.dialogs.AddPlaylistDialog;
import com.hon.fantasy.models.Song;
import com.hon.fantasy.utils.FantasyUtils;
import com.hon.fantasy.utils.NavigationUtils;

import java.util.List;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class AlbumSongsAdapter extends BaseSongAdapter<AlbumSongsAdapter.ItemHolder> {

    private List<Song> arraylist;
    private Activity mContext;
    private long albumID;
    private long[] songIDs;

    public AlbumSongsAdapter(Activity context, List<Song> arraylist, long albumID) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.songIDs = getSongIds();
        this.albumID = albumID;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_album_song, null);
        ItemHolder ml = new ItemHolder(v);
        return ml;


    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {

        Song localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.title);
        itemHolder.duration.setText(FantasyUtils.makeShortTimeString(mContext, (localItem.duration) / 1000));
        int tracknumber = localItem.trackNumber;
        if (tracknumber == 0) {
            itemHolder.trackNumber.setText("-");
        } else itemHolder.trackNumber.setText(String.valueOf(tracknumber));

        setOnPopupMenuListener(itemHolder, i);


    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_song_play:
                                MusicPlayer.getInstance().playAll(mContext, songIDs, position, -1, FantasyUtils.IdType.NA, false);
                                break;
                            case R.id.popup_song_play_next:
                                long[] ids = new long[1];
                                ids[0] = arraylist.get(position).id;
                                MusicPlayer.getInstance().playNext(mContext, ids, -1, FantasyUtils.IdType.NA);
                                break;
                            case R.id.popup_song_goto_album:
                                NavigationUtils.goToAlbum(mContext, arraylist.get(position).albumId);
                                break;
                            case R.id.popup_song_goto_artist:
                                NavigationUtils.goToArtist(mContext, arraylist.get(position).artistId);
                                break;
                            case R.id.popup_song_addto_queue:
                                long[] id = new long[1];
                                id[0] = arraylist.get(position).id;
                                MusicPlayer.getInstance().addToQueue(mContext, id, -1, FantasyUtils.IdType.NA);
                                break;
                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(arraylist.get(position)).show(((AppCompatActivity) mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;
                            case R.id.popup_song_share:
                                FantasyUtils.shareTrack(mContext, arraylist.get(position).id);
                                break;
                            case R.id.popup_song_delete:
                                long[] deleteIds = {arraylist.get(position).id};
                                FantasyUtils.showDeleteDialog(mContext,arraylist.get(position).title, deleteIds, AlbumSongsAdapter.this, position);
                                break;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_song);
                menu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    public long[] getSongIds() {
        long[] ret = new long[getItemCount()];
        for (int i = 0; i < getItemCount(); i++) {
            ret[i] = arraylist.get(i).id;
        }

        return ret;
    }

    @Override
    public void updateDataSet(List<Song> arraylist) {
        this.arraylist = arraylist;
        this.songIDs = getSongIds();
    }

    @Override
    public void removeSongAt(int i){
        arraylist.remove(i);
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, duration, trackNumber;
        protected ImageView menu;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.duration = (TextView) view.findViewById(R.id.song_duration);
            this.trackNumber = (TextView) view.findViewById(R.id.trackNumber);
            this.menu = (ImageView) view.findViewById(R.id.popup_menu);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playAll(mContext, songIDs, getAdapterPosition(), albumID,
                            FantasyUtils.IdType.Album, false,
                            arraylist.get(getAdapterPosition()), true);
                }
            }, 100);

        }

    }
}

