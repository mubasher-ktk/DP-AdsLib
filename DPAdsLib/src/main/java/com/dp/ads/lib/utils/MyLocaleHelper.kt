package com.dp.ads.lib.utils

import android.annotation.TargetApi
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import java.util.Locale

object MyLocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private val sLocale: Locale? = null

    @JvmStatic
    fun onAttach(context: Context, defaultLanguage: String): Context {
        val lang = getPersistedData(context, defaultLanguage)
        Log.i("MyLocaleHelper", "onAttach(): $lang")
        return setLocale(context, lang)
    }

    fun setLocale(context: Context, language: String?): Context {
        persist(context, language)
        Log.i("MyLocaleHelper", "setLocale(): $language")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language)
        }

        return updateResourcesLegacy(context, language)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage)
    }

    private fun persist(context: Context, language: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        Log.i("MyLocaleHelper", "persist(): $language")
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String?): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        Log.i("MyLocaleHelper", "updateResources(): $language")
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    @Suppress("deprecation")
    private fun updateResourcesLegacy(context: Context, language: String?): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        Log.i("MyLocaleHelper", "updateResourcesLegacy(): $language")
        val resources = context.resources

        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    @JvmStatic
    fun updateConfig(app: Application, configuration: Configuration?) {
        if (sLocale != null) {
            val config = Configuration(configuration)
            config.locale = sLocale
            val res = app.baseContext.resources
            res.updateConfiguration(config, res.displayMetrics)
        }
    }
}