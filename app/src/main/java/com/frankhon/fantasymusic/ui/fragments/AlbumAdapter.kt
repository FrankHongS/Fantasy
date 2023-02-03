package com.frankhon.fantasymusic.ui.fragments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.ui.base.BaseViewHolder
import com.frankhon.fantasymusic.utils.getQuantityString
import com.frankhon.fantasymusic.utils.setData
import com.frankhon.fantasymusic.utils.string
import com.frankhon.fantasymusic.vo.view.AlbumItem

/**
 * Created by shuaihua_a on 2023/1/13 16:00.
 * E-mail: hongshuaihua
 */
class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.ArtistsViewHolder>() {

    private val albumsData = mutableListOf<AlbumItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistsViewHolder {
        val viewHolder = ArtistsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_album_list, parent, false)
        )
        return if (viewType == 0) {
            viewHolder.apply {
                coverImage.layoutParams =
                    (coverImage.layoutParams as ConstraintLayout.LayoutParams)
                        .apply { dimensionRatio = "1:1.3" }
            }
        } else {
            viewHolder
        }
    }

    override fun onBindViewHolder(holder: ArtistsViewHolder, position: Int) {
        holder.bindView(position, albumsData[position])
    }

    override fun getItemCount(): Int {
        return albumsData.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 3 == 0) {
            0
        } else {
            1
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(albums: List<AlbumItem>) {
        if (albums == albumsData) {
            return
        }
        albumsData.setData(albums)
        notifyDataSetChanged()
    }

    class ArtistsViewHolder(view: View) : BaseViewHolder<AlbumItem>(view) {

        val coverImage: ImageView = view.findViewById(R.id.iv_cover_album)
        private val albumNameText: TextView = view.findViewById(R.id.tv_name_album)
        private val artistNameText: TextView = view.findViewById(R.id.tv_artist_name_album)
        private val songsCountText: TextView = view.findViewById(R.id.tv_songs_count_album)

        override fun bindView(index: Int, item: AlbumItem) {
            item.run {
                Glide.with(itemView)
                    .load(albumCover)
                    .placeholder(R.drawable.default_placeholder)
                    .into(coverImage)
                albumNameText.text =
                    if (name.isNullOrEmpty()) string(R.string.unknown_album) else name
                //加空格，防止斜体最后一个字被截断
                artistNameText.text = "$artistName "
                songsCountText.text = getQuantityString(R.plurals.songs_count, songsCount)
            }
        }

    }
}