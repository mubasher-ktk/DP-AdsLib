package com.dp.ads.lib.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.hideSystemUIUpdated() {
    window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
}

fun Activity.hideSystemUI() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.setDecorFitsSystemWindows(false)
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}

fun View.blockingClickListener(debounceTime: Long = 1500L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View) {
            val timeNow = SystemClock.elapsedRealtime()
            val elapsedTimeSinceLastClick = timeNow - lastClickTime

            if (elapsedTimeSinceLastClick < debounceTime) {
                Log.e("clickTag", "Double click shielded within $debounceTime ms (elapsed: $elapsedTimeSinceLastClick ms)")
                return
            }
            else {
                Log.e("clickTag", "Click happened (elapsed: $elapsedTimeSinceLastClick ms)")
                action()
            }
            lastClickTime = timeNow
        }
    })
}

fun privacyPolicy(activity: Activity, url: String, tabColor: Int) {
    val customIntent = CustomTabsIntent.Builder()
    customIntent.setToolbarColor(ContextCompat.getColor(activity, tabColor))
    openCustomTab(activity, customIntent.build(), Uri.parse(url))
}

fun openCustomTab(activity: Activity, customTabsIntent: CustomTabsIntent, uri: Uri?) {
    if (uri != null) {
        customTabsIntent.launchUrl(activity, uri)
    }
}

fun composeFeedBackEmail(context: Context, myMail: String) {
    val emailIntent = Intent(Intent.ACTION_SENDTO)
    emailIntent.data = Uri.parse("mailto:$myMail")
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feed Back")
    try {
        context.startActivity(Intent.createChooser(emailIntent, "Send email using..."))
    } catch (e: Exception) {
        Toast.makeText(context, "No email clients installed.", Toast.LENGTH_SHORT).show()
    }
}

fun shareApp(context: Context, myPackageName: String) {
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(
        Intent.EXTRA_TEXT,
        "https://play.google.com/store/apps/details?id=$myPackageName"
    )
    sendIntent.type = "text/plain"
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Try New App")
    val myIntent = Intent.createChooser(sendIntent, "Share via")
    myIntent.flags = FLAG_ACTIVITY_NEW_TASK
    context.startActivity(myIntent)
}

fun moreApps(context: Context, moreAppLink: String) {
    val marketUri = Uri.parse(moreAppLink)
    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
    marketIntent.flags = FLAG_ACTIVITY_NEW_TASK
    context.startActivity(marketIntent)
}