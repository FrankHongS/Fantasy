package com.frankhon.fantasymusic.ui.activities.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.frankhon.fantasymusic.R

/**
 * Created by Frank Hon on 2022/9/20 4:49 下午.
 * E-mail: frank_hon@foxmail.com
 */
class AboutPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about, rootKey)
        initPreferences()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.setBackgroundResource(R.color.colorActionBar)
        return view
    }

    private fun initPreferences() {
        val aboutMe = findPreference<Preference>(getString(R.string.about_me_key))
        val aboutMeDesc = findPreference<Preference>(getString(R.string.about_me_desc_key))
        preferenceScreen.removePreference(aboutMeDesc!!)
        aboutMe?.setOnPreferenceClickListener {
            val result = preferenceScreen.removePreference(aboutMeDesc)
            if (!result) {
                preferenceScreen.addPreference(aboutMeDesc)
                aboutMeDesc.summary = getString(R.string.about_me_desc_summary)
            }
            true
        }
    }

}