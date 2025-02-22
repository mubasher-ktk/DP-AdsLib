package com.dp.ads.lib.activities

import android.app.ActivityOptions
import android.content.Intent
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
import com.dp.ads.lib.interfaces.WelcomeInterface
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.mintegralAdClasses.MintegralBannerAdManager
import com.dp.ads.lib.utils.hideSystemUIUpdated
import com.facebook.shimmer.ShimmerFrameLayout

class WelcomeScreenOne : AppCompatBaseActivity(), WelcomeInterface {

    private var dpAdsConfigurations: DPAdsConfigurations? = null
    private var myView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        supportActionBar?.hide()
        hideSystemUIUpdated()
        dpAdsConfigurations = DPAdsManager.getConfigurations()

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
    }

    override fun onResume() {
        super.onResume()
        val nativeSurvey1Enabled = dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_1") as? Boolean ?: false
        if (nativeSurvey1Enabled) {
            when (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_1_MED")) {
                "ADMOB" -> showAdmobSurveyOneNatives()
                "META" -> showMetaSurveyOneNatives()
                "MINTEGRAL" -> showMintegralSurveyOneBanner()
            }
        } else {
            myView?.let {
                myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.GONE
                myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.GONE
            }
        }
    }

    override fun showWelcomeTwoScreen() {
        WelcomeScreensConfiguration.welcomeInstance?.setWelcomeInterface(null)
        startActivity(Intent(this, WelcomeScreenDup::class.java), ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle())
        finish()
        overridePendingTransition(0, 0)
    }

    private fun showAdmobSurveyOneNatives() {
        myView?.let {
            myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.GONE
            myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.VISIBLE
            dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_SURVEY_1")?.let { adId ->
                AdmobNativeAdManager.requestOrShowAd(
                    mContext = this,
                    adId = adId,
                    adName = "NATIVE_SURVEY_1",
                    isMediaWithCtaOnBottom = true,
                    remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_SURVEY_1").toString().toBoolean(),
                    populateView = true,
                    adContainer = myView?.findViewById(R.id.nativeAdContainerAdmob),
                    onAdFailed = {
                        myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.GONE
                        Log.i("DP_ADS_TAG","WelcomeScreenOne: Admob: onAdFailed()")
                    },
                    onAdLoaded = {
                        Log.i("DP_ADS_TAG","WelcomeScreenOne: Admob: onAdLoaded()")
                    }
                )
            } ?: Log.w("WelcomeScreenOne", "ADMOB_NATIVE_SURVEY_1 ad ID is missing.")
        }
    }
    private fun showMetaSurveyOneNatives() {
        myView?.let {
            myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.GONE
            myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.VISIBLE
            dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_SURVEY_1")?.let { adId ->
                MetaNativeAdManager.requestOrShowAd(
                    mContext = this,
                    adId = adId,
                    adName = "NATIVE_SURVEY_1",
                    isMediaWithCtaOnBottom = true,
                    remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_SURVEY_1").toString().toBoolean(),
                    populateView = true,
                    adContainer = myView?.findViewById(R.id.nativeAdContainerAdmob),
                    onAdFailed = {
                        myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.GONE
                        Log.i("DP_ADS_TAG","WelcomeScreenOne: Meta: onAdFailed()")
                    },
                    onAdLoaded = {
                        Log.i("DP_ADS_TAG","WelcomeScreenOne: Meta: onAdLoaded()")
                    }
                )
            } ?: Log.w("WelcomeScreenOne", "META_NATIVE_SURVEY_1 ad ID is missing.")
        }
    }
    private fun showMintegralSurveyOneBanner() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_1")?.split("-")?.size == 2) {
            myView?.findViewById<CardView>(R.id.nativeAdContainerAdmob)?.visibility = View.GONE
            myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.VISIBLE
            MintegralBannerAdManager.requestBannerAd(
                activity = this,
                placementId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_1")!!.split("-")[0],
                unitId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_1")!!.split("-")[1],
                adName = "NATIVE_SURVEY_1",
                populateView = true,
                bannerContainer = myView?.findViewById(R.id.bannerAdMint),
                shimmerContainer = myView?.findViewById(R.id.shimmerLayoutMint),
                onAdFailed = {
                    myView?.findViewById<CardView>(R.id.nativeAdContainerMintegral)?.visibility = View.GONE
                    Log.i("DP_ADS_TAG", "SURVEY_1: MINTEGRAL: onAdFailed()")
                },
                onAdLoaded = {
                    myView?.findViewById<ShimmerFrameLayout>(R.id.shimmerLayoutMint)?.stopShimmer()
                    myView?.findViewById<ShimmerFrameLayout>(R.id.shimmerLayoutMint)?.visibility = View.INVISIBLE
                    myView?.findViewById<FrameLayout>(R.id.bannerAdMint)?.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "SURVEY_1: MINTEGRAL: onAdLoaded()")
                }
            )
        } else {
            Log.i("DP_ADS_TAG", "BANNER : Mintegral : MAY SURVEY_1 Incorrect ID Format (placementID-unitID)")
        }
    }
}