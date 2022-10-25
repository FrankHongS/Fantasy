package com.frankhon.fantasymusic.ui.fragments.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frankhon.fantasymusic.data.repository.SearchRepository
import com.frankhon.fantasymusic.utils.singleArgSavedStateViewModelFactory
import com.frankhon.fantasymusic.utils.transferToSearchSongItems
import com.frankhon.fantasymusic.vo.view.SearchSongItem
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2022/9/24 11:31 上午.
 * E-mail: frank_hon@foxmail.com
 */
class SearchViewModel(
    private val repository: SearchRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    companion object {
        val FACTORY = singleArgSavedStateViewModelFactory(::SearchViewModel)
        private const val KEY_SONG_LIST = "KEY_SONG_LIST"
    }

    private val _searchResult = MutableLiveData<Pair<Boolean, String?>>()
    val searchResult: MutableLiveData<Pair<Boolean, String?>>
        get() {
            return _searchResult
        }

    private val _songs = state.getLiveData<List<SearchSongItem>>(
        KEY_SONG_LIST
    )
    val songs: MutableLiveData<List<SearchSongItem>>
        get() {
            return _songs
        }

    fun findSongs(keyword: String) {
        viewModelScope.launch {
            val result = repository.findSong(keyword)
            _searchResult.value = Pair(result.isSuccess, result.errorMessage)
            result.data?.let {
                state.set(KEY_SONG_LIST, it.data.songs.transferToSearchSongItems())
            }
        }
    }

}