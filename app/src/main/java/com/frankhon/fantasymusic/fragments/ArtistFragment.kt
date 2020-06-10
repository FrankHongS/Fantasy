package com.frankhon.fantasymusic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.frankhon.fantasymusic.R

/**
 * Created by Frank Hon on 2020-04-19 20:19.
 * E-mail: frank_hon@foxmail.com
 */
class ArtistFragment : BaseFragment {

    //保留无参的构造方法，横竖屏切换时或系统回收重建时会调用该方法；否则会crash
    constructor() : super()

    constructor(name: String) : super(name)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_artist, container, false)
    }
}