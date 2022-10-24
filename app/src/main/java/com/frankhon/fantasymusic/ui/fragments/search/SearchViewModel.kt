package com.frankhon.fantasymusic.ui.fragments.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frankhon.fantasymusic.data.Result
import com.frankhon.fantasymusic.data.repository.SearchRepository
import com.frankhon.fantasymusic.utils.singleArgSavedStateViewModelFactory
import com.frankhon.fantasymusic.vo.bean.DataSongWrapper
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

    private val _searchResult = MutableLiveData<Result<DataSongWrapper>>()
    val searchResult: MutableLiveData<Result<DataSongWrapper>>
        get() {
            return _searchResult
        }

    private val _songs = state.getLiveData<DataSongWrapper>(KEY_SONG_LIST)
    val songs: MutableLiveData<DataSongWrapper>
        get() {
            return _songs
        }

    fun findSongs(keyword: String) {
        viewModelScope.launch {
            val result = repository.findSong(keyword)
            _searchResult.value = result
            result.data?.let {
                state.set(KEY_SONG_LIST, it)
            }
        }
    }

}