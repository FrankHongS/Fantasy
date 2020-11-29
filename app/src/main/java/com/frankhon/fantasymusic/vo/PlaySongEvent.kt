package com.frankhon.fantasymusic.vo

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Frank Hon on 2020-06-05 01:24.
 * E-mail: frank_hon@foxmail.com
 */
data class PlaySongEvent(
    val isPlaying: Boolean = false,
    val isResumed: Boolean = false,
    val picUrl: String? = "",
    val songName: String? = "",
    val artistName: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isPlaying) 1 else 0)
        parcel.writeString(picUrl)
        parcel.writeString(songName)
        parcel.writeString(artistName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaySongEvent> {
        override fun createFromParcel(parcel: Parcel): PlaySongEvent {
            return PlaySongEvent(parcel)
        }

        override fun newArray(size: Int): Array<PlaySongEvent?> {
            return arrayOfNulls(size)
        }
    }
}