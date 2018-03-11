package com.hon.fantasy.subfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hon.fantasy.R;
import com.hon.fantasy.utils.Constants;
import com.hon.fantasy.utils.NavigationUtils;
import com.hon.fantasy.utils.PreferencesUtility;

/**
 * Created by Frank on 2018/3/5.
 * E-mail:frank_hon@foxmail.com
 */

public class SubStyleSelectorFragment extends Fragment {

    private static final String ARG_PAGE_NUMBER = "pageNumber";
    private static final String WHAT = "what";
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private LinearLayout currentStyle;
    private View foreground;
    private ImageView styleImage, imgLock;

    public static SubStyleSelectorFragment newInstance(int pageNumber, String what) {
        SubStyleSelectorFragment fragment = new SubStyleSelectorFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE_NUMBER, pageNumber);
        bundle.putString(WHAT, what);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_style_selector_pager, container, false);

        TextView styleName = (TextView) rootView.findViewById(R.id.style_name);
        styleName.setText(String.valueOf(getArguments().getInt(ARG_PAGE_NUMBER) + 1));
        preferences = getActivity().getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE);

        styleImage = (ImageView) rootView.findViewById(R.id.style_image);
        imgLock = (ImageView) rootView.findViewById(R.id.img_lock);

        styleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getArguments().getInt(ARG_PAGE_NUMBER) >= 4) {
                    if (isUnlocked()) {
                        setPreferences();
                    } else {
                        showPurchaseDialog();
                    }
                } else
                    setPreferences();
            }
        });

        switch (getArguments().getInt(ARG_PAGE_NUMBER)) {
            case 0:
                styleImage.setImageResource(R.drawable.fantasy_1_nowplaying_x);
                break;
            case 1:
                styleImage.setImageResource(R.drawable.fantasy_2_nowplaying_x);
                break;
            case 2:
                styleImage.setImageResource(R.drawable.fantasy_3_nowplaying_x);
                break;
            case 3:
                styleImage.setImageResource(R.drawable.fantasy_4_nowplaying_x);
                break;
            case 4:
                styleImage.setImageResource(R.drawable.fantasy_5_nowplaying_x);
                break;
            case 5:
                styleImage.setImageResource(R.drawable.fantasy_6_nowplaying_x);
                break;
        }

        currentStyle = (LinearLayout) rootView.findViewById(R.id.currentStyle);
        foreground = rootView.findViewById(R.id.foreground);

        setCurrentStyle();

        return rootView;
    }

    private boolean isUnlocked() {
        return getActivity() != null && PreferencesUtility.getInstance(getActivity()).fullUnlocked();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLockedStatus();
    }

    private void updateLockedStatus() {
        if (getArguments().getInt(ARG_PAGE_NUMBER) >= 4 && !isUnlocked()) {
            imgLock.setVisibility(View.VISIBLE);
            foreground.setVisibility(View.VISIBLE);
        }
        else {
            imgLock.setVisibility(View.GONE);
            foreground.setVisibility(View.GONE);
        }
    }
    private void showPurchaseDialog() {
        new MaterialDialog.Builder(getActivity())
                .title("Purchase")
                .content("This now playing style is available after a one time purchase of any amount. Support development and unlock this style?")
                .positiveText("Support development")
                .neutralText("Restore purchases")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Toast.makeText(getActivity(), "thx :)", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }).onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Toast.makeText(getActivity(), "restore :)", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void setCurrentStyle() {
        String fragmentID = preferences.getString(Constants.NOWPLAYING_FRAGMENT_ID, Constants.FANTASY3);

        if (getArguments().getInt(ARG_PAGE_NUMBER) == NavigationUtils.getIntForCurrentNowplaying(fragmentID)) {
            currentStyle.setVisibility(View.VISIBLE);
            foreground.setVisibility(View.VISIBLE);
        } else {
            currentStyle.setVisibility(View.GONE);
            foreground.setVisibility(View.GONE);
        }

    }

    private void setPreferences() {

        if (getArguments().getString(WHAT).equals(Constants.SETTINGS_STYLE_SELECTOR_NOWPLAYING)) {
            editor = getActivity().getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE).edit();
            editor.putString(Constants.NOWPLAYING_FRAGMENT_ID, getStyleForPageNumber());
            editor.apply();
            if (getActivity() != null)
                PreferencesUtility.getInstance(getActivity()).setNowPlayingThemeChanged(true);
            setCurrentStyle();
            ((StyleSelectorFragment) getParentFragment()).updateCurrentStyle();
        }
    }

    private String getStyleForPageNumber() {
        switch (getArguments().getInt(ARG_PAGE_NUMBER)) {
            case 0:
                return Constants.FANTASY1;
            case 1:
                return Constants.FANTASY2;
            case 2:
                return Constants.FANTASY3;
            case 3:
                return Constants.FANTASY4;
            case 4:
                return Constants.FANTASY5;
            case 5:
                return Constants.FANTASY6;
            default:
                return Constants.FANTASY3;
        }
    }

}

