package com.frankhon.fantasymusic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.adapter.MainAdapter
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by Frank Hon on 2020-04-14 23:41.
 * E-mail: frank_hon@foxmail.com
 */
class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val targetActivity = activity as AppCompatActivity
        targetActivity.setSupportActionBar(toolbar_main)
        val actionBar = targetActivity.supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        initViewPager()
        tl_main.setupWithViewPager(vp_main)
    }

    private fun initViewPager() {
        val fragments = arrayListOf(
            SongFragment("Songs"),
            ArtistFragment("Artists"),
            AlbumFragment("Albums")
        )
        val mainAdapter = fragmentManager?.let { MainAdapter(it, fragments) }
        vp_main.adapter = mainAdapter
    }

}