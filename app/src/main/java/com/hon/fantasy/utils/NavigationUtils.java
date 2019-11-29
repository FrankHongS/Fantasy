package com.hon.fantasy.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.hon.fantasy.Fantasy;
import com.hon.fantasy.activities.MainActivity;
import com.hon.fantasy.R;
import com.hon.fantasy.activities.NowPlayingActivity;
import com.hon.fantasy.activities.PlaylistDetailActivity;
import com.hon.fantasy.activities.SearchActivity;
import com.hon.fantasy.activities.SettingsActivity;
import com.hon.fantasy.fragments.AlbumDetailFragment;
import com.hon.fantasy.fragments.ArtistDetailFragment;
import com.hon.fantasy.nowplaying.Fantasy1;
import com.hon.fantasy.nowplaying.Fantasy2;
import com.hon.fantasy.nowplaying.Fantasy3;
import com.hon.fantasy.nowplaying.Fantasy4;
import com.hon.fantasy.nowplaying.Fantasy5;
import com.hon.fantasy.nowplaying.Fantasy6;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class NavigationUtils {

    @TargetApi(21)
    public static void navigateToAlbum(Activity context, long albumID, Pair<View, String> transitionViews) {

        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        transaction.setCustomAnimations(R.anim.activity_fade_in,
                R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out);
        fragment = AlbumDetailFragment.newInstance(albumID, false, null);

        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null).commit();

    }

    @TargetApi(21)
    public static void navigateToArtist(Activity context, long artistID, Pair<View, String> transitionViews) {

        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        transaction.setCustomAnimations(R.anim.activity_fade_in,
                R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out);
        fragment = ArtistDetailFragment.newInstance(artistID, false, null);

        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null).commit();

    }

    public static void goToArtist(Context context, long artistId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_ARTIST);
        intent.putExtra(Constants.ARTIST_ID, artistId);
        context.startActivity(intent);
    }

    public static void goToAlbum(Context context, long albumId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_ALBUM);
        intent.putExtra(Constants.ALBUM_ID, albumId);
        context.startActivity(intent);
    }

    public static void goToLyrics(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_LYRICS);
        context.startActivity(intent);
    }

    public static void navigateToNowplaying(Activity context, boolean withAnimations) {

        final Intent intent = new Intent(context, NowPlayingActivity.class);
        context.startActivity(intent);
    }

    public static Intent getNowPlayingIntent() {

        final Intent intent = new Intent(Fantasy.sContext, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_NOWPLAYING);
        return intent;
    }

    public static void navigateToSettings(Activity context) {
        final Intent intent = new Intent(context, SettingsActivity.class);
        intent.setAction(Constants.NAVIGATE_SETTINGS);
        context.startActivity(intent);
    }

    public static void navigateToSearch(Activity context) {
        final Intent intent = new Intent(context, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setAction(Constants.NAVIGATE_SEARCH);
        context.startActivity(intent);
    }


    @TargetApi(21)
    public static void navigateToPlaylistDetail(Activity context, String action, long firstAlbumID, String playlistName, int foregroundcolor, long playlistID, ArrayList<Pair> transitionViews) {
        final Intent intent = new Intent(context, PlaylistDetailActivity.class);
        intent.setAction(action);
        intent.putExtra(Constants.PLAYLIST_ID, playlistID);
        intent.putExtra(Constants.PLAYLIST_FOREGROUND_COLOR, foregroundcolor);
        intent.putExtra(Constants.ALBUM_ID, firstAlbumID);
        intent.putExtra(Constants.PLAYLIST_NAME, playlistName);
        intent.putExtra(Constants.ACTIVITY_TRANSITION, transitionViews != null);

        if (transitionViews != null && FantasyUtils.isLollipop()) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, transitionViews.get(0), transitionViews.get(1), transitionViews.get(2));
            context.startActivityForResult(intent, Constants.ACTION_DELETE_PLAYLIST, options.toBundle());
        } else {
            context.startActivityForResult(intent, Constants.ACTION_DELETE_PLAYLIST);
        }
    }

    public static void navigateToEqualizer(Activity context) {
        try {
            // The google MusicFX apps need to be started using startActivityForResult
            context.startActivityForResult(FantasyUtils.createEffectsIntent(), 666);
        } catch (final ActivityNotFoundException notFound) {
            Toast.makeText(context, "Equalizer not found", Toast.LENGTH_SHORT).show();
        }
    }

    public static Intent getNavigateToStyleSelectorIntent(Activity context, String what) {
        final Intent intent = new Intent(context, SettingsActivity.class);
        intent.setAction(Constants.SETTINGS_STYLE_SELECTOR);
        intent.putExtra(Constants.SETTINGS_STYLE_SELECTOR_WHAT, what);
        return intent;
    }

    public static Fragment getFragmentForNowplayingID(String fragmentID) {
        switch (fragmentID) {
            case Constants.FANTASY1:
                return new Fantasy1();
            case Constants.FANTASY2:
                return new Fantasy2();
            case Constants.FANTASY3:
                return new Fantasy3();
            case Constants.FANTASY4:
                return new Fantasy4();
            case Constants.FANTASY5:
                return new Fantasy5();
            case Constants.FANTASY6:
                return new Fantasy6();
            default:
                return new Fantasy1();
        }

    }

    public static int getIntForCurrentNowplaying(String nowPlaying) {
        switch (nowPlaying) {
            case Constants.FANTASY1:
                return 0;
            case Constants.FANTASY2:
                return 1;
            case Constants.FANTASY3:
                return 2;
            case Constants.FANTASY4:
                return 3;
            case Constants.FANTASY5:
                return 4;
            case Constants.FANTASY6:
                return 5;
            default:
                return 2;
        }

    }

}

