package com.frankhon.fantasymusic.vo

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Frank Hon on 2020-06-05 01:24.
 * E-mail: frank_hon@foxmail.com
 */
data class PlaySongEvent(
    val song: SimpleSong?,
    val isPlaying: Boolean = false,
    val isResumed: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(SimpleSong::class.java.classLoader),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(song, flags)
        parcel.writeByte(if (isPlaying) 1 else 0)
        parcel.writeByte(if (isResumed) 1 else 0)
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