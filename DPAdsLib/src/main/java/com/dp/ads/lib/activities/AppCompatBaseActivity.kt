package com.dp.ads.lib.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.dp.ads.lib.utils.MyLocaleHelper

open class AppCompatBaseActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(MyLocaleHelper.onAttach(base, "en"))
        val config = applicationContext.resources.configuration
        applicationContext.resources.updateConfiguration(config, applicationContext.resources.displayMetrics)
    }
}