package com.frankhon.fantasymusic.ui.view

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
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.dimenPixelSize
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

    /**
     * 控制板（panel）状态
     */
    private enum class State {
        COLLAPSED,
        EXPANDED,
        DRAGGING
    }

    private val panelHeight = dimenPixelSize(R.dimen.default_panel_height)

    private lateinit var bottomView: View
    private val touchPoint = Array(2) { 0f }

    private var panelState = State.COLLAPSED
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
                    height - panelHeight -> State.COLLAPSED
                    height - changedView.height -> State.EXPANDED
                    else -> State.DRAGGING
                }
            }

            override fun onViewDragStateChanged(state: Int) {

            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                return if (top <= height - child.height) {
                    height - child.height
                } else if (top >= height - panelHeight) {
                    height - panelHeight
                } else {
                    top
                }
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                val offsetPercent = (height - releasedChild.top - panelHeight) * 1f /
                        (releasedChild.height - panelHeight)
                val finalTop = if (offsetPercent > 0.25f && yvel <= 0) {
                    height - releasedChild.height
                } else {
                    height - panelHeight
                }
                viewDragHelper.settleCapturedViewAt(left, finalTop)
                invalidate()
            }

            override fun getViewVerticalDragRange(child: View): Int {
                return child.height - panelHeight
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
                MeasureSpec.makeMeasureSpec(height - panelHeight, heightMode)
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
                        panelHeight,
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
            State.COLLAPSED -> {
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
            State.EXPANDED -> {
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
            State.DRAGGING -> {
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
        return if (panelState == State.EXPANDED) {
            false
        } else {
            post {
                if (viewDragHelper.smoothSlideViewTo(bottomView, 0, height - bottomView.height)) {
                    ViewCompat.postInvalidateOnAnimation(this)
                }
            }
            true
        }
    }

    fun collapse(): Boolean {
        return if (panelState == State.COLLAPSED) {
            false
        } else {
            post {
                if (viewDragHelper.smoothSlideViewTo(
                        bottomView,
                        0,
                        height - panelHeight
                    )
                ) {
                    ViewCompat.postInvalidateOnAnimation(this)
                }
            }
            true
        }
    }

    fun isAllowDragging(allowDragging: Boolean) {
        this.allowDragging = allowDragging
    }

    fun isCollapsed() = panelState == State.COLLAPSED

    private class SavedState : AbsSavedState {
        var panelState = State.COLLAPSED

        constructor(source: Parcelable?) : super(source)

        constructor(parcel: Parcel) : super(parcel) {
            panelState = parcel.readSerializable() as State
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeSerializable(panelState)
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