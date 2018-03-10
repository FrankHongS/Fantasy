package com.hon.fantasy.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class LastfmAlbum {
    private static final String IMAGE = "image";

    @SerializedName(IMAGE)
    public List<Artwork> mArtwork;

    // Only needed fields have been defined. See https://www.last.fm/api/show/album.getInfo
}
