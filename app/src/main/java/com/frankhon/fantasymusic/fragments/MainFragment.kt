package com.frankhon.fantasymusic.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
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
        setHasOptionsMenu(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.searchFragment)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}