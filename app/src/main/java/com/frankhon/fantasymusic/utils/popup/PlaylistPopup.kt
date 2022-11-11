package com.frankhon.fantasymusic.utils.popup

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.view.isVisible
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.vo.view.SongItem

/**
 * Created by Frank Hon on 2022/9/13 5:50 下午.
 * E-mail: frank_hon@foxmail.com
 */

/**
 * 显示播放列表
 *
 * @receiver anchorView
 */
fun View.showPlaylistPopup(
    playlist: List<SongItem>,
    hOffset: Int,
    removeListener: (Int) -> Unit,
    onItemClickListener: (Int) -> Unit
) {
    showPlaylistPopup(
        this,
        PopupAdapter(
            playlist.toMutableList(),
            removeListener,
            onItemClickListener
        ),
        hOffset
    )
}

/**
 * @receiver anchorView
 */
fun View.dismissPlaylistPopup() {
    val popupWindow = getTag(R.id.key_playlist_popup_window) as? ListPopupWindow
    popupWindow?.dismiss()
}

/**
 * 更新播放列表
 * @param index 当前正在播放歌曲位置
 * @param newPlaylist 当前播放列表
 */
fun View.updatePlaylistPopup(index: Int = -1, newPlaylist: List<SongItem>? = null) {
    val adapter = getTag(R.id.key_playlist_popup_window_adapter) as? PopupAdapter
    adapter?.run {
        if (index != -1) {
            for (i in 0 until count) {
                getItem(i).isPlaying = i == index
            }
            notifyDataSetChanged()
        } else {
            newPlaylist.takeIf { !it.isNullOrEmpty() }?.let {
                playlist.setData(it)
                notifyDataSetChanged()
            } ?: kotlin.run {
                dismissPlaylistPopup()
                stopAudio()
            }
        }
    }
}

private fun showPlaylistPopup(
    view: View,
    adapter: BaseAdapter,
    hOffset: Int
) {
    ListPopupWindow(view.context)
        .run {
            anchorView = view
            view.setTag(R.id.key_playlist_popup_window, this)
            view.setTag(R.id.key_playlist_popup_window_adapter, adapter)
            setAdapter(adapter)
            setDropDownGravity(Gravity.START)
            width = 260.dp
            height = 400.dp
            horizontalOffset = hOffset
            isModal = true
            setBackgroundDrawable(view.drawable(R.drawable.bg_playlist))
            setOnDismissListener {
                view.setTag(R.id.key_playlist_popup_window, null)
                view.setTag(R.id.key_playlist_popup_window_adapter, null)
            }
            show()
        }
}

private class PopupAdapter(
    val playlist: MutableList<SongItem>,
    private val removeListener: (Int) -> Unit,
    private val onItemClickListener: (Int) -> Unit
) : BaseAdapter() {
    override fun getCount(): Int = playlist.size

    override fun getItem(position: Int): SongItem = playlist[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_popup_playlist, parent, false)
        if (view.tag == null) {
            view.tag = PlaylistPopupViewHolder(view, removeListener, onItemClickListener)
        }
        val viewHolder = view.tag as PlaylistPopupViewHolder
        viewHolder.bindView(getItem(position), position)
        return view
    }
}

private class PlaylistPopupViewHolder(
    private val itemView: View,
    private val removeListener: (Int) -> Unit,
    private val onItemClickListener: (Int) -> Unit
) {

    private val songName = itemView.findViewById<TextView>(R.id.tv_song_name_playlist)
    private val artistName = itemView.findViewById<TextView>(R.id.tv_artist_name_playlist)
    private val nowPlaying = itemView.findViewById<ImageView>(R.id.iv_song_now_playing_playlist)
    private val removeButton = itemView.findViewById<ImageButton>(R.id.ib_remove_song_playlist)

    fun bindView(song: SongItem, position: Int) {
        songName.text = song.name
        artistName.text = String.format(string(R.string.playlist_artist_name), song.artist)
        nowPlaying.isVisible = song.isPlaying
        removeButton.setOnClickListener {
            removeListener(position)
        }
        itemView.setOnClickListener {
            onItemClickListener(position)
        }
    }

}