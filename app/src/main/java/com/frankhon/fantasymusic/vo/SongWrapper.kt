package com.frankhon.fantasymusic.vo

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Frank Hon on 2020-06-10 10:11.
 * E-mail: frank_hon@foxmail.com
 */
data class SongWrapper(val result:Int, val data: Song) :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readSerializable() as Song
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(result)
        parcel.writeSerializable(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongWrapper> {
        override fun createFromParcel(parcel: Parcel): SongWrapper {
            return SongWrapper(parcel)
        }

        override fun newArray(size: Int): Array<SongWrapper?> {
            return arrayOfNulls(size)
        }
    }
}