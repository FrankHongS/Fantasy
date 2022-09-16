package com.frankhon.fantasymusic.vo

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 */
class SimpleSong(
    var name: String?,
    var artist: String?,
    var location: String?,
    var songPic: String? = "",
    // unit, ms
    var duration: Long = 0L
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong()
    )

    companion object CREATOR : Parcelable.Creator<SimpleSong> {
        override fun createFromParcel(parcel: Parcel): SimpleSong {
            return SimpleSong(parcel)
        }

        override fun newArray(size: Int): Array<SimpleSong?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(location)
        parcel.writeString(songPic)
        parcel.writeLong(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "SimpleSong(name=$name)"
    }

    /**
     * 歌曲名字和歌手名字同时相等，两个对象才相等
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleSong

        if (name != other.name) return false
        if (artist != other.artist) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (artist?.hashCode() ?: 0)
        return result
    }

}