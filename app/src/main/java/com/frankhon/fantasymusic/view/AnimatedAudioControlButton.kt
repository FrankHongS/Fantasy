package com.frankhon.fantasymusic.view

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.AbsSavedState
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.animation.doOnCancel
import androidx.core.view.postDelayed
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.dp

/**
 * Created by Frank Hon on 2022/8/14 12:21 下午.
 * E-mail: frank_hon@foxmail.com
 */
class AnimatedAudioControlButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    //region draw loadingBar
    private val loadingBarPadding = 4.dp.toFloat()
    private val strokePaint by lazy {
        Paint().apply {
            color = Color.parseColor("#795548")
            style = Paint.Style.STROKE
            strokeWidth = 2.dp.toFloat()
            isAntiAlias = true
        }
    }
    private var startAngle = 0f
    private var sweepAngle = 60f
    private val loadingAnimator by lazy {
        AnimatorSet().apply {
            playTogether(
                ValueAnimator.ofFloat(0f, 1f)
                    .apply {
                        duration = 500
                        repeatCount = ValueAnimator.INFINITE
                        interpolator = LinearInterpolator()
                        repeatMode = ValueAnimator.REVERSE
                        addUpdateListener {
                            val animatedValue = it.animatedValue as Float
                            sweepAngle = 60 + animatedValue * 60
                        }
                    },
                ValueAnimator.ofFloat(0f, 1f)
                    .apply {
                        duration = 1000
                        repeatCount = ValueAnimator.INFINITE
                        interpolator = LinearInterpolator()
                        addUpdateListener {
                            val animatedValue = it.animatedValue as Float
                            startAngle = animatedValue * 360
                            invalidate()
                        }
                    }
            )
            doOnCancel {
                invalidate()
            }
        }
    }
    //endregion

    private var playState = ControlButtonState.INITIAL
    private val imageButton: ImageButton

    private var onControlButtonClickListener: ((ControlButtonState) -> Unit)? = null

    init {
        View.inflate(context, R.layout.layout_song_control_button, this)
        imageButton = findViewById<ImageButton>(R.id.btn_song_control).apply {
            setOnClickListener {
                when (playState) {
                    ControlButtonState.PLAYING -> {
                        setPlayState(ControlButtonState.PAUSED)
                    }
                    ControlButtonState.PAUSED -> {
                        setPlayState(ControlButtonState.PLAYING)
                    }
                    else -> {
                        return@setOnClickListener
                    }
                }
                onControlButtonClickListener?.invoke(playState)
            }
        }
        //无背景时，需要调用此方法ViewGroup才会绘制自身，调用onDraw()
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            (imageButton.measuredWidth + 2 * loadingBarPadding + 8.dp).toInt(),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (playState == ControlButtonState.PREPARING) {
            drawLoading(canvas)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val state = SavedState(superState)
        state.playState = playState.name
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(state)
        (state as? SavedState)?.let {
            this.playState = ControlButtonState.valueOf(it.playState)
        }
        switchState(false)
    }

    private fun drawLoading(canvas: Canvas) {
        val centerX = width / 2
        val centerY = height / 2
        val radius = imageButton.width / 2 + loadingBarPadding
        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            startAngle,
            sweepAngle,
            false,
            strokePaint
        )
    }

    private fun switchState(shouldAnimate: Boolean) {
        imageButton.run {
            when (playState) {
                ControlButtonState.PLAYING -> {
                    if (shouldAnimate) {
                        setImageResource(R.drawable.play_to_pause_vector_drawable)
                    } else {
                        setImageResource(R.drawable.ic_pause_song)
                    }
                }
                ControlButtonState.PAUSED -> {
                    if (shouldAnimate) {
                        setImageResource(R.drawable.pause_to_play_vector_drawable)
                    } else {
                        setImageResource(R.drawable.ic_play_song)
                    }
                }
                ControlButtonState.PREPARING -> {
                    imageButton.setImageResource(R.drawable.ic_pause_song)
                    loadingAnimator.start()
                    return
                }
                else -> {
                    return
                }
            }
            (drawable as? AnimatedVectorDrawable)?.start()
        }
    }

    fun setPlayState(state: ControlButtonState) {
        if (state == playState) {
            return
        }
        // 初始状态
        if (state == ControlButtonState.INITIAL) {
            this.playState = state
            imageButton.setImageResource(R.drawable.ic_play_song)
            return
        }
        if (state != ControlButtonState.PREPARING && loadingAnimator.isRunning) {
            postDelayed(500) {
                loadingAnimator.cancel()
                innerSetPlayState(state, false)
            }
        } else {
            innerSetPlayState(state)
        }
    }

    private fun innerSetPlayState(state: ControlButtonState, shouldAnimate: Boolean = true) {
        this.playState = state
        switchState(shouldAnimate)
    }

    fun setOnControlButtonClickListener(listener: (ControlButtonState) -> Unit) {
        this.onControlButtonClickListener = listener
    }

    enum class ControlButtonState {
        INITIAL,
        PLAYING,
        PAUSED,
        PREPARING;
    }

    private class SavedState : AbsSavedState {
        var playState = ""

        constructor(source: Parcelable?) : super(source)

        constructor(parcel: Parcel) : super(parcel) {
            playState = parcel.readString().orEmpty()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeString(playState)
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