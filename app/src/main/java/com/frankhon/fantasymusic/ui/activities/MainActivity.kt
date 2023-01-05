package com.frankhon.fantasymusic.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.ui.fragments.main.MainFragment
import com.hon.mylogger.MyLogger
import com.permissionx.guolindev.PermissionX

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
        requestPermissions()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 点击下载完成的通知，data中包含的数据为content://com.android.providers.downloads.documents/document/1082
        MyLogger.d("onNewIntent: ${intent?.action}, ${intent?.data}")
    }

    /**
     * 有前台服务时，直接杀掉进程，会执行该回调
     */
    override fun onDestroy() {
        MyLogger.d("onDestroy: ")
        super.onDestroy()
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

    private fun requestPermissions() {
        PermissionX.init(this)
            .permissions(Manifest.permission.BLUETOOTH_CONNECT)
            .onExplainRequestReason { scope, permissions ->
                scope.showRequestReasonDialog(
                    permissions, "We need the permission to use bluetooth headset properly",
                    "OK", "Cancel"
                )
            }
            .request { allGranted, _, _ ->

            }
    }

}
