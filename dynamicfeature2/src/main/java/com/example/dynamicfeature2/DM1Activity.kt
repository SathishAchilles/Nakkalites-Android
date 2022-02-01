package com.example.dynamicfeature2

import `in`.nakkalites.mediaclient.view.BaseActivity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.play.core.splitcompat.SplitCompat

class DM1Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dm1)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }
}