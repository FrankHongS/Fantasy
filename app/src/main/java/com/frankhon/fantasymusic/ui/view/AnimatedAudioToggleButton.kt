package com.frankhon.fantasymusic.ui.view

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.annotation.ColorInt
import androidx.core.animation.doOnCancel
import androidx.core.view.postDelayed
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.color
import com.frankhon.fantasymusic.utils.dimenPixelSize
import com.frankhon.fantasymusic.utils.dp

/**
 * Note: width as least 48dp
 *
 * Created by Frank Hon on 2022/8/14 12:21 下午.
 * E-mail: frank_hon@foxmail.com
 */
class AnimatedAudioToggleButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @ColorInt
    private val tintColor: Int

    //region draw loadingBar
    private val loadingBarPadding: Int
    private val strokePaint by lazy {
        Paint().apply {
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
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.AnimatedAudioToggleButton)
        tintColor =
            a.getColor(R.styleable.AnimatedAudioToggleButton_tintColor, color(R.color.colorPrimary))
        loadingBarPadding =
            a.getDimensionPixelSize(
                R.styleable.AnimatedAudioToggleButton_loadingBarPadding,
                dimenPixelSize(R.dimen.dp_4)
            )
        a.recycle()

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
            imageTintList = ColorStateList.valueOf(tintColor)
        }
        strokePaint.color = tintColor
        //无背景时，需要调用此方法ViewGroup才会绘制自身，调用onDraw()
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            measureChildren(widthMeasureSpec, heightMeasureSpec)
            val size = getSize()
            setMeasuredDimension(
                size + paddingStart + paddingEnd,
                size + paddingTop + paddingBottom
            )
        } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY) {
            measureChildren(widthMeasureSpec, heightMeasureSpec)
            val size = getSize()
            setMeasuredDimension(
                size + paddingStart + paddingEnd,
                heightSize
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (playState == ControlButtonState.PREPARING) {
            drawLoading(canvas)
        }
    }

    private fun getSize() =
        imageButton.measuredWidth + 2 * loadingBarPadding + dimenPixelSize(R.dimen.dp_8)

    private fun drawLoading(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = imageButton.width / 2f + loadingBarPadding
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

    fun setPlayState(state: ControlButtonState, shouldAnimate: Boolean = true) {
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
            innerSetPlayState(state, shouldAnimate)
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

}