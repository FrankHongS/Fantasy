package com.frankhon.fantasymusic.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.frankhon.fantasymusic.utils.appContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2022/11/16 8:35 下午.
 * E-mail: frank_hon@foxmail.com
 */

private const val SETTING_PREFERENCES_NAME = "setting_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(SETTING_PREFERENCES_NAME)

val KEY_NOTIFICATION_STYLE = booleanPreferencesKey("key_notification_style")
val KEY_REMOTE_URL = stringPreferencesKey("key_remote_url")

fun readDataStore() = appContext.dataStore.data
val dataStore = appContext.dataStore

private val mainScope by lazy { MainScope() }
fun read(action: (Preferences) -> Unit) {
    mainScope.launch(context = SupervisorJob()) {
        readDataStore().collect(action)
    }
}