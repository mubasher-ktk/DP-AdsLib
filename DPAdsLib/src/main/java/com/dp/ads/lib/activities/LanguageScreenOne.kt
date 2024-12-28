package com.dp.ads.lib.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dp.ads.lib.R
import com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager
import com.dp.ads.lib.adapters.LanguageAdapter
import com.dp.ads.lib.callingClasses.LanguageScreensConfiguration
import com.dp.ads.lib.callingClasses.DPAdsConfigurations
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.interfaces.LanguageInterface
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.utils.hideSystemUI

class LanguageScreenOne : AppCompatBaseActivity(), LanguageInterface {

    private lateinit var languageAdapter: LanguageAdapter
    private lateinit var recyclerView: RecyclerView
    private var DPAdsConfigurations: DPAdsConfigurations? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DPAdsConfigurations = DPAdsManager.getConfigurations()
        supportActionBar?.hide()
        hideSystemUI()
        setContentView(R.layout.language_screen_one)
        recyclerView = findViewById(R.id.recyclerViewLanguage)
        recyclerView.layoutManager = LinearLayoutManager(this)

        Log.i("LanguageScreenOne", "Language: onCreate")

        LanguageScreensConfiguration.languageInstance?.let { config ->
            config.setLanguageInterface(this)

            config.languageList?.let {
                if (config.selectedDrawable != null && config.unSelectedDrawable != null) {
                    if (config.selectedRadio != null && config.unSelectedRadio != null) {
                        languageAdapter = LanguageAdapter(
                            ctx = this,
                            languages = config.languageList!!,
                            selectedDrawable = config.selectedDrawable!!,
                            unSelectedDrawable = config.unSelectedDrawable!!,
                            selectedRadio = config.selectedRadio!!,
                            unSelectedRadio = config.unSelectedRadio!!) {
                            config.showLanguageTwoScreen()
                        }
                        recyclerView.adapter = languageAdapter
                    }
                }
            }
        }

        val nativeLanguage1Enabled = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2") as? Boolean ?: false
        if (nativeLanguage1Enabled) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_2_MED")) {
                "ADMOB" -> loadAdmobLanguageScreenDupNatives()
                "META" -> loadMetaLanguageScreenDupNatives()
            }
        }
    }

    private fun loadMetaLanguageScreenDupNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.get("META_NATIVE_LANGUAGE_2")
        if (adId != null) {
            MetaNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_LANGUAGE_2",
                isMedia = true,
                isMediumAd = true,
                populateView = false
            )
        } else {
            Log.w("LanguageScreenOne", "META_NATIVE_LANGUAGE_2 ad ID is missing in firstOpenFlowAdIds.")
        }
    }

    private fun loadAdmobLanguageScreenDupNatives() {
        val adId = DPAdsConfigurations?.firstOpenFlowAdIds?.get("ADMOB_NATIVE_LANGUAGE_2")
        if (adId != null) {
            AdmobNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_LANGUAGE_2",
                isMedia = true,
                isMediumAd = true,
                populateView = false
            )
        } else {
            Log.w("LanguageScreenOne", "ADMOB_NATIVE_LANGUAGE_2 ad ID is missing in firstOpenFlowAdIds.")
        }
    }


    override fun showLanguageTwoScreen() {
        Log.i("LanguageScreenOne", "Language: showLanguageTwoScreen()")
        startActivity(Intent(this, LanguageScreenDup::class.java), ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle())
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onResume() {
        super.onResume()
        val nativeLanguage1Enabled = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_1") as? Boolean ?: false
        if (nativeLanguage1Enabled) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_LANGUAGE_1_MED")) {
                "ADMOB" -> {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.VISIBLE
                    showAdmobLanguageScreenOneNatives()
                }
                "META" -> {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.VISIBLE
                    showMetaLanguageScreenOneNatives()
                }
            }
        } else {
            findViewById<CardView>(R.id.nativeAdContainerAd)?.let {
                findViewById<CardView>(R.id.nativeAdContainerAd)?.visibility = View.GONE
            }
        }
    }

    private fun showMetaLanguageScreenOneNatives() {
        DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_LANGUAGE_1")?.let { adId ->
            MetaNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_LANGUAGE_1",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_LANGUAGE_1").toString().toBoolean(),
                populateView = true,
                nativeAdLayout = findViewById(R.id.nativeAdContainerAd),
                onAdFailed = {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.GONE
                    Log.i("LanguageScreenOne", "Language: onAdFailed()")
                },
                onAdLoaded = {
                    Log.i("LanguageScreenOne", "Language: onAdLoaded()")
                }
            )
        } ?: Log.w("LanguageScreenOne", "META_NATIVE_LANGUAGE_1 ad ID is missing.")
    }

    private fun showAdmobLanguageScreenOneNatives() {
        DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_LANGUAGE_1")?.let { adId ->
            AdmobNativeAdManager.requestAd(
                mContext = this,
                adId = adId,
                adName = "NATIVE_LANGUAGE_1",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_LANGUAGE_1").toString().toBoolean(),
                populateView = true,
                adContainer = findViewById(R.id.nativeAdContainerAd),
                onAdFailed = {
                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.GONE
                    Log.i("LanguageScreenOne", "Language: onAdFailed()")
                },
                onAdLoaded = {
                    Log.i("LanguageScreenOne", "Language: onAdLoaded()")
                }
            )
        } ?: Log.w("LanguageScreenOne", "ADMOB_NATIVE_LANGUAGE_1 ad ID is missing.")
    }
}