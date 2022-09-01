package com.frankhon.fantasymusic.activities.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.frankhon.fantasymusic.fragments.BaseFragment

/**
 * Created by Frank Hon on 2020-04-19 20:01.
 * E-mail: frank_hon@foxmail.com
 */
class MainAdapter(fm: FragmentManager, private val fragments: List<BaseFragment>) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragments[position].name
    }
}