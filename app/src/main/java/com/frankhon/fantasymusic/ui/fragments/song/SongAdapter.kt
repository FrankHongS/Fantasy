package com.frankhon.fantasymusic.ui.fragments.song

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.ui.base.BaseViewHolder
import com.frankhon.fantasymusic.utils.safeSetText
import com.frankhon.fantasymusic.utils.setData
import com.frankhon.fantasymusic.utils.string
import com.frankhon.fantasymusic.vo.view.SongItem

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 */
class SongAdapter(
    private val onPlayAllClickListener: (View) -> Unit,
    private val onMoreClickListener: (View, Int) -> Unit,
    private val onItemClickListener: (song: SongItem, index: Int) -> Unit
) : RecyclerView.Adapter<BaseViewHolder<SongItem>>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_NORMAL = 1
    }

    private val songs = mutableListOf<SongItem>()
    private var curPlayingIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<SongItem> {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_songs_header, parent, false),
                onPlayAllClickListener
            )
            else -> SongViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_song_list, parent, false),
                onMoreClickListener,
                onItemClickListener
            )
        }
    }

    override fun getItemCount(): Int {
        return songs.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            else -> TYPE_NORMAL
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<SongItem>, position: Int) {
        if (position == 0) {
            (holder as? HeaderViewHolder)?.bindView(songs.size)
        } else {
            holder.bindView(position, songs[position - 1])
        }
    }

    fun setData(items: List<SongItem>, playingIndex: Int) {
        if (items == songs) {
            return
        }
        curPlayingIndex = playingIndex
        songs.setData(items)
        notifyDataChangedWithSelected(playingIndex)
    }

    fun select(playingIndex: Int) {
        if (curPlayingIndex == playingIndex) {
            return
        }
        curPlayingIndex = playingIndex
        notifyDataChangedWithSelected(playingIndex)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(index: Int) {
        songs.removeAt(index)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDataChangedWithSelected(playingIndex: Int) {
        songs.mapIndexed { index, songItem ->
            songItem.also {
                it.isPlaying = index == playingIndex
            }
        }
        notifyDataSetChanged()
    }

    private class SongViewHolder(
        view: View,
        private val onMoreClickListener: (View, Int) -> Unit,
        private val onItemClickListener: (SongItem, Int) -> Unit
    ) : BaseViewHolder<SongItem>(view) {

        private val songPicView = view.findViewById<ImageView>(R.id.iv_song_pic)
        private val songName = view.findViewById<TextView>(R.id.tv_song_name)
        private val artistName = view.findViewById<TextView>(R.id.tv_artist_name)
        private val nowPlayingImage = view.findViewById<View>(R.id.iv_song_now_playing)
        private val songIndex = view.findViewById<TextView>(R.id.tv_song_index)
        private val moreButton = view.findViewById<ImageButton>(R.id.ib_song_list_more)

        override fun bindView(index: Int, item: SongItem) {
            item.run {
                Glide.with(itemView)
                    .load(songPic)
                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher))
                    .into(songPicView)
                songName.text = name
                artistName.text = artist
                if (isPlaying) {
                    nowPlayingImage.isVisible = true
                    songIndex.isVisible = false
                } else {
                    nowPlayingImage.isVisible = false
                    songIndex.safeSetText("$index")
                }
                itemView.setOnClickListener {
                    onItemClickListener(item, index - 1)
                }
                moreButton.setOnClickListener {
                    onMoreClickListener(it, index - 1)
                }
            }
        }

    }

    private class HeaderViewHolder(
        view: View,
        private val onPlayAllClickListener: (View) -> Unit
    ) : BaseViewHolder<SongItem>(view) {

        private val songsCountText = view.findViewById<TextView>(R.id.tv_songs_count)
        private val playAllBtn = view.findViewById<TextView>(R.id.tv_play_all)

        fun bindView(count: Int) {
            songsCountText.text = String.format(string(R.string.songs_count), count)
            playAllBtn.setOnClickListener {
                onPlayAllClickListener(it)
            }
        }

    }
}