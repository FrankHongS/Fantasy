package com.frankhon.fantasymusic.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewStub
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.ui.view.panel.SimpleBottomControlPanel

/**
 * Created by Frank Hon on 2020-04-19 20:32.
 * E-mail: frank_hon@foxmail.com
 */
open class BaseFragment : Fragment() {

    // Fragment是否已经实例化
    protected var isInstantiated = false

    private var controlPanel: SimpleBottomControlPanel? = null
    private var emptyViewStub: ViewStub? = null
    private var emptyPlaceholder: View? = null

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        controlPanel = view.findViewById(R.id.sbcp_control_panel)
        emptyViewStub = view.findViewById(R.id.stub_empty_placeholder)
    }

    override fun onStart() {
        super.onStart()
        controlPanel?.connectAudioPlayer()
        isInstantiated = true
    }

    override fun onStop() {
        super.onStop()
        controlPanel?.disconnectAudioPlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        emptyPlaceholder = null
    }

    override fun onDestroy() {
        super.onDestroy()
        isInstantiated = false
    }

    protected fun inflateEmptyPlaceholder(isShowing: Boolean) {
        if (isShowing) {
            if (emptyPlaceholder == null) {
                emptyPlaceholder = emptyViewStub?.inflate()
            } else {
                emptyPlaceholder!!.isVisible = true
            }
        } else {
            emptyPlaceholder?.isVisible = false
        }
    }
}