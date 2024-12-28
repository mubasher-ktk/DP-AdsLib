package com.dp.ads.lib.activities

import android.app.ActivityOptions
import android.content.Intent
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
import com.dp.ads.lib.interfaces.WelcomeInterface
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.utils.hideSystemUI

class WelcomeScreenOne : AppCompatBaseActivity(), WelcomeInterface {

    private var DPAdsConfigurations: DPAdsConfigurations? = null
    private var myView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        hideSystemUI()
        DPAdsConfigurations = DPAdsManager.getConfigurations()

        WelcomeScreensConfiguration.welcomeInstance?.let { config ->
            config.setWelcomeInterface(this)
            myView = config.view
            myView?.parent?.let { parent ->
                if (parent is ViewGroup) {
                    parent.removeView(myView)
                }
            }
            setContentView(myView)
        }

        val nativeSurvey2Enabled = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_2") as? Boolean ?: false
        if (nativeSurvey2Enabled) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_2_MED")) {
                "ADMOB" -> loadAdmobSurveyDupNatives()
                "META" -> loadMetaSurveyDupNatives()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val nativeSurvey1Enabled = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_1") as? Boolean ?: false
        if (nativeSurvey1Enabled) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_1_MED")) {
                "ADMOB" -> showAdmobLanguageScreenOneNatives()
                "META" -> showMetaLanguageScreenOneNatives()
            }
        } else {
            myView?.let {
                myView?.findViewById<CardView>(R.id.nativeAdContainerAd)?.visibility = View.GONE
            }
        }
    }

    override fun showWelcomeTwoScreen() {
        WelcomeScreensConfiguration.welcomeInstance?.setWelcomeInterface(null)
        startActivity(Intent(this, WelcomeScreenDup::class.java), ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle())
        finish()
        overridePendingTransition(0, 0)
    }

    private fun showMetaLanguageScreenOneNatives() {
        myView?.let {
            DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_SURVEY_1")?.let { adId ->
                MetaNativeAdManager.requestAd(
                    mContext = this,
                    adId = adId,
                    adName = "NATIVE_SURVEY_1",
                    isMedia = true,
                    isMediumAd = true,
                    remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_SURVEY_1").toString().toBoolean(),
                    populateView = true,
                    nativeAdLayout = myView?.findViewById(R.id.nativeAdContainerAd),
                    onAdFailed = {
                        myView?.findViewById<CardView>(R.id.nativeAdContainerAd)?.visibility = View.GONE
                        Log.i("DP_ADS_TAG","WelcomeScreenOne: Meta: onAdFailed()")
                    },
                    onAdLoaded = {
                        Log.i("DP_ADS_TAG","WelcomeScreenOne: Meta: onAdLoaded()")
                    }
                )
            } ?: Log.w("WelcomeScreenOne", "META_NATIVE_SURVEY_1 ad ID is missing.")
        }
    }

    private fun showAdmobLanguageScreenOneNatives() {
        myView?.let {
            DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_SURVEY_1")?.let { adId ->
                AdmobNativeAdManager.requestAd(
                    mContext = this,
                    adId = adId,
                    adName = "NATIVE_SURVEY_1",
                    isMedia = true,
                    isMediumAd = true,
                    remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_SURVEY_1").toString().toBoolean(),
                    populateView = true,
                    adContainer = myView?.findViewById(R.id.nativeAdContainerAd),
                    onAdFailed = {
                        myView?.findViewById<CardView>(R.id.nativeAdContainerAd)?.visibility = View.GONE
                        Log.i("DP_ADS_TAG","WelcomeScreenOne: Admob: onAdFailed()")
                    },
                    onAdLoaded = {
                        Log.i("DP_ADS_TAG","WelcomeScreenOne: Admob: onAdLoaded()")
                    }
                )
            } ?: Log.w("WelcomeScreenOne", "ADMOB_NATIVE_SURVEY_1 ad ID is missing.")
        }
    }

    private fun loadMetaSurveyDupNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_SURVEY_2")
        if (adId != null) {
            MetaNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_SURVEY_2",
                isMedia = true,
                isMediumAd = true,
                populateView = false
            )
        } else {
            Log.e("DP_ADS_TAG", "Meta ad ID not found for NATIVE_SURVEY_2")
        }
    }

    private fun loadAdmobSurveyDupNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_SURVEY_2")
        if (adId != null) {
            AdmobNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_SURVEY_2",
                isMedia = true,
                isMediumAd = true,
                populateView = false
            )
        } else {
            Log.e("DP_ADS_TAG", "Admob ad ID not found for NATIVE_SURVEY_2")
        }
    }
}