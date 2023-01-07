package com.frankhon.fantasymusic.ui.fragments

import androidx.lifecycle.*
import com.frankhon.fantasymusic.data.repository.ArtistRepository
import com.frankhon.fantasymusic.utils.singleArgSavedStateViewModelFactory
import com.frankhon.fantasymusic.utils.transferToArtistItems
import com.frankhon.fantasymusic.vo.view.ArtistItem
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2022/8/30 10:47 下午.
 * E-mail: frank_hon@foxmail.com
 */
class ArtistViewModel(
    private val repository: ArtistRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    companion object {
        val FACTORY = singleArgSavedStateViewModelFactory(::ArtistViewModel)
    }

    private val _artistsLiveData = MutableLiveData<List<ArtistItem>>()
    val artistsLiveData: LiveData<List<ArtistItem>>
        get() = _artistsLiveData

    fun loadArtists() {
        viewModelScope.launch {
            _artistsLiveData.value = repository.getSongInfoByArtist().transferToArtistItems()
        }
    }

}