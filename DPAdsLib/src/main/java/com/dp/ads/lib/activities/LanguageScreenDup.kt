package com.dp.ads.lib.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dp.ads.lib.R
import com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager
import com.dp.ads.lib.adapters.LanguageAdapter
import com.dp.ads.lib.callingClasses.LanguageScreensConfiguration
import com.dp.ads.lib.callingClasses.DPAdsConfigurations
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.mintegralAdClasses.MintegralBannerAdManager
import com.dp.ads.lib.utils.hideSystemUIUpdated
import com.facebook.shimmer.ShimmerFrameLayout

class LanguageScreenDup: AppCompatBaseActivity() {

    private lateinit var languageAdapter: LanguageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var imvDone: AppCompatImageView
    private var dpAdsConfigurations: DPAdsConfigurations? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        supportActionBar?.hide()
        dpAdsConfigurations = DPAdsManager.getConfigurations()
        hideSystemUIUpdated()
        setContentView(R.layout.language_screen_dup)
        imvDone = findViewById(R.id.imvDone)
        recyclerView = findViewById(R.id.recyclerViewLanguage)
        recyclerView.layoutManager = LinearLayoutManager(this)

        LanguageScreensConfiguration.languageInstance?.let { config ->
            config.languageList?.let { languageList ->
                config.selectedDrawable?.let { selectedDrawable ->
                    config.unSelectedDrawable?.let { unSelectedDrawable ->
                        config.selectedRadio?.let { selectedRadio ->
                            config.unSelectedRadio?.let { unSelectedRadio ->
                                languageAdapter = LanguageAdapter(
                                    ctx = this,
                                    languages = languageList,
                                    selectedDrawable = selectedDrawable,
                                    unSelectedDrawable = unSelectedDrawable,
                                    selectedRadio = selectedRadio,
                                    unSelectedRadio = unSelectedRadio
                                ) { }
                                recyclerView.adapter = languageAdapter
                            }
                        }
                    }
                }
            }
        }

        imvDone.setOnClickListener {
            intent?.let {
                if (it.getStringExtra("From").equals("AppSettings")) {
                    finish()
                } else {
                    DPAdsManager.showWelcomeScreen()
                    finish()
                }
            }
        }


        if (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_1") as? Boolean == true) {
            when (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_1_MED")) {
                "ADMOB" -> loadAdmobSurveyOneNatives()
                "META" -> loadMetaSurveyOneNatives()
                "MINTEGRAL" -> loadMintegralSurveyOneBanner()
            }
        }

