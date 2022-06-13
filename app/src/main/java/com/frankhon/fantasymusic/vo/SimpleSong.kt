package com.frankhon.fantasymusic.vo

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 */
data class SimpleSong(
    val name: String?,
    val artist: String?,
    val location: String?,
    val songPic: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(location)
        parcel.writeString(songPic)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SimpleSong> {
        override fun createFromParcel(parcel: Parcel): SimpleSong {
            return SimpleSong(parcel)
        }

        override fun newArray(size: Int): Array<SimpleSong?> {
            return arrayOfNulls(size)
        }
    }
}