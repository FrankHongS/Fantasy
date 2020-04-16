package com.frankhon.fantasymusic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_haha.setOnClickListener(this)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, FragmentA())
            .commit()
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_haha -> Toast.makeText(this, "haha", Toast.LENGTH_SHORT).show()
        }
    }
}
