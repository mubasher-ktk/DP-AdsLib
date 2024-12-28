package com.dp.ads.lib.callingClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import com.dp.ads.lib.activities.WelcomeScreenOne
import com.dp.ads.lib.interfaces.WelcomeDupInterface
import com.dp.ads.lib.interfaces.WelcomeInterface

class WelcomeScreensConfiguration private constructor() {

    private lateinit var activityContext: Activity
    lateinit var view: View
    private var welcomeInterface: WelcomeInterface? = null
    private var welcomeInterfaceDup: WelcomeDupInterface? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var welcomeInstance: WelcomeScreensConfiguration? = null
    }

    fun welcomeInitializationSetup() {
        Log.i("WelcomeScreens", "Welcome: welcomeInitializationSetup()")
        activityContext.startActivity(Intent(activityContext, WelcomeScreenOne::class.java))
        activityContext.finish()
    }

    fun setWelcomeInterface(welcomeInterface: WelcomeInterface?) {
        this.welcomeInterface = welcomeInterface
    }

    fun setWelcomeDupInterface(welcomeInterfaceDup: WelcomeDupInterface?) {
        this.welcomeInterfaceDup = welcomeInterfaceDup
    }

    fun showWelcomeTwoScreen() {
        welcomeInterface?.showWelcomeTwoScreen()
    }

    fun endWelcomeTwoScreen() {
        welcomeInterfaceDup?.endWelcomeTwoScreen()
    }

    class Builder {
        private lateinit var activity: Activity
        private var viewXML: View? = null

        fun setActivityContext(myActivity: Activity) = apply {
            this.activity = myActivity
        }

        fun setXMLLayout(myView: View) = apply {
            this.viewXML = myView
        }

        fun build(): WelcomeScreensConfiguration {
            if (!::activity.isInitialized) {
                throw IllegalStateException("Activity context must be provided")
            }
            if (viewXML == null) {
                throw IllegalStateException("View must be initialized")
            }
            val welcomeScreenAdsConfig = WelcomeScreensConfiguration()
            welcomeScreenAdsConfig.activityContext = activity
            welcomeScreenAdsConfig.view = viewXML!!
            welcomeInstance = welcomeScreenAdsConfig
            return welcomeScreenAdsConfig
        }
    }
}