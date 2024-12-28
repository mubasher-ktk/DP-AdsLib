package com.dp.ads.lib.callingClasses

import android.view.View
import com.dp.ads.lib.data.WalkThroughItem
import java.util.ArrayList

object DPAdsManager {

    private var DPAdsConfigurations: DPAdsConfigurations? = null
    private var onFinish: (() -> Unit)? = null
    private var reConfigureBuilders: (() -> Unit)? = null

    fun startFlow(DPAdsConfigurations: DPAdsConfigurations) {
        if (DPAdsManager.DPAdsConfigurations == null) {
            DPAdsManager.DPAdsConfigurations = DPAdsConfigurations
        }
    }

    fun showWelcomeScreen() {
        DPAdsConfigurations?.startWelcomeScreenConfiguration()
    }

    fun showWelcomeDupScreen() {
        DPAdsConfigurations?.welcomeScreensConfiguration?.showWelcomeTwoScreen()
    }

    fun completeWelcomeScreens() {
        DPAdsConfigurations?.welcomeScreensConfiguration?.endWelcomeTwoScreen()
        DPAdsConfigurations?.startWalkThroughConfiguration()
    }

    fun getConfigurations(): DPAdsConfigurations? {
        return DPAdsConfigurations
    }

    fun setOnFlowStateListener(reConfigureBuilders: () -> Unit, onFinish: () -> Unit) {
        DPAdsManager.onFinish = onFinish
        DPAdsManager.reConfigureBuilders = reConfigureBuilders
    }

    fun notifyFlowFinished() {
        onFinish?.invoke()
    }

    fun notifyReConfigureBuilders() {
        reConfigureBuilders?.invoke()
    }

    fun refreshStrings(upWelcomeScreen: View, walkThroughList: ArrayList<WalkThroughItem>) {
        DPAdsConfigurations?.welcomeScreensConfiguration?.view = upWelcomeScreen
        DPAdsConfigurations?.walkThroughScreensConfiguration?.walkThroughList?.clear()
        DPAdsConfigurations?.walkThroughScreensConfiguration?.walkThroughList = walkThroughList
    }
}
