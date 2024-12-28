package com.dp.ads.lib.activities

import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.dp.ads.lib.utils.hideSystemUI

class LanguageScreenDup: AppCompatBaseActivity() {

    private lateinit var languageAdapter: LanguageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var imvDone: AppCompatImageView
    private var DPAdsConfigurations: DPAdsConfigurations? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        DPAdsConfigurations = DPAdsManager.getConfigurations()
        hideSystemUI()
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


        if (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_1") as? Boolean == true) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_SURVEY_1_MED")) {
                "ADMOB" -> loadAdmobSurveyNatives()
                "META" -> loadMetaSurveyNatives()
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                intent?.let {
                    if (it.getStringExtra("From").equals("AppSettings")) {
                        finish()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onResume() {
        super.onResume()
        if (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2") as? Boolean == true) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2_MED")) {
                "ADMOB" -> {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.VISIBLE
                    showAdmobLanguageScreenOneNatives()
                }
                "META" -> {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.VISIBLE
                    showMetaLanguageScreenOneNatives()
                }
            }
        }  else {
            findViewById<CardView>(R.id.nativeAdContainerAd)?.let {
                findViewById<CardView>(R.id.nativeAdContainerAd)?.visibility = View.GONE
            }
        }
    }

    private fun showAdmobLanguageScreenOneNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_LANGUAGE_2")
        if (adId != null) {
            AdmobNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_LANGUAGE_2",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2").toString().toBoolean(),
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
        } else {
            Log.w("DP_ADS_TAG", "ADMOB_NATIVE_LANGUAGE_2 ad ID is missing.")
        }
    }

    private fun showMetaLanguageScreenOneNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_LANGUAGE_2")
        if (adId != null) {
            MetaNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_LANGUAGE_2",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2").toString().toBoolean(),
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
        } else {
            Log.w("DP_ADS_TAG", "META_NATIVE_LANGUAGE_2 ad ID is missing.")
        }
    }

    private fun loadMetaSurveyNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_SURVEY_1")
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

    private fun loadAdmobSurveyNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_SURVEY_1")
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
}