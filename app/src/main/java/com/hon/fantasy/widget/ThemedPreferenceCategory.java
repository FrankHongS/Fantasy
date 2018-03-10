package com.hon.fantasy.widget;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.hon.fantasy.utils.Helpers;

/**
 * Created by Frank on 2018/3/6.
 * E-mail:frank_hon@foxmail.com
 */

public class ThemedPreferenceCategory extends PreferenceCategory {

    private Context context;

    public ThemedPreferenceCategory(Context context) {
        super(context);
        this.context = context;
    }

    public ThemedPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ThemedPreferenceCategory(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(Config.accentColor(context, Helpers.getATEKey(context)));
    }
}

