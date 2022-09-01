package com.frankhon.fantasymusic.vo

import android.os.Parcel
import android.os.Parcelable
import com.frankhon.fantasymusic.media.PlayerState

/**
 * Created by Frank Hon on 2022/8/31 12:14 下午.
 * E-mail: frank_hon@foxmail.com
 *
 * 当前播放器信息
 */
class CurrentPlayerInfo() : Parcelable {

    var curSong: SimpleSong? = null

    var curPlayList: List<SimpleSong> = emptyList()

    var curSongIndex: Int = -1

    var curPlaybackPosition: Long = 0L

    var curPlayerState = PlayerState.IDLE

    constructor(parcel: Parcel) : this() {
        curSong = parcel.readParcelable(SimpleSong::class.java.classLoader)
        curPlayList = parcel.createTypedArrayList(SimpleSong) ?: emptyList()
        curSongIndex = parcel.readInt()
        curPlaybackPosition = parcel.readLong()
        curPlayerState = PlayerState.valueOf(parcel.readString() ?: PlayerState.IDLE.name)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(curSong, flags)
        parcel.writeTypedList(curPlayList)
        parcel.writeInt(curSongIndex)
        parcel.writeLong(curPlaybackPosition)
        parcel.writeString(curPlayerState.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CurrentPlayerInfo> {
        override fun createFromParcel(parcel: Parcel): CurrentPlayerInfo {
            return CurrentPlayerInfo(parcel)
        }

        override fun newArray(size: Int): Array<CurrentPlayerInfo?> {
            return arrayOfNulls(size)
        }
    }


}