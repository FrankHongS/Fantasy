package com.frankhon.fantasymusic.fragments.song

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.fragments.BaseFragment
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.utils.FileUtil
import com.frankhon.fantasymusic.vo.PlaySongEvent
import com.frankhon.fantasymusic.vo.SimpleSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/**
 * Created by Frank Hon on 2020-04-19 20:19.
 * E-mail: frank_hon@foxmail.com
 */
class SongFragment : BaseFragment {

    private var songs = arrayListOf<SimpleSong>()
    private lateinit var songAdapter: SongAdapter

    constructor() : super()

    constructor(name: String) : super(name)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_song, container, false)
        initView(view)
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            val newSongs = withContext(Dispatchers.IO) {
                FileUtil.getSongsFromAssets(requireContext())
            }
            songs.clear()
            songs.addAll(newSongs)
            songAdapter.notifyDataSetChanged()
        }
    }

    private fun initView(view: View) {
        val songsList = view.findViewById<RecyclerView>(R.id.rv_songs)
        songsList.layoutManager = LinearLayoutManager(context)
        songAdapter = SongAdapter(songs) { _, index ->
            AudioPlayerManager.getInstance().setPlayList(songs, index)
        }
        songsList.adapter = songAdapter
    }

}