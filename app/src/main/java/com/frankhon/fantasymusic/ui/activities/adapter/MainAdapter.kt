package com.frankhon.fantasymusic.ui.activities.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.frankhon.fantasymusic.ui.fragments.AlbumFragment
import com.frankhon.fantasymusic.ui.fragments.ArtistFragment
import com.frankhon.fantasymusic.ui.fragments.song.SongFragment

/**
 * Created by Frank Hon on 2020-04-19 20:01.
 * E-mail: frank_hon@foxmail.com
 */
@Suppress("DEPRECATION")
class MainAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SongFragment()
            1 -> ArtistFragment()
            else -> AlbumFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Songs"
            1 -> "Artists"
            else -> "Albums"
        }
    }
}