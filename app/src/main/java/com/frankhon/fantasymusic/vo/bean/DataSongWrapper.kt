package com.frankhon.fantasymusic.vo.bean

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by Frank Hon on 2020-06-10 10:11.
 * E-mail: frank_hon@foxmail.com
 */
data class DataSongWrapper(val result: Int, val data: DataSongInner) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable(DataSongInner::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(result)
        parcel.writeParcelable(data, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataSongWrapper> {
        override fun createFromParcel(parcel: Parcel): DataSongWrapper {
            return DataSongWrapper(parcel)
        }

        override fun newArray(size: Int): Array<DataSongWrapper?> {
            return arrayOfNulls(size)
        }
    }

}

data class DataSongInner(
    @SerializedName("list")
    var songs: List<DataSong>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(DataSong) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(songs)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataSongInner> {
        override fun createFromParcel(parcel: Parcel): DataSongInner {
            return DataSongInner(parcel)
        }

        override fun newArray(size: Int): Array<DataSongInner?> {
            return arrayOfNulls(size)
        }
    }
}