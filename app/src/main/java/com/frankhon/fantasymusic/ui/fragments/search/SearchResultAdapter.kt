package com.frankhon.fantasymusic.ui.fragments.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
    private val downloader: SongDownloader,
    private val onItemClickListener: (song: SimpleSong) -> Unit
) :
    ListAdapter<DataSong, SearchResultAdapter.SearchResultViewHolder>(
        AsyncDifferConfig.Builder(
            object : DiffUtil.ItemCallback<DataSong>() {
                override fun areContentsTheSame(oldItem: DataSong, newItem: DataSong): Boolean {
                    val oldArtists = oldItem.artists ?: emptyList()
                    val newArtists = newItem.artists ?: emptyList()
                    if (oldArtists.isNotEmpty() && newArtists.isNotEmpty()) {
                        return oldItem.name == newItem.name && oldArtists[0].name == newArtists[0].name
                    } else {
                        return oldItem.name == newItem.name
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
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_searched_song_list, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val song = getItem(position)
        holder.bindView(song, downloader, onItemClickListener)
    }

    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songItem = itemView.findViewById<CardView>(R.id.cv_searched_song)
        private val songPic = itemView.findViewById<ImageView>(R.id.iv_searched_song_pic)
        private val songName = itemView.findViewById<TextView>(R.id.tv_searched_song_name)
        private val artistName = itemView.findViewById<TextView>(R.id.tv_searched_artist_name)
        private val downloadButton = itemView.findViewById<ImageButton>(R.id.iv_download_song)

        fun bindView(
            song: DataSong,
            downloader: SongDownloader,
            onItemClickListener: (song: SimpleSong) -> Unit
        ) {
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
            if (song.url?.startsWith("file://") == true) {
                downloadButton.setImageResource(R.drawable.ic_download_complete)
            } else {
                downloadButton.setImageResource(R.drawable.ic_download_song)
            }
            downloadButton.setOnClickListener {
                downloader.startDownload(song)
            }
        }
    }
}