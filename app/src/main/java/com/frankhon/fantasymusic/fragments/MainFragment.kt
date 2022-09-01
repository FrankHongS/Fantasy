package com.frankhon.fantasymusic.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.activities.adapter.MainAdapter
import com.frankhon.fantasymusic.fragments.song.SongFragment
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by Frank Hon on 2020-04-14 23:41.
 * E-mail: frank_hon@foxmail.com
 *
 * 使用navigation，navigate到新的fragment时，旧的fragment的view会被销毁，但实例会保留。
 * 针对这个issue，目前暂时通过全局变量mainView和标识位isInstantiate来避免重复创建view，否则FragmentViewPager无法正常创建
 */
class MainFragment : BaseFragment() {

    private var mainView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MyLogger.d("onCreateView")
        if (mainView == null) {
            mainView = inflater.inflate(R.layout.fragment_main, container, false)
        }
        return mainView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isInstantiate) {
            setHasOptionsMenu(true)
            val targetActivity = activity as AppCompatActivity
            targetActivity.setSupportActionBar(toolbar_main)
            val actionBar = targetActivity.supportActionBar
            actionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
            actionBar?.setDisplayHomeAsUpEnabled(true)

            initViewPager()
            tl_main.setupWithViewPager(vp_main)
        }
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