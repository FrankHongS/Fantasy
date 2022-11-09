package com.frankhon.fantasymusic.utils.popup

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.lifecycle.LifecycleCoroutineScope
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.application.ServiceLocator
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.utils.*
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.event.SongDeleteEvent
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 * Created by Frank Hon on 2022/10/28 1:01 下午.
 * E-mail: frank_hon@foxmail.com
 */

/**
 * @receiver anchorView
 */
fun View.showMorePopup(song: SimpleSong, scope: LifecycleCoroutineScope) {
    ListPopupWindow(context)
        .run {
            val view = this@showMorePopup
            anchorView = view
            setAdapter(MorePopupAdapter(this, song, scope))
            setDropDownGravity(Gravity.START)
            width = 160.dp
            isModal = true
            verticalOffset = -view.height / 2
            horizontalOffset = (-120).dp
            setBackgroundDrawable(drawable(R.drawable.bg_common_popup))
            show()
        }
}

private class MorePopupAdapter(
    private val popupWindow: ListPopupWindow,
    private val song: SimpleSong,
    private val scope: LifecycleCoroutineScope
) : BaseAdapter() {

    private val actionArray = getStringArray(R.array.action_song_more)
    private val iconList = listOf(
        R.drawable.ic_action_delete_song, R.drawable.ic_action_playlist_add
    )

    override fun getCount(): Int {
        return actionArray.size
    }

    override fun getItem(position: Int): String {
        return actionArray[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_popup_more, parent, false)
        if (view.tag == null) {
            view.tag = SongMorePopupViewHolder(view, song, popupWindow, scope)
        }
        val viewHolder = view.tag as SongMorePopupViewHolder
        viewHolder.bindView(iconList[position], getItem(position), position)
        return view
    }
}

private class SongMorePopupViewHolder(
    private val view: View,
    private val song: SimpleSong,
    private val popupWindow: ListPopupWindow,
    private val scope: LifecycleCoroutineScope
) {

    private val actionImage = view.findViewById<ImageView>(R.id.iv_more_action)
    private val actionText = view.findViewById<TextView>(R.id.tv_more_action)

    fun bindView(iconRes: Int, content: String, position: Int) {
        actionImage.setImageResource(iconRes)
        actionText.text = content
        view.setOnClickListener { _ ->
            popupWindow.dismiss()
            when (position) {
                0 -> deleteSong()
                1 -> addIntoPlaylist()
            }
        }
    }

    private fun deleteSong() {
        song.let {
            if (it.canDelete) {
                scope.launch {
                    val musicRepository = ServiceLocator.provideMusicRepository()
                    //将歌曲从播放列表中删除
                    AudioPlayerManager.removeSongFromPlayList(it)
                    //将歌曲从数据库中删除
                    musicRepository.deleteSong(it)
                    //通知UI更新
                    EventBus.getDefault().post(SongDeleteEvent(it))
                }
            } else {
                showToast(R.string.unable_to_delete)
            }
        }
    }

    private fun addIntoPlaylist() {
        AudioPlayerManager.addIntoPlaylist(song)
        showToast(String.format(getString(R.string.add_into_playlist), song.name))
    }

}