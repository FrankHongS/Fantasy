package com.frankhon.fantasymusic.activities.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.frankhon.fantasymusic.BuildConfig
import com.frankhon.fantasymusic.R
import kotlinx.android.synthetic.main.activity_about.*

/**
 * Created by Frank Hon on 2022/9/18 12:55 下午.
 * E-mail: frank_hon@foxmail.com
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        initView()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.about_container, AboutPreferenceFragment())
                .commit()
        }
    }

    private fun initView() {
        tv_app_version.text =
            String.format(getString(R.string.app_version), BuildConfig.VERSION_NAME)
        cab_about.run {
            setOnBackClickListener { finish() }
            setActionBarTitle(R.string.about)
        }
    }

}