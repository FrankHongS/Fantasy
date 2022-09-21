package com.frankhon.fantasymusic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.activities.MainActivity
import com.frankhon.fantasymusic.activities.about.AboutActivity
import com.frankhon.fantasymusic.activities.adapter.MainAdapter
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.media.PlayerState
import com.frankhon.fantasymusic.media.observer.PlayerConfigurationObserver
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.view.AnimatedAudioCircleImageView
import com.frankhon.fantasymusic.view.AnimatedAudioToggleButton
import com.frankhon.fantasymusic.view.PlayModeImageButton
import com.frankhon.fantasymusic.view.SlidingUpPanelLayout
import com.frankhon.fantasymusic.vo.CurrentPlayerInfo
import com.frankhon.fantasymusic.vo.SimpleSong
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.hon.mylogger.MyLogger

/**
 * Created by Frank Hon on 2020-04-14 23:41.
 * E-mail: frank_hon@foxmail.com
 *
 * 使用navigation，navigate到新的fragment时，旧的fragment的view会被销毁，但实例会保留。
 * 针对这个issue，目前暂时通过全局变量mainView和标识位isInstantiate来避免重复创建view，否则FragmentViewPager无法正常创建
 */
class MainFragment : BaseFragment(), PlayerLifecycleObserver, PlayerConfigurationObserver {

    private var parentActivity: MainActivity? = null
    private var mainView: View? = null

    private lateinit var drawer: DrawerLayout
    private lateinit var panelLayout: SlidingUpPanelLayout
    private lateinit var toggleButton: AnimatedAudioToggleButton
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var playModeButton: PlayModeImageButton
    private lateinit var playlistButton: ImageButton
    private lateinit var albumImage: AnimatedAudioCircleImageView
    private lateinit var songName: TextView
    private lateinit var artistName: TextView
    private lateinit var progressSeekBar: SeekBar
    private lateinit var currentTime: TextView
    private lateinit var durationText: TextView

    //note: 使用childFragmentManager，不能使用parentFragmentManager，或者横竖屏切换时viewPager不能恢复
    private val viewPagerAdapter by lazy { MainAdapter(childFragmentManager) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MyLogger.d("onCreateView")
        if (mainView == null) {
            mainView = inflater.inflate(R.layout.fragment_main, container, false)
        }
        return mainView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        MyLogger.d("onViewCreated: isInstantiate = $isInstantiate")
        if (isInstantiate) {
            return
        }
        parentActivity = activity as? MainActivity
        initView(view)
        connectAudioPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectAudioPlayer()
    }

