package com.frankhon.fantasymusic.fragments

import androidx.fragment.app.Fragment

/**
 * Created by Frank Hon on 2020-04-19 20:32.
 * E-mail: frank_hon@foxmail.com
 */
const val KEY_FRAGMENT_NAME = "FRAGMENT_NAME"

open class BaseFragment : Fragment() {

    protected var isInstantiate = false

    override fun onResume() {
        super.onResume()
        isInstantiate = true
    }
}