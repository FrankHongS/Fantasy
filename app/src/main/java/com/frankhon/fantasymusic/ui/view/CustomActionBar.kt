package com.frankhon.fantasymusic.ui.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.dp
import kotlinx.android.synthetic.main.layout_custom_action_bar.view.*

/**
 * Created by Frank Hon on 2022/9/18 7:09 下午.
 * E-mail: frank_hon@foxmail.com
 */
class CustomActionBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_HEIGHT = 56
    }

    private var onBackClickListener: ((View) -> Unit)? = null

    init {
        View.inflate(context, R.layout.layout_custom_action_bar, this)
        setBackgroundResource(R.color.colorActionBar)
        iv_back.setOnClickListener {
            onBackClickListener?.invoke(it)?: kotlin.run {
                (context as? Activity)?.onBackPressed()
            }
        }
        ViewCompat.setElevation(this, 4.dp.toFloat())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode != MeasureSpec.EXACTLY) {
            super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(DEFAULT_HEIGHT.dp, MeasureSpec.EXACTLY)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setOnBackClickListener(listener: (View) -> Unit) {
        this.onBackClickListener = listener
    }

    fun setActionBarTitle(@StringRes resId: Int) {
        tv_title.setText(resId)
    }
}