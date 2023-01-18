package com.frankhon.fantasymusic.ui.fragments.search

import android.os.Bundle
import android.transition.TransitionInflater
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
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.ui.fragments.BaseFragment
import com.frankhon.fantasymusic.ui.view.GridTextView
import com.frankhon.fantasymusic.utils.FRAGMENT_MAIN_TO_SEARCH_TRANSITION_NAME
import com.frankhon.fantasymusic.utils.showToast
import com.frankhon.fantasymusic.utils.string
import com.frankhon.fantasymusic.vo.event.DownloadCompleteEvent
import com.frankhon.fantasymusic.vo.view.DOWNLOAD_STATE_DOWNLOADED
import com.frankhon.fantasymusic.vo.view.SearchSongItem
import com.frankhon.simplesearchview.generator.DefaultSearchSuggestionGenerator
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.fragment_search.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Frank Hon on 2020-05-19 21:06.
 * E-mail: frank_hon@foxmail.com
 */
class SearchFragment : BaseFragment() {

    private val searchViewModel by viewModels<SearchViewModel> {
        SearchViewModel.FACTORY(ServiceLocator.provideSearchRepository(), this, arguments)
    }
    private lateinit var searchResultAdapter: SearchResultAdapter
    private var searchResultList: List<SearchSongItem>? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var searchBack: View
    private lateinit var suggestionSongs: GridTextView

    private val downloader by lazy { SongDownloader(requireContext()) }

    /**
     * 错误信息Toast只显示一次，横竖屏切换不显示
     */
    private var shouldShowErrorToast = false

    private val suggestionSongList = listOf("园游会", "七里香", "稻香", "不能说的秘密", "一路向北")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EventBus.getDefault().register(this)
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        initView(view)
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.fragment_transition)
        searchBack.transitionName = FRAGMENT_MAIN_TO_SEARCH_TRANSITION_NAME
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(downloader)
        svg_search_songs.run {
            setHistoryCount(10)
            setSuggestionGenerator(DefaultSearchSuggestionGenerator(context))
            setOnSearchListener {
                doSearch(it)
            }
            setOnBackClickListener {
                parentFragmentManager.popBackStack()
            }
        }
        suggestionSongs.setData(suggestionSongList) {
            doSearch(it)
        }
        rv_search_result.run {
            layoutManager = LinearLayoutManager(context)
            searchResultAdapter = SearchResultAdapter(AppExecutors.getInstance(), downloader) {
                AudioPlayerManager.playAndAddIntoPlaylist(it)
            }
            adapter = searchResultAdapter
        }
        searchViewModel.searchResult.observe(viewLifecycleOwner) { result ->
            progressBar.isVisible = false
            val (isSuccess, errorMessage) = result
            if (!isSuccess && shouldShowErrorToast) {
                shouldShowErrorToast = false
                showToast(errorMessage ?: string(R.string.search_error))
                MyLogger.e("Search songs: $errorMessage")
            }
        }
        searchViewModel.songs.observe(viewLifecycleOwner) {
            updateSongList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        viewLifecycleOwner.lifecycle.removeObserver(downloader)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onDownloadComplete(event: DownloadCompleteEvent) {
        event.run {
            searchResultList?.forEachIndexed { index, item ->
                if (item.name == song.name && item.artist == song.artist) {
                    item.songUri = song.songUri
                    item.lyricsUri = song.lyricsUri
                    item.downloadState = DOWNLOAD_STATE_DOWNLOADED
                    searchResultAdapter.notifyItemChanged(index)
                    return
                }
            }
        }
    }

    private fun initView(view: View) {
        progressBar = view.findViewById(R.id.pb_search)
        searchBack = view.findViewById(R.id.ib_search_back)
        suggestionSongs = view.findViewById(R.id.gtv_search_suggestions)
    }

    private fun doSearch(it: String) {
        progressBar.isVisible = true
        shouldShowErrorToast = true
        searchViewModel.findSongs(it)
    }

    private fun updateSongList(searchedItems: List<SearchSongItem>) {
        suggestionSongs.isVisible = false
        searchResultList = searchedItems
        searchResultAdapter.submitList(searchedItems)
    }

}