package com.frankhon.fantasymusic.utils

import android.widget.Toast
import androidx.annotation.StringRes
import com.frankhon.fantasymusic.application.Fantasy

/**
 * Created by Frank Hon on 2022/6/14 10:32 下午.
 * E-mail: frank_hon@foxmail.com
 */

fun showToast(content: String) {
    if (content.isEmpty()) {
        return
    }
    Toast.makeText(Fantasy.getAppContext(), content, Toast.LENGTH_SHORT).show()
}

fun showToast(@StringRes resId: Int) {
    Toast.makeText(Fantasy.getAppContext(), resId, Toast.LENGTH_SHORT).show()
}