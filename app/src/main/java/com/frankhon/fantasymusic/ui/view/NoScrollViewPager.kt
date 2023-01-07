package com.frankhon.fantasymusic.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

/**
 * Created by Frank Hon on 2020-05-28 02:16.
 * E-mail: frank_hon@foxmail.com
 * 切换Fragment时屏蔽滚动动画
 */
class NoScrollViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ViewPager(context, attrs) {

    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(item, false)
    }

}