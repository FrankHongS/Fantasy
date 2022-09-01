package com.frankhon.fantasymusic.utils

import android.widget.Toast
import com.frankhon.fantasymusic.application.Fantasy

/**
 * Created by Frank Hon on 2022/6/14 10:32 下午.
 * E-mail: frank_hon@foxmail.com
 */
object ToastUtil {

    @JvmStatic
    fun showToast(content: String) {
        Toast.makeText(Fantasy.getAppContext(), content, Toast.LENGTH_SHORT).show()
    }

}