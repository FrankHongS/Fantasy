package com.frankhon.fantasymusic.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.dp
import com.hon.mylogger.MyLogger
import kotlin.math.min

/**
 * Created by Frank Hon on 2022/9/18 12:42 下午.
 * E-mail: frank_hon@foxmail.com
 *
 * 圆角图片
 */
class RoundRectImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_RADIUS = 16
    }

    private val imagePath by lazy { Path() }
    private var roundRectRadius: Float

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RoundRectImageView)
        roundRectRadius =
            a.getDimension(R.styleable.RoundRectImageView_radius, DEFAULT_RADIUS.dp.toFloat())
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

//        when (widthMode) {
//            //size可以超过parent
//            MeasureSpec.UNSPECIFIED -> MyLogger.d("UNSPECIFIED: $widthSize")
//            MeasureSpec.EXACTLY -> MyLogger.d("EXACTLY: $widthSize")
//            //size最大为parent
//            MeasureSpec.AT_MOST -> MyLogger.d("AT_MOST: $widthSize")
//        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        imagePath.addRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            roundRectRadius,
            roundRectRadius,
            Path.Direction.CCW
        )
        canvas.clipPath(imagePath)
        super.onDraw(canvas)
    }

}