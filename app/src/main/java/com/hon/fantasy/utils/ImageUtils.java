package com.hon.fantasy.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v8.renderscript.RenderScript;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hon.fantasy.Fantasy;
import com.hon.fantasy.R;
import com.hon.fantasy.api.LastFmClient;
import com.hon.fantasy.api.callbacks.AlbumInfoListener;
import com.hon.fantasy.api.models.AlbumQuery;
import com.hon.fantasy.api.models.LastfmAlbum;
import com.hon.fantasy.dataloader.AlbumLoader;
import com.hon.fantasy.models.Album;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class ImageUtils {
    private static final DisplayImageOptions lastfmDisplayImageOptions =
            new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .showImageOnFail(R.drawable.ic_empty_music2)
                    .build();

    private static final DisplayImageOptions diskDisplayImageOptions =
            new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .build();

    public static void loadAlbumArtIntoView(final long albumId, final ImageView view) {
        loadAlbumArtIntoView(albumId, view, new SimpleImageLoadingListener());
    }

    public static void loadAlbumArtIntoView(final long albumId, final ImageView view,
                                            final ImageLoadingListener listener) {
        if (PreferencesUtility.getInstance(view.getContext()).alwaysLoadAlbumImagesFromLastfm()) {
            loadAlbumArtFromLastfm(albumId, view, listener);
        } else {
            loadAlbumArtFromDiskWithLastfmFallback(albumId, view, listener);
        }
    }

    private static void loadAlbumArtFromDiskWithLastfmFallback(final long albumId, ImageView view,
                                                               final ImageLoadingListener listener) {
        ImageLoader.getInstance()
                .displayImage(FantasyUtils.getAlbumArtUri(albumId).toString(),
                        view,
                        diskDisplayImageOptions,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingFailed(String imageUri, View view,
                                                        FailReason failReason) {
                                loadAlbumArtFromLastfm(albumId, (ImageView) view, listener);
                                listener.onLoadingFailed(imageUri, view, failReason);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                listener.onLoadingComplete(imageUri, view, loadedImage);
                            }
                        });
    }

    private static void loadAlbumArtFromLastfm(long albumId, final ImageView albumArt, final ImageLoadingListener listener) {
        Album album = AlbumLoader.getAlbum(albumArt.getContext(), albumId);
        LastFmClient.getInstance(albumArt.getContext())
                .getAlbumInfo(new AlbumQuery(album.title, album.artistName),
                        new AlbumInfoListener() {
                            @Override
                            public void albumInfoSuccess(final LastfmAlbum album) {
                                if (album != null) {
                                    ImageLoader.getInstance()
                                            .displayImage(album.mArtwork.get(4).mUrl,
                                                    albumArt,
                                                    lastfmDisplayImageOptions, new SimpleImageLoadingListener(){
                                                        @Override
                                                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                                            listener.onLoadingComplete(imageUri, view, loadedImage);
                                                        }

                                                        @Override
                                                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                                            listener.onLoadingFailed(imageUri, view, failReason);
                                                        }
                                                    });
                                }
                            }

                            @Override
                            public void albumInfoFailed() {
                                Toast.makeText(Fantasy.sContext, "albumInfoFailed :)", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    public static Drawable createBlurredImageFromBitmap(Bitmap bitmap, Context context, int inSampleSize) {

        RenderScript rs = RenderScript.create(context);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
        Bitmap blurTemplate = BitmapFactory.decodeStream(bis, null, options);

        final android.support.v8.renderscript.Allocation input = android.support.v8.renderscript.Allocation.createFromBitmap(rs, blurTemplate);
        final android.support.v8.renderscript.Allocation output = android.support.v8.renderscript.Allocation.createTyped(rs, input.getType());
        final android.support.v8.renderscript.ScriptIntrinsicBlur script = android.support.v8.renderscript.ScriptIntrinsicBlur.create(rs, android.support.v8.renderscript.Element.U8_4(rs));
        script.setRadius(8f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(blurTemplate);

        return new BitmapDrawable(context.getResources(), blurTemplate);
    }
}

