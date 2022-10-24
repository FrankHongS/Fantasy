package com.frankhon.fantasymusic.ui.activities.adapter

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.frankhon.fantasymusic.ui.fragments.AlbumFragment
import com.frankhon.fantasymusic.ui.fragments.ArtistFragment
import com.frankhon.fantasymusic.ui.fragments.BaseFragment
import com.frankhon.fantasymusic.ui.fragments.song.SongFragment

/**
 * Created by Frank Hon on 2020-04-19 20:01.
 * E-mail: frank_hon@foxmail.com
 */
class MainAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): BaseFragment {
        return when (position) {
            0 -> SongFragment()
            1 -> ArtistFragment()
            else -> AlbumFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}