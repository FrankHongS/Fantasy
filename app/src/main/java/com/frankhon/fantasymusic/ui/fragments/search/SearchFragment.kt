package com.frankhon.fantasymusic.ui.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.application.AppExecutors
import com.frankhon.fantasymusic.application.ServiceLocator
import com.frankhon.fantasymusic.ui.fragments.BaseFragment
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.utils.showToast
import com.frankhon.fantasymusic.vo.bean.DataSongInner
import com.frankhon.simplesearchview.generator.DefaultSearchSuggestionGenerator
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.fragment_search.*

/**
 * Created by Frank Hon on 2020-05-19 21:06.
 * E-mail: frank_hon@foxmail.com
 */
class SearchFragment : BaseFragment() {

    private val searchViewModel by viewModels<SearchViewModel> {
        SearchViewModel.FACTORY(ServiceLocator.provideSearchRepository(), this, arguments)
    }
    private lateinit var searchResultAdapter: SearchResultAdapter
    private var songWrapper: DataSongInner? = null

    private lateinit var progressBar: ProgressBar

    private val downloader by lazy { SongDownloader(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        initView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycle.addObserver(downloader)
        rv_search_result.run {
            layoutManager = LinearLayoutManager(context)
            searchResultAdapter = SearchResultAdapter(AppExecutors.getInstance(), downloader) {
                AudioPlayerManager.playAndAddIntoPlaylist(it)
            }
            adapter = searchResultAdapter
        }

        svg_search_songs.run {
            setSuggestionGenerator(DefaultSearchSuggestionGenerator(context))
            setOnSearchListener {
                searchViewModel.findSongs(it)
                progressBar.isVisible = true
            }
            setOnBackClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        searchViewModel.searchResult.observe(viewLifecycleOwner) {
            progressBar.isVisible = false
            if (it.isSuccess) {
                it.data?.let { songWrapper ->
                    updateSongList(songWrapper.data)
                }
            } else {
                showToast(it.errorMessage ?: "搜歌出现异常")
                MyLogger.e("Search songs: ${it.errorMessage}")
            }
        }
        searchViewModel.songs.observe(viewLifecycleOwner) {
            updateSongList(it.data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(downloader)
    }

    private fun initView(view: View) {
        progressBar = view.findViewById(R.id.pb_search)
    }

    private fun updateSongList(songWrapper: DataSongInner) {
        this.songWrapper = songWrapper
        searchResultAdapter.submitList(songWrapper.songs)
    }

}