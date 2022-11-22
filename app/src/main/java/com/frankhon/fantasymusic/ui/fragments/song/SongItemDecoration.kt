package com.frankhon.fantasymusic.ui.fragments.song

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.frankhon.customview.paging.PagingAdapter
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.color
import com.frankhon.fantasymusic.utils.dp

/**
 * Created by Frank Hon on 2022/11/11 5:01 下午.
 * E-mail: frank_hon@foxmail.com
 */
class SongItemDecoration : RecyclerView.ItemDecoration() {

    private val defaultStrokeWidth = 2.dp.toFloat()
    private val defaultMargin = 18.dp.toFloat()

    private val linePaint by lazy {
        Paint().apply {
            color = color(R.color.colorBackground)
            style = Paint.Style.FILL
            isAntiAlias = true
            strokeWidth = defaultStrokeWidth
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val count = parent.childCount
        val dataSize = (parent.adapter as? PagingAdapter<*>)?.getDataSize() ?: 0
        for (pos in 0 until count) {
            val child = parent[pos]
            val adapterPos = parent.getChildAdapterPosition(child)
            if (adapterPos == 0 || adapterPos == dataSize) {
                continue
            }
            c.drawLine(
                child.left.toFloat() + defaultMargin,
                child.bottom.toFloat(),
                child.right.toFloat() - defaultMargin,
                child.bottom.toFloat(),
                linePaint
            )
        }
    }

}