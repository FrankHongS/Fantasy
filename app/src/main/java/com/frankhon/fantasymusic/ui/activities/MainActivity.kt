package com.frankhon.fantasymusic.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.ui.fragments.main.MainFragment
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.hon.mylogger.MyLogger

class MainActivity : AppCompatActivity() {

    private var fragment: MainFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MyLogger.d("onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, MainFragment())
                .commit()
        }
    }

    /**
     * 有前台服务时，直接杀掉进程，会执行该回调
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
        fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? MainFragment
        //返回之前先收起控制栏
        fragment?.takeIf { it.isVisible && (it.closeDrawer() || it.collapsePanel()) }
            ?.let { return }
        super.onBackPressed()
    }
}
