package com.dp.ads.lib.callingClasses

import android.app.Activity
import android.util.Log
import com.dp.ads.lib.adMobAdClasses.AdmobInterstitialAdSplash
import com.dp.ads.lib.adMobAdClasses.AdmobResumeAdSplash
import com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager
import com.dp.ads.lib.metaAdClasses.MetaInterstitialAdSplash
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.utils.NetworkCheck
import com.dp.ads.lib.utils.PrefHelper
import com.dp.ads.lib.utilsGoogleAdsConsent.ConsentConfigurations
import com.dp.ads.lib.vungleAdClasses.VungleInterstitialAdSplash
import com.dp.ads.lib.vungleAdClasses.VungleResumeAdSplash

class DPAdsConfigurations private constructor() {

    var languageScreensConfiguration: LanguageScreensConfiguration? = null
    var welcomeScreensConfiguration: WelcomeScreensConfiguration? = null
    var walkThroughScreensConfiguration: WalkThroughScreensConfiguration? = null
    private var remoteConfigData: HashMap<String, Any>? = null
    var firstOpenFlowAdIds: HashMap<String, String> = HashMap()
    lateinit var admobResumeAdSplash: AdmobResumeAdSplash
    lateinit var vungleResumeAdSplash: VungleResumeAdSplash
    lateinit var admobInterstitialAdSplash: AdmobInterstitialAdSplash
    lateinit var metaInterstitialAdSplash: MetaInterstitialAdSplash
    lateinit var vungleInterstitialAdSplash: VungleInterstitialAdSplash

    fun setRemoteConfigData(activityContext: Activity, myRemoteConfigData: HashMap<String, Any>) {
        this.remoteConfigData = myRemoteConfigData
        this.remoteConfigData?.forEach { value ->
            Log.i("RemoteConfigFetches","DPAdsConfigurations : setRemoteConfigData() "+value.key + " : " + value.value)
        }

        when {
            !NetworkCheck.isNetworkAvailable(activityContext) -> {
                proceedNext(activityContext)
            }
            myRemoteConfigData.getValue("RESUME_INTER_SPLASH") == "RESUME" -> {
                when {
                    myRemoteConfigData.getValue("RESUME_INTER_SPLASH_MED") == "ADMOB" -> {
                        showAdMobResumeAdSplash(activityContext)
                    }
                    myRemoteConfigData.getValue("RESUME_INTER_SPLASH_MED") == "LIFTOFF" || myRemoteConfigData.getValue("RESUME_INTER_SPLASH_MED") == "VUNGLE" -> {
                        showVungleResumeAdSplash(activityContext)
                    }
                }
            }
            myRemoteConfigData.getValue("RESUME_INTER_SPLASH") == "INTERSTITIAL" -> {
                when {
                    myRemoteConfigData.getValue("RESUME_INTER_SPLASH_MED") == "ADMOB" -> {
                        showAdMobInterstitialAdSplash(activityContext)
                    }
                    myRemoteConfigData.getValue("RESUME_INTER_SPLASH_MED") == "LIFTOFF" || myRemoteConfigData.getValue("RESUME_INTER_SPLASH_MED") == "VUNGLE" -> {
                        showVungleInterstitialAdSplash(activityContext)
                    }
                    myRemoteConfigData.getValue("RESUME_INTER_SPLASH_MED") == "META" -> {
                        showMetaInterstitialAdSplash(activityContext)
                    }
                }
            }
            myRemoteConfigData.getValue("RESUME_INTER_SPLASH") == "OFF" -> {
                proceedNext(activityContext)
            }
        }

        if (!PrefHelper(activityContext).getBooleanDefault("StartScreens", default = false)) {
            if (myRemoteConfigData.getValue("NATIVE_LANGUAGE_1") == true) {
                when {
                    myRemoteConfigData.getValue("NATIVE_LANGUAGE_1_MED") == "ADMOB" -> {
                        loadAdmobLanguageScreenOneNatives(activityContext)
                    }
                    myRemoteConfigData.getValue("NATIVE_LANGUAGE_1_MED") == "META" -> {
                        loadMetaLanguageScreenOneNatives(activityContext)
                    }
                }
            }
        }
    }

    private fun loadAdmobLanguageScreenOneNatives(mContext: Activity) {
         AdmobNativeAdManager.requestAd(
             mContext = mContext,
             adId = firstOpenFlowAdIds.getValue("ADMOB_NATIVE_LANGUAGE_1"),
             adName = "NATIVE_LANGUAGE_1",
             isMedia = true,
             isMediumAd = true,
             populateView = false
         )
    }

    private fun loadMetaLanguageScreenOneNatives(mContext: Activity) {
        MetaNativeAdManager.requestAd(
            mContext = mContext,
            adId = firstOpenFlowAdIds.getValue("META_NATIVE_LANGUAGE_1"),
            adName = "NATIVE_LANGUAGE_1",
            isMedia = true,
            isMediumAd = true,
            populateView = false
        )
    }

    private fun showMetaInterstitialAdSplash(activityContext: Activity) {
        activityContext.let {
            metaInterstitialAdSplash = MetaInterstitialAdSplash(activityContext, firstOpenFlowAdIds.getValue("META_SPLASH_INTERSTITIAL"),
                onAdDismissed = {
                    proceedNext(activityContext)
                },
                onAdFailed = {
                    proceedNext(activityContext)
                },
                onAdTimeout = {
                    proceedNext(activityContext)
                },
                onAdShowed = {}
            )
        }
    }

