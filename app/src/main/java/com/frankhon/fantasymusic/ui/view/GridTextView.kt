package com.frankhon.fantasymusic.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.get
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.dp
import com.hon.mylogger.MyLogger

/**
 * 密集排列的标签View
 *
 * note: 单个标签的宽度不能超过父View的宽度
 *
 * requestLayout() 会回调onMeasure() 以及 onLayout()
 * invalidate() 会回调onDraw()
 *
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

    /**
     * 每行中元素的最大个数
     */
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
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        MyLogger.d(
            "onMeasure: width = $width widthMode = ${
                when (widthMode) {
                    MeasureSpec.EXACTLY -> "EXACTLY"
                    MeasureSpec.AT_MOST -> "AT_MOST"
                    MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
                    else -> ""
                }
            }"
        )
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        MyLogger.d("onSizeChanged: ")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        MyLogger.d("onLayout: $changed")
        val itemCountList = getItemCountList()
        var tempPaddingLeft = paddingLeft
        var tempPaddingTop = paddingTop
        var rowCount = 0
        var start = -1
        for (i in 0 until childCount) {
            val view = get(i)
            view.layout(
                tempPaddingLeft + marginHorizontal,
                tempPaddingTop + marginVertical,
                tempPaddingLeft + view.measuredWidth + marginHorizontal,
                tempPaddingTop + view.measuredHeight + marginVertical
            )
            if (i - start != itemCountList[rowCount]) {
                tempPaddingLeft += view.measuredWidth + marginHorizontal
            } else {
                rowCount++
                start = i
                tempPaddingLeft = paddingLeft
                tempPaddingTop += view.measuredHeight + marginVertical
            }
        }
    }

    /**
     * @return itemCountList, 每个索引表示相应的行，值为对应行中元素的个数
     */
    private fun getItemCountList(): List<Int> {
        val itemCountList = mutableListOf<Int>()
        var tempWidth = 0
        var start = -1
        for (i in 0 until childCount) {
            val child = get(i)
            tempWidth += child.measuredWidth + marginHorizontal
            if (i - start <= spanCount) {
                if (tempWidth > measuredWidth) {
                    itemCountList.add(i - start - 1)
                    start = i - 1
                    tempWidth = child.measuredWidth + marginHorizontal
                } else if (i - start == spanCount) {
                    itemCountList.add(spanCount)
                    start = i
                    tempWidth = 0
                }
            }
        }
        val itemCount = itemCountList.fold(0) { acc, ele -> acc + ele }
        if (itemCount < childCount) {
            itemCountList.add(childCount - itemCount)
        }
        return itemCountList
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
    }
}