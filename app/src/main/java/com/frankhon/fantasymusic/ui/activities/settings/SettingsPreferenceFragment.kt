package com.frankhon.fantasymusic.ui.activities.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.data.settings.KEY_NOTIFICATION_STYLE
import com.frankhon.fantasymusic.data.settings.KEY_REMOTE_URL
import com.frankhon.fantasymusic.data.settings.dataStore
import com.frankhon.fantasymusic.data.settings.readDataStore
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.media.notification.sendMediaNotification
import com.frankhon.fantasymusic.utils.BASE_URL
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2022/11/16 10:09 下午.
 * E-mail: frank_hon@foxmail.com
 */
class SettingsPreferenceFragment : PreferenceFragmentCompat(), OnPreferenceChangeListener {

    private var notificationStyle: SwitchPreference? = null
    private var remoteUrl: EditTextPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        initPreferences()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readDataStore().map {
            listOf(
                it[KEY_NOTIFICATION_STYLE] ?: true,
                it[KEY_REMOTE_URL] ?: BASE_URL
            )
        }.asLiveData().observe(viewLifecycleOwner) {
            notificationStyle?.run {
                isChecked = it[0] as Boolean
                summary = if (isChecked) getString(R.string.settings_enable)
                else getString(R.string.settings_unable)
            }
            remoteUrl?.run {
                summary = it[1] as String
            }
        }
    }

    private fun initPreferences() {
        remoteUrl = findPreference<EditTextPreference>("remote_url")?.also {
            it.onPreferenceChangeListener = this
        }
        notificationStyle = findPreference<SwitchPreference>("notification_style")?.also {
            it.onPreferenceChangeListener = this
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        preference.let {
            when (it) {
                remoteUrl -> {
                    val url = "http://$newValue/"
                    it.summary = url
                    edit { preferences ->
                        preferences[KEY_REMOTE_URL] = url
                    }
                    BASE_URL = url
                }
                notificationStyle -> {
                    val isChecked = newValue as Boolean
                    it.summary = if (isChecked) getString(R.string.settings_enable)
                    else getString(R.string.settings_unable)
                    edit { preferences ->
                        preferences[KEY_NOTIFICATION_STYLE] = isChecked
                    }
                    AudioPlayerManager.getCurrentPlayerInfo()?.let { playInfo ->
                        sendMediaNotification(isChecked, playInfo)
                    }
                }
                else -> {}
            }
        }
        Log.d("frankhon", "onPreferenceChange: preference = $preference newValue = $newValue")
        return true
    }

    private fun edit(transform: (MutablePreferences) -> Unit) {
        lifecycleScope.launch {
            dataStore.edit(transform)
        }
    }
}