    //region Audio lifecycle
    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        playerInfo?.run {
            curSong?.let {
                updateSongPanel(it)
                updatePlayControlIcon(curPlayerState)
                updatePreviousNextButton(curPlayMode, curSongIndex, curPlaylist.size)
                playModeButton.playMode = PlayModeImageButton.State.valueOf(curPlayMode.name)
                updateSongDuration(it)
                // update progress
                currentTime.text = msToMMSS(curPlaybackPosition)
                progressSeekBar.progress = curPlaybackPosition.toInt()
            }
        }
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        updateSongPanel(song)
        toggleButton.setPlayState(AnimatedAudioToggleButton.ControlButtonState.PREPARING)
        updatePreviousNextButton(playMode, curIndex, totalSize)
        //更新播放列表中当前播放歌曲
        updatePlaylistPopup(playlistButton, index = curIndex)
    }

    override fun onPlaying(song: SimpleSong) {
        updateSongDuration(song)
        updatePlayControlIcon(PlayerState.PLAYING)
        albumImage.startRotateAnimator()
    }

    override fun onAudioPause() {
        updatePlayControlIcon(PlayerState.PAUSED)
        albumImage.cancelRotateAnimator()
    }

    override fun onAudioStop() {
        setDefaultPanel()
    }

    override fun onFinished() {
        updatePlayControlIcon(PlayerState.FINISHED)
    }

    override fun onError(errorMsg: String) {
        updatePlayControlIcon(PlayerState.PAUSED)
        if (errorMsg.isNotEmpty()) {
            ToastUtil.showToast(errorMsg)
        }
    }
    //endregion

    //region Audio player configuration
    override fun onProgressUpdated(curPosition: Long, duration: Long) {
        albumImage.startUpdateProgress(curPosition.toInt(), duration.toInt())
        currentTime.text = msToMMSS(curPosition)
        val isTracking = (progressSeekBar.tag as? Boolean) ?: false
        //未拖拽时更新进度条
        if (!isTracking) {
            progressSeekBar.progress = curPosition.toInt()
        }
    }

    override fun onPlayModeChanged(playMode: PlayMode, curIndex: Int, totalSize: Int) {
        updatePreviousNextButton(playMode, curIndex, totalSize)
    }

    override fun onPlaylistChanged(playMode: PlayMode, playlist: List<SimpleSong>, curIndex: Int) {
        updatePlaylistPopup(
            playlistButton,
            newPlaylist = playlist.transferToSongItems(curIndex)
        )
        updatePreviousNextButton(playMode, curIndex, playlist.size)
    }
    //endregion

    private fun initView(view: View) {
        drawer = view.findViewById(R.id.dl_main)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_main)
        val viewPager = view.findViewById<ViewPager>(R.id.vp_main)
        val tabLayout = view.findViewById<TabLayout>(R.id.tl_main)
        val navigationView = view.findViewById<NavigationView>(R.id.nv_drawer)

        panelLayout = view.findViewById(R.id.supl_main)
        toggleButton = view.findViewById(R.id.ib_pause_or_resume)
        nextButton = view.findViewById(R.id.ib_next_song)
        prevButton = view.findViewById(R.id.ib_previous_song)
        playModeButton = view.findViewById(R.id.ib_play_mode)
        playlistButton = view.findViewById(R.id.ib_playlist)
        albumImage = view.findViewById(R.id.iv_song_bottom_pic)
        songName = view.findViewById(R.id.tv_bottom_song_name)
        artistName = view.findViewById(R.id.tv_bottom_artist_name)
        progressSeekBar = view.findViewById(R.id.sb_play_progress)
        currentTime = view.findViewById(R.id.tv_current_time)
        durationText = view.findViewById(R.id.tv_duration)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_search -> NavHostFragment.findNavController(this)
                    .navigate(R.id.searchFragment)
            }
            true
        }
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        navigationView.run {
            setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_about -> parentActivity?.navigate<AboutActivity>()
                }
                true
            }
            inflateHeaderView(R.layout.nav_header_main)
        }
        drawer.addDrawerListener(ActionBarDrawerToggle(
            parentActivity,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ).apply {
            syncState()
        })

        toggleButton.setOnControlButtonClickListener { curState ->
            when (curState) {
                AnimatedAudioToggleButton.ControlButtonState.PLAYING -> {
                    val currentPlayerInfo = AudioPlayerManager.getCurrentPlayerInfo()
                    currentPlayerInfo?.run {
                        if (curPlayerState == PlayerState.ERROR) {
                            AudioPlayerManager.play(curSong)
                        } else {
                            AudioPlayerManager.resume()
                        }
                    } ?: kotlin.run { AudioPlayerManager.resume() }
                }
                AnimatedAudioToggleButton.ControlButtonState.PAUSED -> AudioPlayerManager.pause()
                else -> {}
            }
        }
        nextButton.setOnClickListener {
            AudioPlayerManager.next()
        }
        prevButton.setOnClickListener {
            AudioPlayerManager.previous()
        }
        playModeButton.setPlayModeListener {
            val toastText = when (it) {
                PlayModeImageButton.State.SHUFFLE -> {
                    AudioPlayerManager.setPlayMode(PlayMode.SHUFFLE)
                    getString(R.string.play_mode_shuffle)
                }
                PlayModeImageButton.State.LOOP_SINGLE -> {
                    AudioPlayerManager.setPlayMode(PlayMode.LOOP_SINGLE)
                    getString(R.string.play_mode_single_loop)
                }
                PlayModeImageButton.State.LOOP_LIST -> {
                    AudioPlayerManager.setPlayMode(PlayMode.LOOP_LIST)
                    getString(R.string.play_mode_list_loop)
                }
            }
            ToastUtil.showToast(toastText)
        }
        progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                seekBar.tag = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekBar.tag = false
                AudioPlayerManager.seekTo(seekBar.progress)
            }
        })
        playlistButton.setOnClickListener {
            val currentPlayerInfo = AudioPlayerManager.getCurrentPlayerInfo()
            currentPlayerInfo?.run {
                showPlaylistPopup(it, curPlaylist.transferToSongItems(curSongIndex), {
                    AudioPlayerManager.removeSongFromPlayList(it)
                }) {
                    AudioPlayerManager.play(it)
                }
            }
        }
        setDefaultPanel()
    }

    private fun setDefaultPanel() {
        albumImage.run {
            setImageResource(R.mipmap.ic_launcher)
            cancelRotateAnimator()
        }
        currentTime.text = msToMMSS(0)
        durationText.text = ""
        songName.text = getText(R.string.app_name)
        artistName.text = getText(R.string.welcome_text)
        toggleButton.setPlayState(AnimatedAudioToggleButton.ControlButtonState.INITIAL)
        prevButton.isEnabled = false
        nextButton.isEnabled = false
        progressSeekBar.progress = 0
        panelLayout.setAllowDragging(false)
        collapsePanel()
    }

    private fun updatePlayControlIcon(playerState: PlayerState) {
        when (playerState) {
            PlayerState.PLAYING, PlayerState.RESUMED -> toggleButton.setPlayState(
                AnimatedAudioToggleButton.ControlButtonState.PLAYING
            )
            PlayerState.PREPARING -> toggleButton.setPlayState(AnimatedAudioToggleButton.ControlButtonState.PREPARING)
            else -> toggleButton.setPlayState(AnimatedAudioToggleButton.ControlButtonState.PAUSED)
        }
    }

    private fun updateSongPanel(song: SimpleSong) {
        song.run {
            parentActivity?.takeIf { !it.isDestroyed && !it.isFinishing }?.let {
                Glide.with(it)
                    .load(songPic)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(albumImage)
                songName.text = name
                artistName.text = artist
                panelLayout.setAllowDragging(true)
            }
        }
    }

    private fun updatePreviousNextButton(curPlayMode: PlayMode, curIndex: Int, totalSize: Int) {
        if (totalSize == 1) {
            prevButton.isEnabled = false
            nextButton.isEnabled = false
            return
        }
        if (curPlayMode == PlayMode.LOOP_SINGLE) {
            prevButton.isEnabled = curIndex != 0
            nextButton.isEnabled = curIndex != totalSize - 1
        } else {
            prevButton.isEnabled = true
            nextButton.isEnabled = true
        }
    }

    private fun updateSongDuration(song: SimpleSong) {
        song.let {
            durationText.text = msToMMSS(it.duration)
            progressSeekBar.run {
                max = it.duration.toInt()
            }
        }
    }

    private fun connectAudioPlayer() {
        AudioPlayerManager.connect {
            it.registerLifecycleObserver(this)
            it.registerProgressObserver(this)
        }
    }

    private fun disconnectAudioPlayer() {
        AudioPlayerManager.run {
            unregisterLifecycleObserver(this@MainFragment)
            unregisterProgressObserver(this@MainFragment)
        }
    }

    fun collapsePanel() = panelLayout.collapse()

    fun closeDrawer(): Boolean {
        return if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
            true
        } else {
            false
        }
    }
}
