package com.hon.fantasy.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.hon.fantasy.R;
import com.hon.fantasy.api.LastFmClient;
import com.hon.fantasy.api.callbacks.ArtistInfoListener;
import com.hon.fantasy.api.models.ArtistQuery;
import com.hon.fantasy.api.models.LastfmArtist;
import com.hon.fantasy.models.Artist;
import com.hon.fantasy.utils.FantasyUtils;
import com.hon.fantasy.utils.Helpers;
import com.hon.fantasy.utils.NavigationUtils;
import com.hon.fantasy.utils.PreferencesUtility;
import com.hon.fantasy.widget.BubbleTextGetter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

/**
 * Created by Frank on 2018/3/5.
 * E-mail:frank_hon@foxmail.com
 */

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ItemHolder> implements BubbleTextGetter {

    private List<Artist> arraylist;
    private Activity mContext;
    private boolean isGrid;

    public ArtistAdapter(Activity context, List<Artist> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.isGrid = PreferencesUtility.getInstance(mContext).isArtistsInGrid();
    }

    public static int getOpaqueColor(@ColorInt int paramInt) {
        return 0xFF000000 | paramInt;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (isGrid) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist_grid, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        }
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {
        final Artist localItem = arraylist.get(i);

        itemHolder.name.setText(localItem.name);
        String albumNmber = FantasyUtils.makeLabel(mContext, R.plurals.Nalbums, localItem.albumCount);
        String songCount = FantasyUtils.makeLabel(mContext, R.plurals.Nsongs, localItem.songCount);
        itemHolder.albums.setText(FantasyUtils.makeCombinedString(mContext, albumNmber, songCount));


        LastFmClient.getInstance(mContext).getArtistInfo(new ArtistQuery(localItem.name), new ArtistInfoListener() {
            @Override
            public void artistInfoSucess(LastfmArtist artist) {
                if (artist != null && artist.mArtwork != null) {
                    if (isGrid) {
                        ImageLoader.getInstance().displayImage(artist.mArtwork.get(2).mUrl, itemHolder.artistImage,
                                new DisplayImageOptions.Builder().cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .showImageOnLoading(R.drawable.ic_empty_music2)
                                        .resetViewBeforeLoading(true)
                                        .displayer(new FadeInBitmapDisplayer(400))
                                        .build(), new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        if (isGrid && loadedImage != null) {
                                            new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                                @Override
                                                public void onGenerated(Palette palette) {
                                                    int color = palette.getVibrantColor(Color.parseColor("#66000000"));
                                                    itemHolder.footer.setBackgroundColor(color);
                                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                                    int textColor;
                                                    if (swatch != null) {
                                                        textColor = getOpaqueColor(swatch.getTitleTextColor());
                                                    } else textColor = Color.parseColor("#ffffff");

                                                    itemHolder.name.setTextColor(textColor);
                                                    itemHolder.albums.setTextColor(textColor);
                                                }
                                            });
                                        }

                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                        if (isGrid) {
                                            itemHolder.footer.setBackgroundColor(0);
                                            if (mContext != null) {
                                                int textColorPrimary = Config.textColorPrimary(mContext, Helpers.getATEKey(mContext));
                                                itemHolder.name.setTextColor(textColorPrimary);
                                                itemHolder.albums.setTextColor(textColorPrimary);
                                            }
                                        }
                                    }
                                });
                    } else {
                        ImageLoader.getInstance().displayImage(artist.mArtwork.get(1).mUrl, itemHolder.artistImage,
                                new DisplayImageOptions.Builder().cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .showImageOnLoading(R.drawable.ic_empty_music2)
                                        .resetViewBeforeLoading(true)
                                        .displayer(new FadeInBitmapDisplayer(400))
                                        .build());
                    }
                }
            }

            @Override
            public void artistInfoFailed() {

            }
        });

        if (FantasyUtils.isLollipop())
            itemHolder.artistImage.setTransitionName("transition_artist_art" + i);

    }

    @Override
    public long getItemId(int position) {
        return arraylist.get(position).id;
    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    @Override
    public String getTextToShowInBubble(final int pos) {
        if (arraylist == null || arraylist.size() == 0)
            return "";
        return Character.toString(arraylist.get(pos).name.charAt(0));
    }

    public void updateDataSet(List<Artist> arrayList) {
        this.arraylist = arrayList;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView name, albums;
        protected ImageView artistImage;
        protected View footer;

        public ItemHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.artist_name);
            this.albums = (TextView) view.findViewById(R.id.album_song_count);
            this.artistImage = (ImageView) view.findViewById(R.id.artistImage);
            this.footer = view.findViewById(R.id.footer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtils.navigateToArtist(mContext, arraylist.get(getAdapterPosition()).id,
                    new Pair<View, String>(artistImage, "transition_artist_art" + getAdapterPosition()));
        }

    }
}

