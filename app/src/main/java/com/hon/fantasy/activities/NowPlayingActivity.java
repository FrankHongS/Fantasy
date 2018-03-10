package com.hon.fantasy.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.appthemeengine.customizers.ATEStatusBarCustomizer;
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer;
import com.hon.fantasy.R;
import com.hon.fantasy.utils.Constants;
import com.hon.fantasy.utils.NavigationUtils;
import com.hon.fantasy.utils.PreferencesUtility;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class NowPlayingActivity extends BaseActivity implements ATEActivityThemeCustomizer, ATEToolbarCustomizer, ATEStatusBarCustomizer {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nowplaying);
        SharedPreferences prefs = getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE);
        String fragmentID = prefs.getString(Constants.NOWPLAYING_FRAGMENT_ID, Constants.FANTASY3);

        Fragment fragment = NavigationUtils.getFragmentForNowplayingID(fragmentID);
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ? R.style.AppTheme_FullScreen_Dark : R.style.AppTheme_FullScreen_Light;
    }

    @Override
    public int getLightToolbarMode() {
        return Config.LIGHT_TOOLBAR_AUTO;
    }

    @Override
    public int getLightStatusBarMode() {
        return Config.LIGHT_STATUS_BAR_OFF;
    }

    @Override
    public int getToolbarColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public int getStatusBarColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PreferencesUtility.getInstance(this).didNowplayingThemeChanged()) {
            PreferencesUtility.getInstance(this).setNowPlayingThemeChanged(false);
            recreate();
        }
    }
}

