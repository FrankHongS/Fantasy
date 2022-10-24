package com.frankhon.fantasymusic.ui.fragments.song

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.ui.fragments.main.MainViewModel
import com.frankhon.fantasymusic.application.ServiceLocator
import com.frankhon.fantasymusic.ui.fragments.BaseFragment
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.utils.setData
import com.frankhon.fantasymusic.utils.transferToSongItems
import com.frankhon.fantasymusic.vo.SimpleSong
import com.hon.mylogger.MyLogger

/**
 * Created by Frank Hon on 2020-04-19 20:19.
 * E-mail: frank_hon@foxmail.com
 */
class SongFragment : BaseFragment(), PlayerLifecycleObserver {

    /**
     * Activity之间共享ViewModel
     */
    private val model by activityViewModels<MainViewModel> {
        MainViewModel.FACTORY(ServiceLocator.provideMusicRepository(), this, arguments)
    }

    private var songs = arrayListOf<SimpleSong>()
    private lateinit var songAdapter: SongAdapter
    private lateinit var refreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MyLogger.d("onCreateView: ")
        val view = inflater.inflate(R.layout.fragment_song, container, false)
        initView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        MyLogger.d("onViewCreated: ")
        AudioPlayerManager.connect {
            it.registerLifecycleObserver(this)
        }
        model.songs.observe(viewLifecycleOwner) {
            MyLogger.d("setData: $it")
            refreshLayout.isRefreshing = false
            songs.run {
                setData(it)
                songAdapter.setData(
                    transferToSongItems(),
                    indexOf(AudioPlayerManager.getCurrentPlayerInfo()?.curSong)
                )
            }
        }
        model.getSongs()
    }

    override fun onDestroyView() {
        MyLogger.d("onDestroyView: ")
        super.onDestroyView()
        AudioPlayerManager.unregisterLifecycleObserver(this)
    }

    override fun onDestroy() {
        MyLogger.d("onDestroy: ")
        super.onDestroy()
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        model.select(songs.indexOf(song))
    }

    private fun initView(view: View) {
        refreshLayout = view.findViewById(R.id.srl_songs)
        val songsList = view.findViewById<RecyclerView>(R.id.rv_songs)

        refreshLayout.run {
            isRefreshing = true
            setOnRefreshListener {
                model.getSongs()
            }
        }
        songsList.run {
            layoutManager = LinearLayoutManager(context)
            songAdapter = SongAdapter { _, index ->
                AudioPlayerManager.setPlayList(songs, index)
            }
            adapter = songAdapter
        }
        model.selected.observe(viewLifecycleOwner) {
            MyLogger.d("select: $it $songAdapter")
            songAdapter.select(it)
        }
    }
}