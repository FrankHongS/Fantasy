package com.frankhon.fantasymusic.ui.fragments.song

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.setData
import com.frankhon.fantasymusic.vo.view.SongItem

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 */
class SongAdapter(
    private val onMoreClickListener: (View, Int) -> Unit,
    private val onItemClickListener: (song: SongItem, index: Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private val songs = mutableListOf<SongItem>()
    private var curPlayingIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_song_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bindView(song, position, onMoreClickListener, onItemClickListener)
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
        if (curPlayingIndex == playingIndex || playingIndex < 0 || playingIndex >= songs.size) {
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

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val songItem = itemView.findViewById<CardView>(R.id.cv_song)
        private val songPicView = itemView.findViewById<ImageView>(R.id.iv_song_pic)
        private val songName = itemView.findViewById<TextView>(R.id.tv_song_name)
        private val artistName = itemView.findViewById<TextView>(R.id.tv_artist_name)
        private val nowPlayingImage = itemView.findViewById<View>(R.id.iv_song_now_playing)
        private val songIndex = itemView.findViewById<TextView>(R.id.tv_song_index)
        private val moreButton = itemView.findViewById<ImageButton>(R.id.ib_song_list_more)

        @SuppressLint("SetTextI18n")
        fun bindView(
            song: SongItem,
            index: Int,
            onMoreClickListener: (View, Int) -> Unit,
            onItemClickListener: (SongItem, Int) -> Unit
        ) {
            song.run {
                if (TextUtils.isEmpty(songPic)) {
                    songPicView.setImageResource(R.mipmap.ic_launcher)
                } else {
                    Glide.with(itemView)
                        .load(songPic)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher))
                        .into(songPicView)
                }
                songName.text = name
                artistName.text = artist
                if (isPlaying) {
                    nowPlayingImage.isVisible = true
                    songIndex.isVisible = false
                } else {
                    nowPlayingImage.isVisible = false
                    songIndex.isVisible = true
                    songIndex.text = "${index + 1}"
                }
                songItem.setOnClickListener {
                    onItemClickListener(song, index)
                }
                moreButton.setOnClickListener {
                    onMoreClickListener(it, index)
                }
            }
        }

    }
}