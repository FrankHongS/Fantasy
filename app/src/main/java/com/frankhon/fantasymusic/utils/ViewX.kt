package com.frankhon.fantasymusic.utils

import android.widget.TextView
import androidx.core.view.isVisible

/**
 * Created by Frank Hon on 2022/11/10 9:09 上午.
 * E-mail: frank_hon@foxmail.com
 */

fun TextView.safeSetText(content: String?) {
    if (content.isNullOrEmpty()) {
        isVisible = false
    } else {
        isVisible = true
        text = content
    }
}