package com.frankhon.fantasymusic.ui.fragments.song

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.application.ServiceLocator
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.ui.fragments.BaseFragment
import com.frankhon.fantasymusic.utils.DEFAULT_SONGS_PAGE_LIMIT
import com.frankhon.fantasymusic.utils.popup.showMorePopup
import com.frankhon.fantasymusic.utils.transferToSongItems
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.event.SongDeleteEvent
import com.hon.mylogger.MyLogger
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Frank Hon on 2020-04-19 20:19.
 * E-mail: frank_hon@foxmail.com
 */
class SongFragment : BaseFragment(), PlayerLifecycleObserver {

    /**
     * Activity之间共享ViewModel
     */
    private val model by activityViewModels<SongViewModel> {
        SongViewModel.FACTORY(ServiceLocator.provideMusicRepository(), this, arguments)
    }

    private lateinit var songAdapter: SongAdapter
    private lateinit var refreshLayout: SwipeRefreshLayout

    private val songList
        get() = model.songs

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MyLogger.d("onCreateView: ")
        EventBus.getDefault().register(this)
        val view = inflater.inflate(R.layout.fragment_song, container, false)
        initView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        MyLogger.d("onViewCreated: $savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        AudioPlayerManager.connect(object : AudioPlayerManager.OnServiceConnectedListener {
            override fun onServiceConnected(manager: AudioPlayerManager) {
                manager.registerLifecycleObserver(this@SongFragment)
            }
        })
        if (model.count == -1) {
            loadSongs()
        } else {
            setData(model.songs, model.count)
        }
    }

    override fun onDestroyView() {
        MyLogger.d("onDestroyView: ")
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        AudioPlayerManager.unregisterLifecycleObserver(this)
    }

    override fun onDestroy() {
        MyLogger.d("onDestroy: ")
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onSongDelete(event: SongDeleteEvent) {
        val index = songList.indexOf(event.song)
        if (index != -1) {
            model.deleteSong(index)
            songAdapter.deleteSong(index)
        }
    }

    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        songAdapter.select(playerInfo?.curSong)
    }

    // region Audio Lifecycle
    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        songAdapter.select(song)
    }

    override fun onAudioStop() {
        // 取消选中
        songAdapter.select(null)
    }
    // endregion

    private fun initView(view: View) {
        refreshLayout = view.findViewById(R.id.srl_songs)
        val songsList = view.findViewById<RecyclerView>(R.id.rv_songs)

        refreshLayout.run {
            isRefreshing = true
            setOnRefreshListener {
                loadSongs()
            }
        }
        songsList.run {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
            adapter = SongAdapter(
                pageLimit = DEFAULT_SONGS_PAGE_LIMIT,
                onPlayAllClickListener = {
                    lifecycleScope.launchWhenResumed {
                        val allSongs = model.getAllSongs()
                        AudioPlayerManager.setPlayList(allSongs)
                    }
                },
                onMoreClickListener = { view, index ->
                    view.showMorePopup(songList[index], lifecycleScope)
                }) { _, index ->
                AudioPlayerManager.playAndAddIntoPlaylist(songList[index])
            }.apply {
                songAdapter = this
                setOnLoadListener {
                    lifecycleScope.launchWhenResumed {
                        val moreSongs = model.loadMoreSongs(offset = getDataSize())
                        addSongs(
                            moreSongs.transferToSongItems(),
                            getCurrentSong()
                        )
                    }
                }
            }
            addItemDecoration(SongItemDecoration())
        }
    }

    private fun loadSongs() {
        lifecycleScope.launch {
            val songs = model.loadSongs()
            val count = model.getCount()
            setData(songs, count)
        }
    }

    private fun setData(songs: List<SimpleSong>, count: Int) {
        MyLogger.d("setData: $count")
        refreshLayout.isRefreshing = false
        songAdapter.run {
            setSongs(count, songs.transferToSongItems(), getCurrentSong())
            if (songs.size == count) {
                markNoMoreState()
            }
        }
    }

    private fun getCurrentSong() = AudioPlayerManager.getCurrentPlayerInfo()?.curSong
}