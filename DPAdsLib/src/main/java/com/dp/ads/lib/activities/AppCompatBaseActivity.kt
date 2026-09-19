package com.dp.ads.lib.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.dp.ads.lib.utils.MyLocaleHelper
import java.util.Locale

open class AppCompatBaseActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        val deviceDefaultLanguage = Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "en"
        super.attachBaseContext(MyLocaleHelper.onAttach(base, deviceDefaultLanguage))
        val config = applicationContext.resources.configuration
        applicationContext.resources.updateConfiguration(config, applicationContext.resources.displayMetrics)
    }
}