package com.frankhon.fantasymusic.utils

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Created by Frank Hon on 2022/9/9 6:31 下午.
 * E-mail: frank_hon@foxmail.com
 */

fun <T : ViewModel, A> singleArgSavedStateViewModelFactory(constructor: (A, SavedStateHandle) -> T)
        : (A, SavedStateRegistryOwner, Bundle?) -> AbstractSavedStateViewModelFactory {
    return { arg: A, owner: SavedStateRegistryOwner,
             defaultArgs: Bundle? ->
        object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return constructor(arg, handle) as T
            }
        }
    }
}