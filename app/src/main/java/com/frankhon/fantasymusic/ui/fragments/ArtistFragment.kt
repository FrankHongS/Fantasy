package com.frankhon.fantasymusic.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.application.ServiceLocator
import com.frankhon.fantasymusic.ui.base.BaseViewHolder
import com.frankhon.fantasymusic.utils.setData
import com.frankhon.fantasymusic.utils.string
import com.frankhon.fantasymusic.vo.view.ArtistItem

/**
 * [androidx.viewpager2.widget.ViewPager2]有bug，当RecyclerView为StaggeredGridLayoutManager时，
 * ViewPager切换Fragment，RecyclerView上方item会自动对齐，改用[androidx.viewpager.widget.ViewPager]
 *
 * Created by Frank Hon on 2020-04-19 20:19.
 * E-mail: frank_hon@foxmail.com
 */
class ArtistFragment : BaseFragment() {

    private val model by activityViewModels<ArtistViewModel> {
        ArtistViewModel.FACTORY(ServiceLocator.provideArtistRepository(), this, arguments)
    }
    private lateinit var artistsRecyclerView: RecyclerView
    private lateinit var artistsAdapter: ArtistsAdapter
    private lateinit var refreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_artist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initData()
    }

    private fun initView(view: View) {
        artistsRecyclerView = view.findViewById(R.id.rv_artists)
        refreshLayout = view.findViewById(R.id.srl_artists)

        artistsRecyclerView.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = ArtistsAdapter().apply {
                artistsAdapter = this
            }
        }
        refreshLayout.setOnRefreshListener {
            model.loadArtists()
        }
    }

    private fun initData() {
        model.artistsLiveData.observe(viewLifecycleOwner) {
            inflateEmptyPlaceholder(it.isEmpty())
            refreshLayout.isRefreshing = false
            artistsAdapter.setData(it)
        }

        if (model.artistsLiveData.value == null) {
            refreshLayout.isRefreshing = true
            model.loadArtists()
        }
    }

    private class ArtistsAdapter : RecyclerView.Adapter<ArtistsViewHolder>() {

        private val artistsData = mutableListOf<ArtistItem>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistsViewHolder {
            return ArtistsViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_artist_list, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ArtistsViewHolder, position: Int) {
            holder.bindView(position, artistsData[position])
        }

        override fun getItemCount(): Int {
            return artistsData.size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setData(artists: List<ArtistItem>) {
            if (artists == artistsData) {
                return
            }
            artistsData.setData(artists)
            notifyDataSetChanged()
        }
    }

    private class ArtistsViewHolder(view: View) : BaseViewHolder<ArtistItem>(view) {

        val coverImage: ImageView = view.findViewById(R.id.iv_album_cover_artist)
        private val artistName: TextView = view.findViewById(R.id.tv_name_artist)
        private val songsCount: TextView = view.findViewById(R.id.tv_songs_count_artist)

        override fun bindView(index: Int, item: ArtistItem) {
            Glide.with(itemView)
                .load(item.albumCover)
                .placeholder(R.drawable.default_placeholder)
                .into(coverImage)
            artistName.text = item.name
            songsCount.text = String.format(string(R.string.songs_count), item.songsCount)
        }

    }
}