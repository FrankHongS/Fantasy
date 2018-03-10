package com.hon.fantasy.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class ArtistQuery {
    private static final String ARTIST_NAME = "artist";

    @SerializedName(ARTIST_NAME)
    public String mArtist;

    public ArtistQuery(String artist) {
        this.mArtist = artist;
    }
}

