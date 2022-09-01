package com.frankhon.fantasymusic.fragments.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.frankhon.fantasymusic.application.AppExecutors
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.Song

/**
 * Created by Frank Hon on 2020-06-03 00:50.
 * E-mail: frank_hon@foxmail.com
 */
class SearchResultAdapter(
    appExecutors: AppExecutors,
    private val onItemClickListener: (song: SimpleSong) -> Unit
) :
    ListAdapter<Song, SearchResultAdapter.SearchResultViewHolder>(
        AsyncDifferConfig.Builder(
            object : DiffUtil.ItemCallback<Song>() {
                override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                    if (oldItem.artists.size > 0 && newItem.artists.size > 0) {
                        return oldItem.pic == newItem.pic && oldItem.artists[0].name == newItem.artists[0].name
                    } else {
                        return oldItem.pic == newItem.pic
                    }
                }

                override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                    return oldItem.name == newItem.name
                }
            }
        )
            .setBackgroundThreadExecutor(appExecutors.diskIO)
            .build()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val song = getItem(position)
        holder.bindView(song, onItemClickListener)
    }

    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songItem = itemView.findViewById<CardView>(R.id.cv_song)
        private val songPic = itemView.findViewById<ImageView>(R.id.iv_song_pic)
        private val songName = itemView.findViewById<TextView>(R.id.tv_song_name)
        private val artistName = itemView.findViewById<TextView>(R.id.tv_artist_name)

        fun bindView(song: Song, onItemClickListener: (song: SimpleSong) -> Unit) {
            if (song.album != null) {
                Glide.with(itemView)
                    .load(song.album.picUrl)
                    .into(songPic)
                songName.text = song.name
                artistName.text = song.artists[0].name
            }
            songItem.setOnClickListener {
                onItemClickListener(
                    SimpleSong(
                        song.name,
                        song.artists[0].name,
                        song.url,
                        if (song.album != null) song.album.picUrl else ""
                    )
                )
            }
        }
    }
}