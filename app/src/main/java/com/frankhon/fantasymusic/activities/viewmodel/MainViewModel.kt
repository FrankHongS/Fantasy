package com.frankhon.fantasymusic.activities.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frankhon.fantasymusic.data.source.MusicRepository
import com.frankhon.fantasymusic.utils.singleArgViewModelFactory
import com.frankhon.fantasymusic.vo.SimpleSong
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2022/8/30 10:47 下午.
 * E-mail: frank_hon@foxmail.com
 */
class MainViewModel(private val repository: MusicRepository, private val state: SavedStateHandle) :
    ViewModel() {

    companion object {
        val FACTORY = singleArgViewModelFactory(::MainViewModel)

        private const val KEY_NOW_PLAYING = "KEY_NOW_PLAYING"
    }

    private val _songs = MutableLiveData<List<SimpleSong>>()
    val songs: MutableLiveData<List<SimpleSong>>
        get() {
            return _songs
        }

    private val _selected = state.getLiveData<Int>(KEY_NOW_PLAYING)
    val selected: MutableLiveData<Int>
        get() {
            return _selected
        }

    fun getSongs() {
        viewModelScope.launch {
            _songs.value = repository.getSongs()
        }
    }

    fun select(index: Int) {
        state.set(KEY_NOW_PLAYING, index)
    }
}