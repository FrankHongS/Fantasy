package com.frankhon.fantasymusic.ui.fragments

import androidx.lifecycle.*
import com.frankhon.fantasymusic.data.repository.AlbumRepository
import com.frankhon.fantasymusic.utils.singleArgSavedStateViewModelFactory
import com.frankhon.fantasymusic.utils.transferToAlbumItems
import com.frankhon.fantasymusic.vo.view.AlbumItem
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2022/8/30 10:47 下午.
 * E-mail: frank_hon@foxmail.com
 */
class AlbumViewModel(
    private val repository: AlbumRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    companion object {
        val FACTORY = singleArgSavedStateViewModelFactory(::AlbumViewModel)
    }

    private val _albumsLiveData = MutableLiveData<List<AlbumItem>>()
    val albumsLiveData: LiveData<List<AlbumItem>>
        get() = _albumsLiveData

    fun loadAlbums() {
        viewModelScope.launch {
            _albumsLiveData.value = repository.getSongInfoByAlbum().transferToAlbumItems()
        }
    }

}