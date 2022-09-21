package com.frankhon.fantasymusic.activities.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.frankhon.fantasymusic.fragments.AlbumFragment
import com.frankhon.fantasymusic.fragments.ArtistFragment
import com.frankhon.fantasymusic.fragments.BaseFragment
import com.frankhon.fantasymusic.fragments.song.SongFragment

/**
 * Created by Frank Hon on 2020-04-19 20:01.
 * E-mail: frank_hon@foxmail.com
 */
class MainAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): BaseFragment {
        return when (position) {
            0 -> SongFragment()
            1 -> ArtistFragment()
            else -> AlbumFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Songs"
            1 -> "Artists"
            else -> "Albums"
        }
    }
}