    private fun showVungleInterstitialAdSplash(activityContext: Activity) {
        activityContext.let {
            vungleInterstitialAdSplash = VungleInterstitialAdSplash(activityContext, firstOpenFlowAdIds.getValue("LIFTOFF_SPLASH_INTERSTITIAL"),
                onAdDismissed = {
                    proceedNext(activityContext)
                },
                onAdFailed = {
                    proceedNext(activityContext)
                },
                onAdTimeout = {
                    proceedNext(activityContext)
                },
                onAdShowed = {}
            )
        }
    }

    private fun showVungleResumeAdSplash(activityContext: Activity) {
        activityContext.let {
            vungleResumeAdSplash = VungleResumeAdSplash(activityContext, firstOpenFlowAdIds.getValue("LIFTOFF_SPLASH_RESUME"),
                onAdDismissed = {
                    proceedNext(activityContext)
                },
                onAdFailed = {
                    proceedNext(activityContext)
                },
                onAdTimeout = {
                    proceedNext(activityContext)
                },
                onAdShowed = {}
            )
        }
    }

    private fun showAdMobResumeAdSplash(activityContext: Activity) {
        activityContext.let {
            admobResumeAdSplash = AdmobResumeAdSplash(activityContext, firstOpenFlowAdIds.getValue("ADMOB_SPLASH_RESUME"),
                onAdDismissed = {
                    proceedNext(activityContext)
                },
                onAdFailed = {
                    proceedNext(activityContext)
                },
                onAdTimeout = {
                    proceedNext(activityContext)
                },
                onAdShowed = {}
            )
        }
    }

    private fun showAdMobInterstitialAdSplash(activityContext: Activity) {
        activityContext.let {
            admobInterstitialAdSplash = AdmobInterstitialAdSplash(activityContext, firstOpenFlowAdIds.getValue("ADMOB_SPLASH_INTERSTITIAL"),
                onAdDismissed = {
                    proceedNext(activityContext)
                },
                onAdFailed = {
                    proceedNext(activityContext)
                },
                onAdTimeout = {
                    proceedNext(activityContext)
                },
                onAdShowed = {}
            )
        }
    }

    private fun proceedNext(activityContext: Activity) {
        if (PrefHelper(activityContext).getBooleanDefault("StartScreens", default = false)) {
            DPAdsManager.notifyFlowFinished()
        } else {
            startLanguageScreenConfiguration()
        }
    }

    fun getRemoteConfigData() : HashMap<String, Any>? {
        return this.remoteConfigData
    }

    private fun startLanguageScreenConfiguration() {
        Log.i("LanguageScreens","DPAdsConfigurations : startLanguageScreenConfiguration() ")
        this.languageScreensConfiguration?.languageInitializationSetup() ?: run { }
    }

    fun startWelcomeScreenConfiguration() {
        Log.i("WelcomeScreens","DPAdsConfigurations : startWelcomeScreenConfiguration() ")
        DPAdsManager.notifyReConfigureBuilders()
        this.welcomeScreensConfiguration?.welcomeInitializationSetup() ?: run { }
    }

    fun startWalkThroughConfiguration() {
        Log.i("WalkThroughScreens","DPAdsConfigurations : startWalkThroughConfiguration() ")
        this.walkThroughScreensConfiguration?.walkThroughInitializationSetup() ?: run { }
    }

    class Builder {
        private lateinit var consentConfigurations: ConsentConfigurations
        private var languageScreensConfiguration: LanguageScreensConfiguration? = null
        private var welcomeScreensConfiguration: WelcomeScreensConfiguration? = null
        private var walkThroughScreensConfiguration: WalkThroughScreensConfiguration? = null
        private var firstOpenFlowAdIds: HashMap<String, String> = HashMap()

        fun setFirstOpenFlowAdIds(firstOpenFlowAdIds: HashMap<String, String>) = apply {
            this.firstOpenFlowAdIds = firstOpenFlowAdIds
            this.firstOpenFlowAdIds.forEach { value ->
                Log.i("DP_ADS_TAG","setFirstOpenFlowAdIds : "+value.key + " : " + value.value)
            }
        }

        fun setConsentConfig(consentConfig: ConsentConfigurations) = apply {
            this.consentConfigurations = consentConfig
        }

        fun setLanguageScreenConfiguration(languageScreensConfiguration: LanguageScreensConfiguration) = apply {
            this.languageScreensConfiguration = languageScreensConfiguration
        }

        fun setWelcomeScreenConfiguration(welcomeScreensConfiguration: WelcomeScreensConfiguration) = apply {
            this.welcomeScreensConfiguration = welcomeScreensConfiguration
        }

        fun setWalkThroughScreenConfiguration(walkThroughScreensConfiguration: WalkThroughScreensConfiguration) = apply {
            this.walkThroughScreensConfiguration = walkThroughScreensConfiguration
        }

        fun build(): DPAdsConfigurations {
            if (!::consentConfigurations.isInitialized) {
                throw IllegalStateException("ConsentConfigurations must be provided")
            }
            val DPAdsConfigurations = DPAdsConfigurations()
            DPAdsConfigurations.firstOpenFlowAdIds = firstOpenFlowAdIds
            DPAdsConfigurations.welcomeScreensConfiguration = welcomeScreensConfiguration
            DPAdsConfigurations.languageScreensConfiguration = languageScreensConfiguration
            DPAdsConfigurations.walkThroughScreensConfiguration = walkThroughScreensConfiguration
            return DPAdsConfigurations
        }
    }
}
