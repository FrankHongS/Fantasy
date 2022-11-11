package com.frankhon.fantasymusic.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Frank Hon on 2022/11/11 3:41 下午.
 * E-mail: frank_hon@foxmail.com
 */
abstract class BaseViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    open fun bindView(index: Int, item: T) {}

}