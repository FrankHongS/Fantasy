package com.frankhon.fantasymusic.fragments.song

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.vo.SimpleSong

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 */
class SongAdapter(
    private val songs: List<SimpleSong>,
    private val onItemClickListener: (song: SimpleSong, index: Int) -> Unit
) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bindView(song, position, onItemClickListener)
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val songItem = itemView.findViewById<CardView>(R.id.cv_song)
        private val songPicView = itemView.findViewById<ImageView>(R.id.iv_song_pic)
        private val songName = itemView.findViewById<TextView>(R.id.tv_song_name)
        private val artistName = itemView.findViewById<TextView>(R.id.tv_artist_name)

        fun bindView(
            song: SimpleSong,
            index: Int,
            onItemClickListener: (song: SimpleSong, index: Int) -> Unit
        ) {
            val songPic = song.songPic
            if (TextUtils.isEmpty(songPic)) {
                songPicView.setImageResource(R.mipmap.ic_launcher)
            } else {
                Glide.with(itemView)
                    .load(songPic)
                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher))
                    .into(songPicView)
            }
            songName.text = song.name
            artistName.text = song.artist
            songItem.setOnClickListener {
                onItemClickListener(song, index)
            }
        }

    }
}