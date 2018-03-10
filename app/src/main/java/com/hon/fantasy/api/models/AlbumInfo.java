package com.hon.fantasy.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class AlbumInfo {

    private static final String ALBUM = "album";

    @SerializedName(ALBUM)
    public LastfmAlbum mAlbum;
}
