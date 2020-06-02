package com.frankhon.fantasymusic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.frankhon.fantasymusic.R

/**
 * Created by Frank Hon on 2020-04-19 20:19.
 * E-mail: frank_hon@foxmail.com
 */
class SongFragment(name: String) : BaseFragment(name) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_song, container, false)
    }


}