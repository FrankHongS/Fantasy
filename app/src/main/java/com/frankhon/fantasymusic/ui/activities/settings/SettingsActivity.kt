package com.frankhon.fantasymusic.ui.activities.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.ui.activities.about.AboutPreferenceFragment
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * Created by Frank Hon on 2022/11/16 9:58 下午.
 * E-mail: frank_hon@foxmail.com
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initView()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.settings_container, SettingsPreferenceFragment())
                .commit()
        }
    }

    private fun initView() {
        cab_settings.setActionBarTitle(R.string.settings)
    }

}