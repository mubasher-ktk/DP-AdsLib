package com.dp.ads.lib.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import com.dp.ads.lib.R
import com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager
import com.dp.ads.lib.callingClasses.DPAdsConfigurations
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.callingClasses.WelcomeScreensConfiguration
import com.dp.ads.lib.interfaces.WelcomeDupInterface
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.mintegralAdClasses.MintegralBannerAdManager
import com.dp.ads.lib.utils.hideSystemUIUpdated
import com.facebook.shimmer.ShimmerFrameLayout

class WelcomeScreenDup: AppCompatBaseActivity(), WelcomeDupInterface {

    private var dpAdsConfigurations: DPAdsConfigurations? = null
    private var myView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        supportActionBar?.hide()
        hideSystemUIUpdated()
        dpAdsConfigurations = DPAdsManager.getConfigurations()

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

        val nativeWalkThrough1Enabled = dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_1") as? Boolean ?: false
        if (nativeWalkThrough1Enabled) {
            when (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_1_MED")) {
                "ADMOB" -> loadAdmobWTOneNatives()
                "META" -> loadMetaWTOneNatives()
                "MINTEGRAL" -> loadMintegralWTOneBanner()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val nativeSurvey1Enabled = dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_2") as? Boolean ?: false
        if (nativeSurvey1Enabled) {
            when (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_2_MED")) {
                "ADMOB" -> showAdmobLanguageScreenOneNatives()
                "META" -> showMetaLanguageScreenOneNatives()
                "MINTEGRAL" -> showMintegralSurveyDupBanner()
            }
        } else {
            myView?.let {
                myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.GONE
                myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.GONE
            }
        }
    }

    private fun showAdmobLanguageScreenOneNatives() {
        myView?.let {
            myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.GONE
            myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.VISIBLE
            dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_SURVEY_2")?.let { adId ->
                AdmobNativeAdManager.requestOrShowAd(
                    mContext = this,
                    adId = adId,
                    adName = "NATIVE_SURVEY_2",
                    isMediaWithCtaOnBottom = true,
                    remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_SURVEY_2").toString().toBoolean(),
                    populateView = true,
                    adContainer = myView?.findViewById(R.id.nativeAdContainerAdmob),
                    onAdFailed = {
                        myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.GONE
                        Log.i("DP_ADS_TAG","WelcomeScreenDup: Admob: onAdFailed()")
                    },
                    onAdLoaded = {
                        Log.i("DP_ADS_TAG","WelcomeScreenDup: Admob: onAdLoaded()")
                    }
                )
            } ?: Log.w("WelcomeScreenDup", "ADMOB_NATIVE_SURVEY_2 ad ID is missing.")
        }
    }
    private fun showMetaLanguageScreenOneNatives() {
        myView?.let {
            myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.GONE
            myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.VISIBLE
            dpAdsConfigurations?.firstOpenFlowAdIds?.get("META_NATIVE_SURVEY_2")?.let { adId ->
                MetaNativeAdManager.requestOrShowAd(
                    mContext = this,
                    adId = adId,
                    adName = "NATIVE_SURVEY_2",
                    isMediaWithCtaOnBottom = true,
                    remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_SURVEY_2").toString().toBoolean(),
                    populateView = true,
                    adContainer = myView?.findViewById(R.id.nativeAdContainerAdmob),
                    onAdFailed = {
                        myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.GONE
                        Log.i("DP_ADS_TAG","WelcomeScreenDup: Meta: onAdFailed()")
                    },
                    onAdLoaded = {
                        Log.i("DP_ADS_TAG","WelcomeScreenDup: Meta: onAdLoaded()")
                    }
                )
            } ?: Log.w("WelcomeScreenDup", "META_NATIVE_SURVEY_2 ad ID is missing.")
        }
    }
    private fun showMintegralSurveyDupBanner() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_2")?.split("-")?.size == 2) {
            myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.GONE
            myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.VISIBLE
            MintegralBannerAdManager.requestBannerAd(
                activity = this,
                placementId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_2")!!.split("-")[0],
                unitId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_2")!!.split("-")[1],
                adName = "NATIVE_SURVEY_2",
                populateView = true,
                bannerContainer = myView?.findViewById(R.id.bannerAdMint),
                shimmerContainer = myView?.findViewById(R.id.shimmerLayoutMint),
                onAdFailed = {
                    myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.GONE
                    Log.i("DP_ADS_TAG", "SURVEY_2: MINTEGRAL: onAdFailed()")
                },
                onAdLoaded = {
                    myView?.findViewById<ShimmerFrameLayout>(R.id.shimmerLayoutMint)?.stopShimmer()
                    myView?.findViewById<ShimmerFrameLayout>(R.id.shimmerLayoutMint)?.visibility = View.INVISIBLE
                    myView?.findViewById<FrameLayout>(R.id.bannerAdMint)?.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "SURVEY_2: MINTEGRAL: onAdLoaded()")
                }
            )
        } else {
            Log.i("DP_ADS_TAG", "BANNER : Mintegral : MAY SURVEY_2 Incorrect ID Format (placementID-unitID)")
        }
    }

    private fun loadMetaWTOneNatives() {
        val adId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_WALKTHROUGH_1")
        if (adId != null) {
            MetaNativeAdManager.requestOrShowAd(
                mContext = this,
                adId = adId,
                adName = "WALKTHROUGH_1",
                isMediaWithCtaOnTop = true,
                populateView = false
            )
        } else {
            Log.e("DP_ADS_TAG","Meta ad ID not found for WALKTHROUGH_1")
        }
    }
    private fun loadAdmobWTOneNatives() {
        val adId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_WALKTHROUGH_1")
        if (adId != null) {
            AdmobNativeAdManager.requestOrShowAd(
                mContext = this,
                adId = adId,
                adName = "WALKTHROUGH_1",
                isMediaWithCtaOnTop = true,
                populateView = false
            )
        } else {
            Log.e("DP_ADS_TAG","Admob ad ID not found for WALKTHROUGH_1")
        }
    }
    private fun loadMintegralWTOneBanner() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_WALKTHROUGH_1")?.split("-")?.size == 2) {
            MintegralBannerAdManager.requestBannerAd(
                activity = this,
                placementId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_WALKTHROUGH_1")!!.split("-")[0],
                unitId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_WALKTHROUGH_1")!!.split("-")[1],
                adName = "WALKTHROUGH_1",
                populateView = false)
        } else {
            Log.e("DP_ADS_TAG","BANNER : Mintegral : MAY SURVEY_1 Incorrect ID Format (placementID-unitID)")
        }
    }

    override fun endWelcomeTwoScreen() {
        finish()
    }
}