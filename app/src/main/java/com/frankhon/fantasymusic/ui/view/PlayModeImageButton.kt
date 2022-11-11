package com.frankhon.fantasymusic.ui.view

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

    enum class State {
        SHUFFLE,
        LOOP_LIST,
        LOOP_SINGLE
    }

    var playMode = State.LOOP_LIST
        set(value) {
            val pos = getPositionByMode(value);
            setImageResource(icons[pos])
            field = value
        }

    // 选中播放模式的位置
    private var position = 0
    private val size = State.values().size
    private var icons = listOf(
        R.drawable.ic_loop_list,
        R.drawable.ic_shuffle,
        R.drawable.ic_loop_single
    )
    private var listener: ((State) -> Unit)? = null

    init {
        setOnClickListener {
            position++
            position %= size
            playMode = getModeByPosition(position)
            listener?.invoke(playMode)
        }
    }

    fun setIcons(icons: List<Int>) {
        if (icons.size != size) {
            throw RuntimeException("Icons size smaller than play mode size")
        }
        this.icons = icons
    }

    fun setPlayModeListener(listener: ((State) -> Unit)?) {
        this.listener = listener
    }

    fun reset() {
        position = 0
        playMode = State.LOOP_LIST
    }

    private fun getModeByPosition(position: Int): State {
        return when (position) {
            0 -> State.LOOP_LIST
            1 -> State.SHUFFLE
            2 -> State.LOOP_SINGLE
            else -> State.LOOP_LIST
        }
    }

    private fun getPositionByMode(playMode: State): Int {
        return when (playMode) {
            State.LOOP_LIST -> 0
            State.SHUFFLE -> 1
            State.LOOP_SINGLE -> 2
        }
    }
}