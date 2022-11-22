package com.frankhon.fantasymusic.ui.fragments.song

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.frankhon.fantasymusic.data.repository.MusicRepository
import com.frankhon.fantasymusic.utils.setData
import com.frankhon.fantasymusic.utils.singleArgSavedStateViewModelFactory
import com.frankhon.fantasymusic.vo.SimpleSong
import kotlinx.coroutines.delay

/**
 * Created by Frank Hon on 2022/8/30 10:47 下午.
 * E-mail: frank_hon@foxmail.com
 */
class SongViewModel(private val repository: MusicRepository, private val state: SavedStateHandle) :
    ViewModel() {

    companion object {
        private const val KEY_NOW_PLAYING = "KEY_NOW_PLAYING"
        val FACTORY = singleArgSavedStateViewModelFactory(::SongViewModel)
    }

    private val _selected = state.getLiveData<SimpleSong?>(KEY_NOW_PLAYING)
    val selected: MutableLiveData<SimpleSong?>
        get() {
            return _selected
        }

    private var _count: Int = -1
    val count: Int
        get() = _count

    private val _songs = mutableListOf<SimpleSong>()
    val songs: List<SimpleSong>
        get() = _songs

    private var page = 1

    suspend fun getCount() = repository.getCount().apply { _count = this }

    /**
     * 获取首页数据，并重置页数
     */
    suspend fun loadSongs(): List<SimpleSong> {
        page = 1
        return repository.getSongs(page).apply { _songs.setData(this) }
    }

    /**
     * 获取更多数据
     */
    suspend fun loadMoreSongs(): List<SimpleSong> {
        delay(500)
        page++
        return repository.getSongs(page).apply { _songs.addAll(this) }
    }

    suspend fun getAllSongs(): List<SimpleSong> {
        return repository.getAllSongs()
    }

    fun select(song: SimpleSong?) {
        state.set(KEY_NOW_PLAYING, song)
    }

    fun deleteSong(index: Int) {
        _songs.removeAt(index)
        _count--
    }
}