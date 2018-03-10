package com.hon.fantasy.subfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hon.fantasy.R;
import com.hon.fantasy.utils.Constants;
import com.hon.fantasy.utils.NavigationUtils;
import com.hon.fantasy.widget.MultiViewPager;

/**
 * Created by Frank on 2018/3/5.
 * E-mail:frank_hon@foxmail.com
 */

public class StyleSelectorFragment extends Fragment {

    public String ACTION = "action";
    private FragmentStatePagerAdapter adapter;
    private MultiViewPager pager;
    private SubStyleSelectorFragment selectorFragment;
    private SharedPreferences preferences;

    public static StyleSelectorFragment newInstance(String what) {
        StyleSelectorFragment fragment = new StyleSelectorFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SETTINGS_STYLE_SELECTOR_WHAT, what);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ACTION = getArguments().getString(Constants.SETTINGS_STYLE_SELECTOR_WHAT);
        }
        preferences = getActivity().getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_style_selector, container, false);

        if (ACTION.equals(Constants.SETTINGS_STYLE_SELECTOR_NOWPLAYING)) {

        }
        pager = (MultiViewPager) rootView.findViewById(R.id.pager);

        adapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public int getCount() {
                return 6;
            }

            @Override
            public Fragment getItem(int position) {
                selectorFragment = SubStyleSelectorFragment.newInstance(position, ACTION);
                return selectorFragment;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };
        pager.setAdapter(adapter);
        scrollToCurrentStyle();

        return rootView;
    }

    public void updateCurrentStyle() {
        if (selectorFragment != null) {
            adapter.notifyDataSetChanged();
            scrollToCurrentStyle();
        }

    }

    public void scrollToCurrentStyle() {
        String fragmentID = preferences.getString(Constants.NOWPLAYING_FRAGMENT_ID, Constants.FANTASY3);
        pager.setCurrentItem(NavigationUtils.getIntForCurrentNowplaying(fragmentID));
    }
}

