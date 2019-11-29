package com.hon.fantasy.adapter;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.hon.fantasy.MusicPlayer;
import com.hon.fantasy.R;
import com.hon.fantasy.models.Song;
import com.hon.fantasy.utils.FantasyUtils;
import com.hon.fantasy.utils.Helpers;
import com.hon.fantasy.widget.MusicVisualizer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class BaseQueueAdapter extends RecyclerView.Adapter<BaseQueueAdapter.ItemHolder> {

    public static int currentlyPlayingPosition;
    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private String ateKey;

    public BaseQueueAdapter(AppCompatActivity context, List<Song> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;
        currentlyPlayingPosition = MusicPlayer.getInstance().getQueuePosition();
        this.ateKey = Helpers.getATEKey(context);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song_fantasy1, null);
        ItemHolder ml = new ItemHolder(v);
        return ml;
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {
        Song localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);

        if (MusicPlayer.getInstance().getCurrentAudioId() == localItem.id) {
            itemHolder.title.setTextColor(Config.accentColor(mContext, ateKey));
            if (MusicPlayer.getInstance().isPlaying()) {
                itemHolder.visualizer.setColor(Config.accentColor(mContext, ateKey));
                itemHolder.visualizer.setVisibility(View.VISIBLE);
            } else {
                itemHolder.visualizer.setVisibility(View.GONE);
            }
        } else {
            itemHolder.title.setTextColor(Config.textColorPrimary(mContext, ateKey));
            itemHolder.visualizer.setVisibility(View.GONE);
        }
        ImageLoader.getInstance().displayImage(FantasyUtils.getAlbumArtUri(localItem.albumId).toString(),
                itemHolder.albumArt, new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnLoading(R.drawable.ic_empty_music2).resetViewBeforeLoading(true).build());
        setOnPopupMenuListener(itemHolder, i);
    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                final PopupMenu menu = new PopupMenu(mContext, v);
//                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.popup_song_play:
//                                MusicPlayer.playAll(mContext, getSongIds(), position, -1, FantasyUtils.IdType.NA, false);
//                                break;
//                            case R.id.popup_song_play_next:
//                                long[] ids = new long[1];
//                                ids[0] = arraylist.get(position).id;
//                                MusicPlayer.playNext(mContext, ids, -1, FantasyUtils.IdType.NA);
//                                break;
//                            case R.id.popup_song_goto_album:
//                                NavigationUtils.goToAlbum(mContext, arraylist.get(position).albumId);
//                                break;
//                            case R.id.popup_song_goto_artist:
//                                NavigationUtils.goToArtist(mContext, arraylist.get(position).artistId);
//                                break;
//                            case R.id.popup_song_addto_queue:
//                                long[] id = new long[1];
//                                id[0] = arraylist.get(position).id;
//                                MusicPlayer.addToQueue(mContext, id, -1, FantasyUtils.IdType.NA);
//                                break;
//                            case R.id.popup_song_addto_playlist:
//                                AddPlaylistDialog.newInstance(arraylist.get(position)).show(mContext.getSupportFragmentManager(), "ADD_PLAYLIST");
//                                break;
//                            case R.id.popup_song_share:
//                                FantasyUtils.shareTrack(mContext, arraylist.get(position).id);
//                                break;
//                            case R.id.popup_song_delete:
//                                long[] deleteIds = {arraylist.get(position).id};
//                                FantasyUtils.showDeleteDialog(mContext,arraylist.get(position).title, deleteIds, BaseQueueAdapter.this, position);
//                                break;
//                        }
//                        return false;
//                    }
//                });
//                menu.inflate(R.menu.popup_song);
//                menu.show();
            }
        });
    }

    public long[] getSongIds() {
        long[] ret = new long[getItemCount()];
        for (int i = 0; i < getItemCount(); i++) {
            ret[i] = arraylist.get(i).id;
        }

        return ret;
    }

    public void removeSongAt(int i){
        arraylist.remove(i);
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt, popupMenu;
        private MusicVisualizer visualizer;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.artist = (TextView) view.findViewById(R.id.song_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            visualizer = (MusicVisualizer) view.findViewById(R.id.visualizer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.getInstance().setQueuePosition(getAdapterPosition());
                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged(currentlyPlayingPosition);
                            notifyItemChanged(getAdapterPosition());
                        }
                    }, 50);
                }
            }, 100);

        }

    }

}
