package com.frankhon.fantasymusic.utils;

import com.frankhon.fantasymusic.Fantasy;

/**
 * Created by Frank_Hon on 1/6/2020.
 * E-mail: v-shhong@microsoft.com
 */
public final class Util {

    public static int dp2px(int dp) {
        float density = Fantasy.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

}
