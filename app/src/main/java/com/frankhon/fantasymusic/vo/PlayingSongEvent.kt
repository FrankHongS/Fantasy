package com.frankhon.fantasymusic.vo

import android.os.Parcel
import android.os.Parcelable
import com.frankhon.fantasymusic.media.PlayerState

/**
 * Created by Frank Hon on 2020-06-05 01:24.
 * E-mail: frank_hon@foxmail.com
 */
data class PlayingSongEvent(
    val song: SimpleSong?,
    val playerState: PlayerState
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(SimpleSong::class.java.classLoader),
        PlayerState.valueOf(parcel.readString().orEmpty())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(song, flags)
        parcel.writeString(playerState.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayingSongEvent> {
        override fun createFromParcel(parcel: Parcel): PlayingSongEvent {
            return PlayingSongEvent(parcel)
        }

        override fun newArray(size: Int): Array<PlayingSongEvent?> {
            return arrayOfNulls(size)
        }
    }

}