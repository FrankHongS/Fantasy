package com.frankhon.fantasymusic.vo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Frank Hon on 2020-06-01 23:47.
 * E-mail: frank_hon@foxmail.com
 */
public class Song {

    public String name;

    public String url;

    public Album album;

    public List<Artist> artists;

    public String pic;

    public String bgPic;

    public class Album{
        public String name;

        public String picUrl;

        @Override
        public String toString() {
            return "Album{" +
                    "name='" + name + '\'' +
                    ", picUrl='" + picUrl + '\'' +
                    '}';
        }
    }

    public class Artist{
        @SerializedName("name")
        public String name;

        @Override
        public String toString() {
            return "Artist{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Song{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", album=" + album +
                ", artists=" + artists +
                ", pic='" + pic + '\'' +
                ", bgPic='" + bgPic + '\'' +
                '}';
    }
}
