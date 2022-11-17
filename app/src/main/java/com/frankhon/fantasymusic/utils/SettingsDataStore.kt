package com.frankhon.fantasymusic.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.collect

/**
 * Created by Frank Hon on 2022/11/16 8:35 下午.
 * E-mail: frank_hon@foxmail.com
 */

private const val SETTING_PREFERENCES_NAME = "setting_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(SETTING_PREFERENCES_NAME)

val KEY_NOTIFICATION_STYLE = intPreferencesKey("key_notification_style")

fun readDataStore() = appContext.dataStore.data