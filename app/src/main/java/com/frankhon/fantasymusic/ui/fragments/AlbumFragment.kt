package com.frankhon.fantasymusic.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.application.ServiceLocator

/**
 * Created by Frank Hon on 2020-04-19 20:20.
 * E-mail: frank_hon@foxmail.com
 */
class AlbumFragment : BaseFragment() {

    private val model by activityViewModels<AlbumViewModel> {
        AlbumViewModel.FACTORY(ServiceLocator.provideAlbumRepository(), this, arguments)
    }

    private lateinit var albumsRecyclerView: RecyclerView
    private lateinit var albumsAdapter: AlbumAdapter
    private lateinit var refreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initData()
    }

    private fun initView(view: View) {
        albumsRecyclerView = view.findViewById(R.id.rv_albums)
        refreshLayout = view.findViewById(R.id.srl_albums)

        albumsRecyclerView.run {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = AlbumAdapter().apply {
                albumsAdapter = this
            }
        }
        refreshLayout.setOnRefreshListener {
            model.loadAlbums()
        }
    }

    private fun initData() {
        model.albumsLiveData.observe(viewLifecycleOwner) {
            inflateEmptyPlaceholder(it.isEmpty())
            refreshLayout.isRefreshing = false
            albumsAdapter.setData(it)
        }
        if (model.albumsLiveData.value == null) {
            refreshLayout.isRefreshing = true
            model.loadAlbums()
        }
    }
}