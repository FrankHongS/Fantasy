package com.frankhon.fantasymusic.vo.view

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Frank Hon on 2022/8/29 7:14 下午.
 * E-mail: frank_hon@foxmail.com
 */
data class SearchSongItem(
    val name: String?,
    val artist: String?,
    val albumPicUrl: String? = "",
    var songUri: String? = "",
    // 0 未下载，1 下载中，2 已下载
    var downloadState: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    )

    fun clone(): SearchSongItem {
        return SearchSongItem(name, artist, albumPicUrl, songUri, downloadState)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchSongItem

        if (name != other.name) return false
        if (artist != other.artist) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (artist?.hashCode() ?: 0)
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(albumPicUrl)
        parcel.writeString(songUri)
        parcel.writeInt(downloadState)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchSongItem> {
        override fun createFromParcel(parcel: Parcel): SearchSongItem {
            return SearchSongItem(parcel)
        }

        override fun newArray(size: Int): Array<SearchSongItem?> {
            return arrayOfNulls(size)
        }
    }
}