        val nativeSurvey2Enabled = dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_2") as? Boolean ?: false
        if (nativeSurvey2Enabled) {
            when (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_2_MED")) {
                "ADMOB" -> loadAdmobSurveyDupNatives()
                "META" -> loadMetaSurveyDupNatives()
                "MINTEGRAL" -> loadMintegralSurveyDupBanner()
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                intent?.let {
//                    if (it.getStringExtra("From").equals("AppSettings")) {
                    finish()
//                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onResume() {
        super.onResume()
        if (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2") as? Boolean == true) {
            when (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2_MED")) {
                "ADMOB" -> {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.VISIBLE
                    showAdmobLanguageScreenDupNatives()
                }
                "META" -> {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.VISIBLE
                    showMetaLanguageScreenDupNatives()
                }
                "MINTEGRAL" -> {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.VISIBLE
                    showMintegralLanguageScreenDupBanner()
                }
            }
        } else {
            findViewById<CardView>(R.id.nativeAdContainerAd)?.let {
                findViewById<CardView>(R.id.nativeAdContainerAd)?.visibility = View.GONE
            }
        }
    }

    private fun showAdmobLanguageScreenDupNatives() {
        dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_LANGUAGE_2")?.let { adId ->
            AdmobNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_LANGUAGE_2",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2").toString().toBoolean(),
                populateView = true,
                adContainer = findViewById(R.id.nativeAdContainerAd),
                onAdFailed = {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.GONE
                    Log.i("DP_ADS_TAG", "LanguageScreenDup: Admob onAdFailed()")
                },
                onAdLoaded = {
                    Log.i("DP_ADS_TAG", "LanguageScreenDup: Admob onAdLoaded()")
                }
            )
        } ?: Log.w("DP_ADS_TAG", "ADMOB_NATIVE_LANGUAGE_2 ad ID is missing.")
    }
    private fun showMetaLanguageScreenDupNatives() {
        dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_LANGUAGE_2")?.let { adId ->
            MetaNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_LANGUAGE_2",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2").toString().toBoolean(),
                populateView = true,
                nativeAdLayout = findViewById(R.id.nativeAdContainerAd),
                onAdFailed = {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.GONE
                    Log.i("DP_ADS_TAG", "LanguageScreenDup: Meta: onAdFailed()")
                },
                onAdLoaded = {
                    Log.i("DP_ADS_TAG", "LanguageScreenDup: Meta: onAdLoaded()")
                }
            )
        } ?: Log.w("DP_ADS_TAG", "META_NATIVE_LANGUAGE_2 ad ID is missing.")
    }
    private fun showMintegralLanguageScreenDupBanner() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_LANGUAGE_2")?.split("-")?.size == 2) {
            MintegralBannerAdManager.requestBannerAd(
                activity = this,
                placementId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_BANNER_LANGUAGE_2").split("-")[0],
                unitId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_BANNER_LANGUAGE_2").split("-")[1],
                adName = "NATIVE_LANGUAGE_2",
                remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_LANGUAGE_2").toString().toBoolean(),
                populateView = true,
                bannerContainer = findViewById(R.id.bannerAdMint),
                shimmerContainer = findViewById(R.id.shimmerLayout),
                onAdFailed = {
                    Log.i("DP_ADS_TAG", "LANGUAGE_2: MINTEGRAL: onAdFailed()")
                },
                onAdLoaded = {
                    findViewById<ShimmerFrameLayout>(R.id.shimmerLayout).stopShimmer()
                    findViewById<ShimmerFrameLayout>(R.id.shimmerLayout).visibility = View.INVISIBLE
                    findViewById<FrameLayout>(R.id.bannerAdMint).visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "LANGUAGE_2: MINTEGRAL: onAdLoaded()")
                }
            )
        } else {
            Log.i("DP_ADS_TAG", "BANNER : Mintegral : MAY LANGUAGE_2 Incorrect ID Format (placementID-unitID)")
        }
    }

    private fun loadAdmobSurveyOneNatives() {
        val adId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_SURVEY_1")
        if (adId != null) {
            AdmobNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_SURVEY_1",
                isMedia = true,
                isMediumAd = true,
                populateView = false
            )
        } else {
            Log.w("DP_ADS_TAG", "ADMOB_NATIVE_SURVEY_1 ad ID is missing.")
        }
    }
    private fun loadMetaSurveyOneNatives() {
        val adId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_SURVEY_1")
        if (adId != null) {
            MetaNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_SURVEY_1",
                isMedia = true,
                isMediumAd = true,
                populateView = false
            )
        } else {
            Log.w("DP_ADS_TAG", "META_NATIVE_SURVEY_1 ad ID is missing.")
        }
    }
    private fun loadMintegralSurveyOneBanner() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_1")?.split("-")?.size == 2) {
            MintegralBannerAdManager.requestBannerAd(
                activity = this,
                placementId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_1")!!.split("-")[0],
                unitId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_1")!!.split("-")[1],
                adName = "NATIVE_SURVEY_1",
                populateView = false)
        } else {
            Log.e("DP_ADS_TAG","BANNER : Mintegral : MAY SURVEY_1 Incorrect ID Format (placementID-unitID)")
        }
    }

    private fun loadAdmobSurveyDupNatives() {
        val adId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_SURVEY_2")
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
    private fun loadMetaSurveyDupNatives() {
        val adId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_SURVEY_2")
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
    private fun loadMintegralSurveyDupBanner() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_2")?.split("-")?.size == 2) {
            MintegralBannerAdManager.requestBannerAd(
                activity = this,
                placementId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_2")!!.split("-")[0],
                unitId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_SURVEY_2")!!.split("-")[1],
                adName = "NATIVE_SURVEY_2",
                populateView = false)
        } else {
            Log.e("DP_ADS_TAG","BANNER : Mintegral : MAY SURVEY_2 Incorrect ID Format (placementID-unitID)")
        }
    }
}