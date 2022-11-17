package com.frankhon.fantasymusic.ui.activities.settings

import android.os.Bundle
import android.view.View
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.media.AudioPlayerManager
import com.frankhon.fantasymusic.utils.KEY_NOTIFICATION_STYLE
import com.frankhon.fantasymusic.utils.dataStore
import com.frankhon.fantasymusic.utils.sendMediaNotification
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by Frank Hon on 2022/11/16 10:09 下午.
 * E-mail: frank_hon@foxmail.com
 */
class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    private var notificationStyle: SwitchPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        initPreferences()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.dataStore?.data?.map {
            it[KEY_NOTIFICATION_STYLE] ?: 0
        }?.asLiveData()?.observe(viewLifecycleOwner) {
            notificationStyle?.run {
                isChecked = it == 0
                summary =
                    if (isChecked) getString(R.string.settings_enable) else getString(R.string.settings_unable)
            }
        }
    }

    private fun initPreferences() {
        notificationStyle = findPreference("notification_style")
        notificationStyle?.run {
            setOnPreferenceClickListener { preference ->
                val switchPreference = preference as SwitchPreference
                summary = if (switchPreference.isChecked) getString(R.string.settings_enable)
                else getString(R.string.settings_unable)
                lifecycleScope.launch {
                    context.dataStore.edit {
                        it[KEY_NOTIFICATION_STYLE] =
                            if (switchPreference.isChecked) 0 else 1
                    }
                }
                AudioPlayerManager.getCurrentPlayerInfo()?.let {
                    sendMediaNotification(switchPreference.isChecked, it)
                }
                true
            }
        }
    }
}