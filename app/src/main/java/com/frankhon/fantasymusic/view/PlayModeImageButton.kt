package com.frankhon.fantasymusic.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.frankhon.fantasymusic.R


/**
 * Created by Frank Hon on 2022/6/16 7:17 下午.
 * E-mail: frank_hon@foxmail.com
 */
class PlayModeImageButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    enum class PlayMode {
        SHUFFLE,
        LOOP_LIST,
        LOOP_SINGLE
    }

    var playMode = PlayMode.LOOP_LIST

    private var position = 0
    private val size = PlayMode.values().size
    private var icons = listOf(
        R.drawable.ic_loop_list,
        R.drawable.shuffle,
        R.drawable.ic_loop_single
    )
    private var observer: ((PlayMode) -> Unit)? = null

    init {
        setBackgroundResource(icons[position])
        setOnClickListener {
            position++
            position %= size
            playMode = getModeByPosition(position)
            setBackgroundResource(icons[position])
            observer?.invoke(playMode)
        }
    }

    fun setIcons(icons: List<Int>) {
        if (icons.size != size) {
            throw RuntimeException("Icons size smaller than play mode size")
        }
        this.icons = icons
    }

    fun setObserver(observer: ((PlayMode) -> Unit)?) {
        this.observer = observer
    }

    private fun getModeByPosition(position: Int): PlayMode {
        return when (position) {
            0 -> PlayMode.LOOP_LIST
            1 -> PlayMode.SHUFFLE
            2 -> PlayMode.LOOP_SINGLE
            else -> PlayMode.LOOP_LIST
        }
    }
}