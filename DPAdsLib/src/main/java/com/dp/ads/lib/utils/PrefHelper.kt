package com.dp.ads.lib.utils

import android.content.Context
import android.content.SharedPreferences

class PrefHelper(context: Context) {
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("DPAdsLib", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun putString(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    fun getStringDefault(key: String, default: String): String? {
        return sharedPreferences.getString(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun getBooleanDefault(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun clear() {
        editor.clear().apply()
    }
}