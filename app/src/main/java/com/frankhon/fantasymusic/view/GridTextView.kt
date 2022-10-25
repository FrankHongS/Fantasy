package com.frankhon.fantasymusic.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.get
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.dp
import kotlin.math.max

/**
 * 密集排列的标签View
 * Created by shuaihua on 2021/11/4 3:21 下午
 * Email: shuaihua@staff.sina.com.cn
 */
class GridTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var marginHorizontal = 10.dp
    private var marginVertical = 10.dp
    private var layoutId = 0
    private var spanCount = 4

    init {
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.GridTextView)
        layoutId = a.getResourceId(R.styleable.GridTextView_itemLayout, 0)
        marginHorizontal =
            a.getDimensionPixelSize(R.styleable.GridTextView_marginHorizontal, marginHorizontal)
        marginVertical =
            a.getDimensionPixelSize(R.styleable.GridTextView_marginVertical, marginVertical)
        spanCount = a.getInt(R.styleable.GridTextView_spanCount, spanCount)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        var maxWidth = 0
        var maxHeight = 0
        var tempWidth = 0
        for (i in 0 until childCount) {
            val child = get(i)
            if (i % spanCount == 0) {
                maxWidth = max(maxWidth, tempWidth)
                tempWidth = 0
                maxHeight += child.measuredHeight + marginVertical
            }
            tempWidth += child.measuredWidth + marginHorizontal
        }
        maxWidth = max(maxWidth, tempWidth)
        setMeasuredDimension(
            maxWidth + marginHorizontal,
            maxHeight + marginVertical
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var tempPaddingLeft = paddingLeft
        var tempPaddingTop = paddingTop
        for (i in 0 until childCount) {
            val view = get(i)
            view.layout(
                tempPaddingLeft + marginHorizontal,
                tempPaddingTop + marginVertical,
                tempPaddingLeft + view.measuredWidth + marginHorizontal,
                tempPaddingTop + view.measuredHeight + marginVertical
            )
            if (i % spanCount != spanCount - 1) {
                tempPaddingLeft += view.measuredWidth + marginHorizontal
            } else {
                tempPaddingLeft = paddingLeft
                tempPaddingTop += view.measuredHeight + marginVertical
            }
        }
    }

    fun setData(strList: List<String>, listener: ((String) -> Unit)? = null) {
        strList.forEach {
            val textView = LayoutInflater.from(context).inflate(layoutId, null)
                    as? TextView
            textView?.run {
                addView(this.apply {
                    text = it
                    setOnClickListener { _ ->
                        listener?.invoke(it)
                    }
                })
            }
        }
        requestLayout()
    }
}