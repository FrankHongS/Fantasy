package com.frankhon.fantasymusic.application

import android.app.Activity
import java.util.LinkedList

/**
 * Activity返回栈管理类
 * Created by shuaihua_a on 2023/2/11 14:56.
 * E-mail: hongshuaihua
 */
object ActivityStackManager {

    private val stack = LinkedList<Activity>()

    val size: Int
        get() = stack.size

    fun push(activity: Activity) {
        stack.push(activity)
    }

    fun pop(): Activity? {
        return if (stack.isNotEmpty()) {
            stack.pop()
        } else {
            null
        }
    }

    fun isEmpty() = size == 0

}