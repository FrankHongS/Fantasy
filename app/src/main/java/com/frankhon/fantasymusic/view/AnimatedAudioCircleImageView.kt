package com.frankhon.fantasymusic.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.frankhon.fantasymusic.R
import kotlin.math.min

/**
 * 圆形ImageView+旋转+进度条
 *
 * @author shuaihua
 * @since 2021/8/20 10:44 上午
 */
class AnimatedAudioCircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    companion object {
        private val SCALE_TYPE = ScaleType.CENTER_CROP
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        private const val COLOR_DRAWABLE_DIMENSION = 2
        private const val DEFAULT_BORDER_WIDTH = 6
        private val DEFAULT_BORDER_COLOR = Color.parseColor("#FE350E")
    }

    private lateinit var bitmapPaint: Paint
    private lateinit var borderPaint: Paint
    private var mBorderWidth: Int
    private val mBorderDayColor: Int
    private var mBitmap: Bitmap? = null
    private var mDrawableRadius = 0f
    private var progressRotatedDegree = 0f

    private lateinit var arcRect: RectF
    private lateinit var drawableRect: RectF
    private lateinit var borderRect: RectF
    private lateinit var bitmapShaderMatrix: Matrix
    private lateinit var imageAnimator: ValueAnimator

    var borderWidth: Int
        get() = mBorderWidth
        set(borderWidth) {
            if (borderWidth != mBorderWidth) {
                mBorderWidth = borderWidth
                invalidate()
            }
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedAudioCircleImageView)
        mBorderWidth = a.getDimensionPixelSize(
            R.styleable.AnimatedAudioCircleImageView_borderWidth,
            DEFAULT_BORDER_WIDTH
        )
        mBorderDayColor =
            a.getColor(R.styleable.AnimatedAudioCircleImageView_borderColor, DEFAULT_BORDER_COLOR)
        a.recycle()
        initBorderPaint()
        initBitmapPaint()
        initAnimator()
        initRect()
    }

    private fun initBorderPaint() {
        borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = mBorderDayColor
            strokeWidth = mBorderWidth.toFloat()
        }
    }

    private fun initBitmapPaint() {
        bitmapPaint = Paint().apply { isAntiAlias = true }
        bitmapShaderMatrix = Matrix()
    }

    private fun initAnimator() {
        imageAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = (5 * 1000).toLong()
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animation: ValueAnimator ->
                val imageRotatedDegree = animation.animatedValue as Float
                updateBitmapShaderMatrix(imageRotatedDegree)
                invalidate()
            }
        }
    }

    private fun initRect() {
        arcRect = RectF()
        drawableRect = RectF()
        borderRect = RectF()
    }

    override fun getScaleType(): ScaleType {
        return SCALE_TYPE
    }

    override fun setScaleType(scaleType: ScaleType) {
        require(scaleType == SCALE_TYPE) {
            String.format(
                "ScaleType %s not supported.", scaleType
            )
        }
    }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        require(!adjustViewBounds) { "adjustViewBounds not supported." }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBitmapShaderMatrix(0f)
    }

    override fun onDraw(canvas: Canvas) {
        if (drawable == null) {
            return
        }
        canvas.drawCircle(width / 2f, height / 2f, mDrawableRadius, bitmapPaint)
        if (mBorderWidth != 0) {
            canvas.drawArc(
                arcRect.apply {
                    left = mBorderWidth / 2f
                    top = mBorderWidth / 2f
                    right = width - mBorderWidth / 2f
                    bottom = height - mBorderWidth / 2f
                },
                270f,
                progressRotatedDegree,
                false,
                borderPaint
            )
        }
    }

    override fun setImageBitmap(bitmap: Bitmap) {
        super.setImageBitmap(bitmap)
        mBitmap = bitmap
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        drawable?.let {
            mBitmap = getBitmapFromDrawable(it)
            resetBitmap()
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        mBitmap = getBitmapFromDrawable(drawable)
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        mBitmap = getBitmapFromDrawable(drawable)
    }

    /**
     * drawable发生改变时，将数据重置
     */
    private fun resetBitmap() {
        updateBitmapShaderMatrix(0f)
    }

    private fun setBorderPaintColor(@ColorInt color: Int) {
        borderPaint.color = color
    }

    private fun setBitmapPaintAlpha(alpha: Int) {
        bitmapPaint.alpha = alpha
    }

    fun startUpdateProgress(progress: Int, total: Int) {
        if (total != 0) {
            progressRotatedDegree = progress * 360f / total
            invalidate()
        }
    }

    fun resetProgress() {
        progressRotatedDegree = 0f
        invalidate()
    }

    fun startRotateAnimator() {
        imageAnimator.start()
    }

    fun cancelRotateAnimator() {
        if (imageAnimator.isRunning) {
            imageAnimator.cancel()
        }
    }

    fun pauseRotateAnimator() {
        imageAnimator.pause()
    }

    fun resumeRotateAnimator() {
        if (imageAnimator.isPaused) {
            imageAnimator.resume()
        } else {
            startRotateAnimator()
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else try {
            val bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(
                    COLOR_DRAWABLE_DIMENSION,
                    COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG
                )
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight, BITMAP_CONFIG
                )
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    private fun updateBitmapShaderMatrix(rotatedDegree: Float) {
        if (mBitmap == null) {
            return
        }
        val bitmapHeight = mBitmap!!.height
        val bitmapWidth = mBitmap!!.width

        // 整个图像的显示区域：即全部的View大小区域。
        borderRect[0f, 0f, width.toFloat()] = height.toFloat()

        // 图片显示的区域：即View的大小区域减去边界的大小。
        drawableRect[mBorderWidth.toFloat(), mBorderWidth.toFloat(), borderRect.width()
                - mBorderWidth] = borderRect.height() - mBorderWidth
        // 图片的半径大小取图片小边。
        mDrawableRadius = min(drawableRect.height() / 2, drawableRect.width() / 2)
        val scale: Float
        var dx = 0f
        var dy = 0f
        if (bitmapWidth * drawableRect.height() > drawableRect.width()
            * bitmapHeight
        ) {
            scale = drawableRect.height() / bitmapHeight.toFloat()
            dx = (drawableRect.width() - bitmapWidth * scale) * 0.5f
        } else {
            scale = drawableRect.width() / bitmapWidth.toFloat()
            dy = (drawableRect.height() - bitmapHeight * scale) * 0.5f
        }
        bitmapShaderMatrix.run {
            reset()
            setScale(scale, scale)
            postTranslate(
                ((dx + 0.5f).toInt() + mBorderWidth).toFloat(), (
                        (dy + 0.5f).toInt() + mBorderWidth).toFloat()
            )
            postRotate(rotatedDegree, width / 2f, height / 2f)
        }
        bitmapPaint.shader = BitmapShader(
            mBitmap!!, Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT
        ).apply { setLocalMatrix(bitmapShaderMatrix) }
    }

}