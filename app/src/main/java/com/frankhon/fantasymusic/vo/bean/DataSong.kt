package com.frankhon.fantasymusic.vo.bean

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Frank Hon on 2020-06-01 23:47.
 * E-mail: frank_hon@foxmail.com
 */
class DataSong() : Parcelable {
    var name: String? = null
    var url: String? = null
    var album: Album? = null
    var artists: List<Artist>? = null
    var pic: String? = null
    var bgPic: String? = null

    val artist: String
        get() {
            return if (artists.isNullOrEmpty()) {
                ""
            } else {
                artists?.first()?.name.orEmpty()
            }
        }

    val albumPicUrl: String
        get() {
            return album?.picUrl.orEmpty()
        }

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        url = parcel.readString()
        album = parcel.readParcelable(Album::class.java.classLoader)
        artists = parcel.createTypedArrayList(Artist)
        pic = parcel.readString()
        bgPic = parcel.readString()
    }

    class Album() : Parcelable {
        var name: String? = null
        var picUrl: String? = null

        constructor(parcel: Parcel) : this() {
            name = parcel.readString()
            picUrl = parcel.readString()
        }

        override fun toString(): String {
            return "Album{" +
                    "name='" + name + '\'' +
                    ", picUrl='" + picUrl + '\'' +
                    '}'
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(picUrl)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Album> {
            override fun createFromParcel(parcel: Parcel): Album {
                return Album(parcel)
            }

            override fun newArray(size: Int): Array<Album?> {
                return arrayOfNulls(size)
            }
        }
    }

    class Artist() : Parcelable {
        @SerializedName("name")
        var name: String? = null

        constructor(parcel: Parcel) : this() {
            name = parcel.readString()
        }

        override fun toString(): String {
            return "Artist{" +
                    "name='" + name + '\'' +
                    '}'
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Artist> {
            override fun createFromParcel(parcel: Parcel): Artist {
                return Artist(parcel)
            }

            override fun newArray(size: Int): Array<Artist?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun toString(): String {
        return "Song{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", album=" + album +
                ", artists=" + artists +
                ", pic='" + pic + '\'' +
                ", bgPic='" + bgPic + '\'' +
                '}'
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(url)
        parcel.writeParcelable(album, flags)
        parcel.writeTypedList(artists)
        parcel.writeString(pic)
        parcel.writeString(bgPic)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataSong> {
        override fun createFromParcel(parcel: Parcel): DataSong {
            return DataSong(parcel)
        }

        override fun newArray(size: Int): Array<DataSong?> {
            return arrayOfNulls(size)
        }
    }
}