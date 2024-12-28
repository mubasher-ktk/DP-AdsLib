package com.dp.ads.lib.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.dp.ads.lib.R
import com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager
import com.dp.ads.lib.callingClasses.DPAdsConfigurations
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.callingClasses.WelcomeScreensConfiguration
import com.dp.ads.lib.interfaces.WelcomeDupInterface
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.utils.hideSystemUI

class WelcomeScreenDup: AppCompatBaseActivity(), WelcomeDupInterface {

    private var myView: View? = null
    private var DPAdsConfigurations: DPAdsConfigurations? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        hideSystemUI()
        DPAdsConfigurations = DPAdsManager.getConfigurations()

        WelcomeScreensConfiguration.welcomeInstance?.let { config ->
            config.setWelcomeDupInterface(this)
            myView = config.view
            myView?.parent?.let { parent ->
                if (parent is ViewGroup) {
                    parent.removeView(myView)
                }
            }
            setContentView(myView)
        }

        val nativeSurvey2Enabled = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_1") as? Boolean ?: false
        if (nativeSurvey2Enabled) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_1_MED")) {
                "ADMOB" -> loadAdmobWTOneNatives()
                "META" -> loadMetaWTOneNatives()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val nativeSurvey1Enabled = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_2") as? Boolean ?: false
        if (nativeSurvey1Enabled) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_2_MED")) {
                "ADMOB" -> showAdmobLanguageScreenOneNatives()
                "META" -> showMetaLanguageScreenOneNatives()
            }
        } else {
            myView?.let {
                myView?.findViewById<CardView>(R.id.nativeAdContainerAd)?.visibility = View.GONE
            }
        }
    }

    private fun showMetaLanguageScreenOneNatives() {
        myView?.let {
            DPAdsConfigurations?.firstOpenFlowAdIds?.get("META_NATIVE_SURVEY_2")?.let { adId ->
                MetaNativeAdManager.requestAd(
                    mContext = this,
                    adId = adId,
                    adName = "NATIVE_SURVEY_2",
                    isMedia = true,
                    isMediumAd = true,
                    remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_SURVEY_2").toString().toBoolean(),
                    populateView = true,
                    nativeAdLayout = myView?.findViewById(R.id.nativeAdContainerAd),
                    onAdFailed = {
                        myView?.findViewById<CardView>(R.id.nativeAdContainerAd)?.visibility = View.GONE
                        Log.i("DP_ADS_TAG","WelcomeScreenDup: Meta: onAdFailed()")
                    },
                    onAdLoaded = {
                        Log.i("DP_ADS_TAG","WelcomeScreenDup: Meta: onAdLoaded()")
                    }
                )
            } ?: Log.w("WelcomeScreenDup", "META_NATIVE_SURVEY_2 ad ID is missing.")
        }
    }

    private fun showAdmobLanguageScreenOneNatives() {
        myView?.let {
            DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_SURVEY_2")?.let { adId ->
                AdmobNativeAdManager.requestAd(
                    mContext = this,
                    adId = adId,
                    adName = "NATIVE_SURVEY_2",
                    isMedia = true,
                    isMediumAd = true,
                    remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_SURVEY_2").toString().toBoolean(),
                    populateView = true,
                    adContainer = myView?.findViewById(R.id.nativeAdContainerAd),
                    onAdFailed = {
                        myView?.findViewById<CardView>(R.id.nativeAdContainerAd)?.visibility = View.GONE
                        Log.i("DP_ADS_TAG","WelcomeScreenDup: Admob: onAdFailed()")
                    },
                    onAdLoaded = {
                        Log.i("DP_ADS_TAG","WelcomeScreenDup: Admob: onAdLoaded()")
                    }
                )
            } ?: Log.w("WelcomeScreenDup", "ADMOB_NATIVE_SURVEY_2 ad ID is missing.")
        }
    }

    private fun loadMetaWTOneNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_WALKTHROUGH_1")
        if (adId != null) {
            MetaNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "WALKTHROUGH_1",
                isMedia = true,
                isMediumAd = true,
                populateView = false
            )
        } else {
            Log.e("DP_ADS_TAG","Meta ad ID not found for WALKTHROUGH_1")
        }
    }

    private fun loadAdmobWTOneNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_WALKTHROUGH_1")
        if (adId != null) {
            AdmobNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "WALKTHROUGH_1",
                isMedia = true,
                isMediumAd = true,
                populateView = false
            )
        } else {
            Log.e("DP_ADS_TAG","Admob ad ID not found for WALKTHROUGH_1")
        }
    }

    override fun endWelcomeTwoScreen() {
        finish()
    }
}