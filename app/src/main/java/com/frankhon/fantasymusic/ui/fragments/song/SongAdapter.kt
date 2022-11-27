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
import com.frankhon.customview.paging.PagingAdapter
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.ui.base.BaseViewHolder
import com.frankhon.fantasymusic.utils.compareTo
import com.frankhon.fantasymusic.utils.safeSetText
import com.frankhon.fantasymusic.utils.string
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.view.SongItem
import kotlin.math.max

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 */
class SongAdapter(
    pageLimit: Int,
    private val onPlayAllClickListener: (View) -> Unit,
    private val onMoreClickListener: (View, Int) -> Unit,
    private val onItemClickListener: (song: SongItem, index: Int) -> Unit
) : PagingAdapter<SongItem>(pageLimit) {

    companion object {
        private const val TYPE_HEADER = 4
    }

    private var curSong: SimpleSong? = null
    private var songsCount = 0

    override fun onCreateNormalViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<SongItem> {
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

    override fun onBindNormalViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            (holder as? HeaderViewHolder)?.bindView(songsCount)
        } else {
            (holder as? SongViewHolder)?.bindView(position, dataList[position - 1])
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getNormalItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            else -> super.getNormalItemViewType(position)
        }
    }

    fun setSongs(count: Int, items: List<SongItem>, song: SimpleSong?) {
        songsCount = count
        curSong = song
        setData(items.map { songItem ->
            songItem.also {
                it.isPlaying = songItem.compareTo(song)
            }
        }, false)
    }

    fun addSongs(items: List<SongItem>, song: SimpleSong?) {
        curSong = song
        addData(items.map { songItem ->
            songItem.also {
                it.isPlaying = songItem.compareTo(song)
            }
        })
        //如果加载更多时，item总数大于count，更新count，这种情况一般发生在下载新歌曲之后
        val count = max(dataList.size, songsCount)
        if (count != songsCount) {
            songsCount = count
            notifyItemChanged(0)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun select(song: SimpleSong?) {
        if (curSong == song) {
            return
        }
        curSong = song
        dataList.map { songItem ->
            songItem.also {
                it.isPlaying = songItem.compareTo(curSong)
            }
        }
        notifyItemRangeChanged(1, dataList.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteSong(index: Int) {
        songsCount--
        dataList.removeAt(index)
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
                    .placeholder(R.drawable.default_placeholder)
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