package com.frankhon.fantasymusic.ui.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.PlayMode
import com.frankhon.fantasymusic.media.isStopped
import com.frankhon.fantasymusic.media.observer.PlayerLifecycleObserver
import com.frankhon.fantasymusic.ui.activities.MainActivity
import com.frankhon.fantasymusic.ui.activities.about.AboutActivity
import com.frankhon.fantasymusic.ui.activities.adapter.MainAdapter
import com.frankhon.fantasymusic.ui.activities.settings.SettingsActivity
import com.frankhon.fantasymusic.ui.fragments.BaseFragment
import com.frankhon.fantasymusic.ui.fragments.search.SearchFragment
import com.frankhon.fantasymusic.ui.view.SlidingUpPanelLayout
import com.frankhon.fantasymusic.ui.view.panel.HomeBottomControlPanel
import com.frankhon.fantasymusic.utils.FRAGMENT_MAIN_TO_SEARCH_TRANSITION_NAME
import com.frankhon.fantasymusic.utils.dp
import com.frankhon.fantasymusic.utils.navigate
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
 *
 * 暂时放弃使用navigation，bug不可控
 */
class MainFragment : BaseFragment(), PlayerLifecycleObserver {

    private var parentActivity: MainActivity? = null

    private lateinit var drawer: DrawerLayout
    private lateinit var panelLayout: SlidingUpPanelLayout
    private lateinit var controlPanel: HomeBottomControlPanel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MyLogger.d("onCreateView")
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        MyLogger.d("onViewCreated: ")
        super.onViewCreated(view, savedInstanceState)
        parentActivity = activity as? MainActivity
        initView(view)
    }

    override fun onStart() {
        super.onStart()
        connectAudioPlayer()
        controlPanel.connectAudioPlayer()
    }

    override fun onStop() {
        super.onStop()
        disconnectAudioPlayer()
        controlPanel.disconnectAudioPlayer()
    }

    override fun onDestroyView() {
        MyLogger.d("onDestroyView: ")
        super.onDestroyView()
    }

    override fun onPlayerConnected(playerInfo: CurrentPlayerInfo?) {
        playerInfo?.run {
            val isPlayerStopped = curPlayerState.isStopped()
            panelLayout.isAllowDragging(!isPlayerStopped)
            if (isPlayerStopped) {
                collapsePanel()
            }
        }
    }

    override fun onPrepare(song: SimpleSong, playMode: PlayMode, curIndex: Int, totalSize: Int) {
        panelLayout.isAllowDragging(true)
    }

    override fun onAudioStop() {
        resetPanelLayout()
    }

    private fun initView(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_main)
        val viewPager = view.findViewById<ViewPager>(R.id.vp_main)
        val tabLayout = view.findViewById<TabLayout>(R.id.tl_main)
        val navigationView = view.findViewById<NavigationView>(R.id.nv_drawer)

        drawer = view.findViewById(R.id.dl_main)
        panelLayout = view.findViewById(R.id.supl_main)
        controlPanel = view.findViewById(R.id.cbpl_control_panel)

        toolbar.setOnMenuItemClickListener {
            val actionView = (toolbar.getChildAt(1) as ActionMenuView)[0]
            when (it.itemId) {
                R.id.action_search -> parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SearchFragment())
                    .addSharedElement(
                        actionView.apply {
                            transitionName = FRAGMENT_MAIN_TO_SEARCH_TRANSITION_NAME
                        },
                        FRAGMENT_MAIN_TO_SEARCH_TRANSITION_NAME
                    )
                    .addToBackStack(null)
                    .commit()
            }
            true
        }
        viewPager.run {
            //note: 使用childFragmentManager，不能使用parentFragmentManager，或者横竖屏切换时viewPager不能恢复
            adapter = MainAdapter(childFragmentManager)
            pageMargin = 16.dp
        }
        tabLayout.setupWithViewPager(viewPager)
        navigationView.run {
            setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_settings -> parentActivity?.navigate<SettingsActivity>()
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

        resetPanelLayout()
    }

    private fun connectAudioPlayer() {
        AudioPlayerManager.connect(object : AudioPlayerManager.OnServiceConnectedListener {
            override fun onServiceConnected(manager: AudioPlayerManager) {
                manager.registerLifecycleObserver(this@MainFragment)
            }
        })
    }

    private fun disconnectAudioPlayer() {
        AudioPlayerManager.run {
            unregisterLifecycleObserver(this@MainFragment)
        }
    }

    private fun resetPanelLayout() {
        collapsePanel()
        panelLayout.isAllowDragging(false)
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
