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
import com.frankhon.fantasymusic.utils.showToast
import com.frankhon.fantasymusic.utils.transformToSimpleSong
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.view.SearchSongItem

/**
 * Created by Frank Hon on 2020-06-03 00:50.
 * E-mail: frank_hon@foxmail.com
 */
class SearchResultAdapter(
    appExecutors: AppExecutors,
    private val downloader: SongDownloader,
    private val onItemClickListener: (song: SimpleSong) -> Unit
) :
    ListAdapter<SearchSongItem, SearchResultAdapter.SearchResultViewHolder>(
        AsyncDifferConfig.Builder(
            object : DiffUtil.ItemCallback<SearchSongItem>() {
                override fun areContentsTheSame(
                    oldItem: SearchSongItem,
                    newItem: SearchSongItem
                ): Boolean {
                    return oldItem.songUri == newItem.songUri
                }

                override fun areItemsTheSame(
                    oldItem: SearchSongItem,
                    newItem: SearchSongItem
                ): Boolean {
                    return oldItem.name == newItem.name && oldItem.artist == newItem.artist
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
            song: SearchSongItem,
            downloader: SongDownloader,
            onItemClickListener: (song: SimpleSong) -> Unit
        ) {
            song.run {
                Glide.with(itemView)
                    .load(albumPicUrl)
                    .into(songPic)
                songName.text = name
                artistName.text = artist
                songItem.setOnClickListener {
                    onItemClickListener(
                        transformToSimpleSong()
                    )
                }
                when (downloadState) {
                    0 -> {
                        downloadButton.isEnabled = true
                        downloadButton.setImageResource(R.drawable.ic_download_song)
                    }
                    1 -> {
                        downloadButton.isEnabled = false
                        downloadButton.setImageResource(R.drawable.ic_download_song_disable)
                    }
                    2 -> {
                        downloadButton.isEnabled = true
                        downloadButton.setImageResource(R.drawable.ic_download_complete)
                    }
                }
                downloadButton.setOnClickListener {
                    when (downloadState) {
                        0 -> {
                            if (songUri.isNullOrEmpty()) {
                                showToast(R.string.song_url_error)
                            } else {
                                showToast(R.string.downloading)
                                downloader.startDownload(song.transformToSimpleSong())
                                downloadButton.setImageResource(R.drawable.ic_download_song_disable)
                                downloadState = 1
                            }
                        }
                        2 -> showToast(R.string.song_downloaded)
                    }
                }
            }
        }
    }
}