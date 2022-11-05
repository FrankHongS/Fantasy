package com.frankhon.fantasymusic.ui.fragments.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frankhon.fantasymusic.data.repository.MusicRepository
import com.frankhon.fantasymusic.utils.singleArgSavedStateViewModelFactory
import com.frankhon.fantasymusic.vo.SimpleSong
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2022/8/30 10:47 下午.
 * E-mail: frank_hon@foxmail.com
 */
class MainViewModel(private val repository: MusicRepository, private val state: SavedStateHandle) :
    ViewModel() {

    companion object {
        val FACTORY = singleArgSavedStateViewModelFactory(::MainViewModel)
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

    fun deleteSong(index: Int) {
        val origin = _songs.value
        origin?.let {
            if (index < it.size) {
                _songs.value = origin.toMutableList().apply { removeAt(index) }
            }
        }
    }

    fun select(index: Int) {
        state.set(KEY_NOW_PLAYING, index)
    }
}