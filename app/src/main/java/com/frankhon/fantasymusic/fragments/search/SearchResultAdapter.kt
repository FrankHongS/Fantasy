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
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.application.AppExecutors
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.bean.DataSong

/**
 * Created by Frank Hon on 2020-06-03 00:50.
 * E-mail: frank_hon@foxmail.com
 */
class SearchResultAdapter(
    appExecutors: AppExecutors,
    private val onItemClickListener: (song: SimpleSong) -> Unit
) :
    ListAdapter<DataSong, SearchResultAdapter.SearchResultViewHolder>(
        AsyncDifferConfig.Builder(
            object : DiffUtil.ItemCallback<DataSong>() {
                override fun areContentsTheSame(oldItem: DataSong, newItem: DataSong): Boolean {
                    val oldArtists = oldItem.artists ?: emptyList()
                    val newArtists = newItem.artists ?: emptyList()
                    if (oldArtists.isNotEmpty() && newArtists.isNotEmpty()) {
                        return oldItem.pic == newItem.pic && oldArtists[0].name == newArtists[0].name
                    } else {
                        return oldItem.pic == newItem.pic
                    }
                }

                override fun areItemsTheSame(oldItem: DataSong, newItem: DataSong): Boolean {
                    return oldItem.url == newItem.url
                }
            }
        )
            .setBackgroundThreadExecutor(appExecutors.diskIO)
            .build()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_song_list, parent, false)
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

        fun bindView(song: DataSong, onItemClickListener: (song: SimpleSong) -> Unit) {
            Glide.with(itemView)
                .load(song.album?.picUrl)
                .into(songPic)
            songName.text = song.name
            artistName.text = song.artists?.get(0)?.name.orEmpty()
            songItem.setOnClickListener {
                onItemClickListener(
                    SimpleSong(
                        song.name,
                        song.artists?.get(0)?.name,
                        song.url,
                        song.album?.picUrl.orEmpty()
                    )
                )
            }
        }
    }
}