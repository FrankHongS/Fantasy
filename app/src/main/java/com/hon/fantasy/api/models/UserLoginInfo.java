package com.hon.fantasy.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class UserLoginInfo {
    private static final String SESSION = "session";

    @SerializedName(SESSION)
    public LastfmUserSession mSession;

}

