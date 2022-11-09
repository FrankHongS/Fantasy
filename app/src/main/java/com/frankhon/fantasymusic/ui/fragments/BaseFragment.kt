package com.frankhon.fantasymusic.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.ui.view.panel.SimpleBottomControlPanel

/**
 * Created by Frank Hon on 2020-04-19 20:32.
 * E-mail: frank_hon@foxmail.com
 */
const val KEY_FRAGMENT_NAME = "FRAGMENT_NAME"

open class BaseFragment : Fragment() {

    // Fragment是否已经实例化
    protected var isInstantiated = false

    private var controlPanel: SimpleBottomControlPanel? = null

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        controlPanel = view.findViewById(R.id.sbcp_control_panel)
    }

    override fun onResume() {
        super.onResume()
        isInstantiated = true
        controlPanel?.doOnResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        isInstantiated = false
    }
}