package com.frankhon.fantasymusic.utils

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.frankhon.fantasymusic.application.ActivityStackManager
import com.frankhon.fantasymusic.ui.activities.MainActivity
import java.util.*

/**
 * Created by Frank Hon on 2022/6/14 11:18 下午.
 * E-mail: frank_hon@foxmail.com
 */

/**
 * 扩展属性，DSL
 */
val Int.dp: Int
    get() {
        return dp2px(this)
    }

fun String?.matchesUri(): Boolean {
    return this?.matches(Regex("^(https?://|file://).*")) ?: false
}

fun <T> MutableList<T>.setData(data: List<T>) {
    clear()
    addAll(data)
}

fun Context.drawable(@DrawableRes resId: Int) = ContextCompat.getDrawable(this, resId)

fun Context.color(@ColorRes resId: Int) = ContextCompat.getColor(this, resId)

fun View.drawable(@DrawableRes resId: Int) = ContextCompat.getDrawable(context, resId)

fun View.color(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)

inline fun <reified T : Activity> Context.navigate() {
    startActivity(Intent(this, T::class.java))
}

inline fun <reified T : Activity> Context.navigateInSingleTop() {
    startActivity(Intent(this, T::class.java)
        .apply {
            flags = FLAG_ACTIVITY_SINGLE_TOP
        })
}

inline fun <reified T : Activity> Context.navigateWithTransitions() {
    (this as? Activity)?.let {
        startActivity(
            Intent(it, T::class.java),
            ActivityOptions.makeSceneTransitionAnimation(it).toBundle()
        )
    }
}

/**
 * 当非MainActivity回退时，返回栈没有其他Activity时，返回到MainActivity
 */
fun Context.backToMain() {
    if (ActivityStackManager.size == 1) {
        startActivity(Intent(this, MainActivity::class.java)
            .apply {
                // 清空栈中该Activity及其以上Activity，并重新创建该Activity
                flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP
            })
    }
}

/**
 * 获取当前栈顶的Fragment
 */
inline fun <reified T : Fragment> NavHostFragment.getSpecifiedFragment(): T? {
    return childFragmentManager.fragments.first() as? T
}

@Suppress("DEPRECATION")
fun Activity.toImmersiveMode() {
    val decorView = window.decorView
    val option =
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
//                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    decorView.systemUiVisibility = option
    window.run {
        statusBarColor = Color.TRANSPARENT
        navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 有些设备导航栏无法变透明，需额外设置这个
            isNavigationBarContrastEnforced = false
        }
    }
}