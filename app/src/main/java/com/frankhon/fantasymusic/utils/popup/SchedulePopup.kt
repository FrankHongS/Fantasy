package com.frankhon.fantasymusic.utils.popup

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.utils.*

/**
 * Created by Frank Hon on 2022/11/3 5:44 下午.
 * E-mail: frank_hon@foxmail.com
 */

/**
 * @receiver anchorView
 */
fun View.showSchedulePopup() {
    ListPopupWindow(context)
        .run {
            val view = this@showSchedulePopup
            anchorView = view
            setAdapter(SchedulePopupAdapter(this))
            setDropDownGravity(Gravity.START)
            width = 180.dp
            isModal = true
            horizontalOffset = view.width / 2
            setBackgroundDrawable(drawable(R.drawable.bg_common_popup))
            show()
        }
}

private class SchedulePopupAdapter(private val popupWindow: ListPopupWindow) : BaseAdapter() {

    private val contentList = getStringArray(R.array.item_schedule_time_content)
    private val timeList = getIntegerArray(R.array.item_schedule_time)

    override fun getCount(): Int {
        return contentList.size
    }

    override fun getItem(position: Int): String {
        return contentList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_schedule_popup, parent, false)
        (view as? TextView)?.run {
            text = getItem(position)
            if (position == count - 1) {
                setTextColor(color(R.color.highlightText))
            }
            setOnClickListener {
                popupWindow.dismiss()
                if (position == count - 1) {
                    showToast(R.string.schedule_cancel)
                    removeSchedulePause()
                } else {
                    val minutes = timeList[position]
                    showToast(String.format(getString(R.string.schedule_start), minutes))
                    schedulePause(minutes)
                }
            }
        }
        return view
    }
}