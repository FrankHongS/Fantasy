package com.frankhon.fantasymusic.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.frankhon.fantasymusic.R

/**
 * Created by Frank Hon on 2020-04-19 20:20.
 * E-mail: frank_hon@foxmail.com
 */
class AlbumFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }
}