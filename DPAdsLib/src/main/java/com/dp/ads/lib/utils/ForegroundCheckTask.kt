package com.dp.ads.lib.utils

import android.app.ActivityManager
import android.content.Context
import android.os.AsyncTask

@Suppress("OverrideDeprecatedMigration", "OverrideDeprecatedMigration",
    "OverrideDeprecatedMigration"
)
class ForegroundCheckTask : AsyncTask<Context?, Void?, Boolean>() {

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName: String = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg p0: Context?): Boolean {
        val context: Context = p0[0]!!.applicationContext
        return isAppOnForeground(context)
    }
}