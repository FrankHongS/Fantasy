package com.frankhon.fantasymusic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Created by Frank Hon on 2020-04-19 20:32.
 * E-mail: frank_hon@foxmail.com
 */
open class BaseFragment(val name: String = "") : Fragment() {

    protected var isInstantiate = false

    override fun onResume() {
        super.onResume()
        isInstantiate = true
    }
}