package com.frankhon.fantasymusic.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.AbsSavedState
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

    private lateinit var bottomView: View
    private val touchPoint = Array(2) { 0f }

    // 0 collapsed; 1 expanded; 2 dragging;
    private var panelState = 0
    private var allowDragging = true

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
                panelState = when (top) {
                    height - PANEL_HEIGHT.dp -> 0
                    height - changedView.height -> 1
                    else -> 2
                }
            }

            override fun onViewDragStateChanged(state: Int) {

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

    @SuppressLint("ClickableViewAccessibility")
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
                    setOnTouchListener { _, _ -> true }
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        when (panelState) {
            0 -> {
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
            }
            1 -> {
                children.forEachIndexed { index, child ->
                    if (index == 0) {
                        child.layout(
                            paddingLeft,
                            paddingTop,
                            paddingLeft + child.measuredWidth,
                            paddingTop + child.measuredHeight
                        )
                    } else if (index == 1) {
                        child.layout(
                            paddingLeft,
                            measuredHeight - child.measuredHeight,
                            paddingLeft + child.measuredWidth,
                            measuredHeight
                        )
                    }
                }
            }
            2 -> {
                children.forEach {
                    it.layout(it.left, it.top, it.right, it.bottom)
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!allowDragging) {
            return false
        }
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

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val state = SavedState(superState)
        state.panelState = panelState
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        (state as? SavedState)?.let {
            panelState = it.panelState
        }
    }

    fun expand(): Boolean {
        if (panelState != 0) {
            return false
        }
        if (viewDragHelper.smoothSlideViewTo(bottomView, 0, height - bottomView.height)) {
            ViewCompat.postInvalidateOnAnimation(this)
            return true
        }
        return false
    }

    fun collapse(): Boolean {
        if (panelState == 0) {
            return false
        }
        if (::bottomView.isInitialized && viewDragHelper.smoothSlideViewTo(
                bottomView,
                0,
                height - PANEL_HEIGHT.dp
            )
        ) {
            ViewCompat.postInvalidateOnAnimation(this)
            return true
        }
        return false
    }

    fun setAllowDragging(allowDragging: Boolean) {
        this.allowDragging = allowDragging
    }

    private class SavedState : AbsSavedState {
        var panelState = 0

        constructor(source: Parcelable?) : super(source)

        constructor(parcel: Parcel) : super(parcel) {
            panelState = parcel.readInt()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(panelState)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}