package com.frankhon.fantasymusic.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.fragments.MainFragment
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var fragment: MainFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MyLogger.d("onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    /**
     * 有前台服务时，直接杀掉金城武，会执行该回调
     */
    override fun onDestroy() {
        MyLogger.d("onDestroy: ")
        super.onDestroy()
        AudioPlayerManager.release()
    }

    /**
     * Android 12 behavior changes:
     * (Lifecycle) Root launcher activities are no longer finished on Back press
     */
    override fun onBackPressed() {
        MyLogger.d("onBackPressed: ")
        //返回之前先收起控制栏
        fragment?.takeIf { it.closeDrawer() || it.collapsePanel() }?.let { return }
        super.onBackPressed()
    }

    private fun initView() {
        val hostFragment = fragment_container.getFragment<NavHostFragment>()
        fragment = hostFragment.childFragmentManager.primaryNavigationFragment as? MainFragment
    }
}
