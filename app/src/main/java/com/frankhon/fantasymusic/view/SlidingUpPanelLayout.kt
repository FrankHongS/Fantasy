package com.frankhon.fantasymusic.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.customview.widget.ViewDragHelper
import com.frankhon.fantasymusic.utils.dp
import kotlin.math.abs

/**
 * Created by Frank Hon on 2022/7/18 6:47 下午.
 * E-mail: frank_hon@foxmail.com
 */
class SlidingUpPanelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ViewGroup(context, attrs, defStyle) {

    companion object {
        // unit, dp
        private const val PANEL_HEIGHT = 68
    }

    private var bottomView: View? = null
    private val touchPoint = Array(2) { 0f }

    private var isCollapsed = true

    private val viewDragHelperCallback by lazy {
        object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return bottomView == child
            }

            override fun onViewPositionChanged(
                changedView: View,
                left: Int,
                top: Int,
                dx: Int,
                dy: Int
            ) {
                isCollapsed = top == height - PANEL_HEIGHT.dp
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                return if (top <= height - child.height) {
                    height - child.height
                } else if (top >= height - PANEL_HEIGHT.dp) {
                    height - PANEL_HEIGHT.dp
                } else {
                    top
                }
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                val offsetPercent = (height - releasedChild.top - PANEL_HEIGHT.dp) * 1f /
                        (releasedChild.height - PANEL_HEIGHT.dp)
                val finalTop = if (offsetPercent > 0.25f && yvel <= 0) {
                    height - releasedChild.height
                } else {
                    height - PANEL_HEIGHT.dp
                }
                viewDragHelper.settleCapturedViewAt(left, finalTop)
                invalidate()
            }

            override fun getViewVerticalDragRange(child: View): Int {
                return child.height - PANEL_HEIGHT.dp
            }

        }
    }

    private val viewDragHelper: ViewDragHelper by lazy {
        ViewDragHelper.create(this, 1.0f, viewDragHelperCallback)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (childCount > 0 && heightMode != MeasureSpec.UNSPECIFIED) {
            val height = MeasureSpec.getSize(heightMeasureSpec)
            val topView = getChildAt(0)
            measureChild(
                topView,
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(height - PANEL_HEIGHT.dp, heightMode)
            )
            if (childCount > 1) {
                val bottomView = getChildAt(1)
                val layoutParams = bottomView.layoutParams
                val bottomHeightMeasureSpec = when (layoutParams.height) {
                    LayoutParams.WRAP_CONTENT -> MeasureSpec.makeMeasureSpec(
                        0,
                        MeasureSpec.UNSPECIFIED
                    )
                    LayoutParams.MATCH_PARENT -> MeasureSpec.makeMeasureSpec(
                        PANEL_HEIGHT.dp,
                        MeasureSpec.EXACTLY
                    )
                    else -> MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY)
                }
                bottomView.measure(widthMeasureSpec, bottomHeightMeasureSpec)
                this.bottomView = bottomView.apply {
                    //消费touch事件，防止事件冒泡（bubble）传递给下层view
                    isFocusable = true
                    isClickable = true
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isCollapsed) {
            var tempPaddingTop = paddingTop
            children.forEach {
                it.layout(
                    paddingLeft,
                    tempPaddingTop,
                    paddingLeft + it.measuredWidth,
                    tempPaddingTop + it.measuredHeight
                )
                tempPaddingTop += it.measuredHeight
            }
        } else {
            children.forEach {
                it.layout(it.left, it.top, it.right, it.bottom)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_CANCEL || ev.actionMasked == MotionEvent.ACTION_UP) {
            viewDragHelper.cancel()
            return false
        }
        return viewDragHelper.shouldInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //region 防止水平滑动，panel收起
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchPoint[0] = event.x
                touchPoint[1] = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(event.x - touchPoint[0])
                val deltaY = abs(event.y - touchPoint[1])
                touchPoint[0] = event.x
                touchPoint[1] = event.y
                if (deltaX + 10 >= deltaY) {
                    return false
                }
            }
        }
        //endregion
        viewDragHelper.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }
}