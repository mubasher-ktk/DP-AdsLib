package com.dp.ads.lib.adMobAdClasses

import android.app.Activity
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.utils.NetworkCheck
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class AdMobBannerAdSplash(
    activity: Activity? = null,
    private val placementID: String,
    private val bannerContainer: FrameLayout,
    private val shimmerContainer: View,
    private val onAdFailed: (() -> Unit)? = null,
    private val onAdLoaded: (() -> Unit)? = null,
    private val onAdClicked: (() -> Unit)? = null) {
    private var adView: AdView? = null
    private var currentActivity: Activity? = activity
    var isBannerLoaded = false

    init {
        currentActivity?.let {
            if (NetworkCheck.isNetworkAvailable(it)) {
                loadBannerAd(onAdFailed, onAdLoaded, onAdClicked)
            } else {
                Log.i("DP_ADS_TAG", "AdMob: BannerAd : No Network Available")
            }
        }
    }

    private fun loadBannerAd(onAdFailed: (() -> Unit)? = null, onAdLoaded: (() -> Unit)? = null, onAdClicked: (() -> Unit)? = null) {
        if (isBannerLoaded) {
            return
        }

        adView = AdView(currentActivity!!.baseContext).apply {
            adUnitId = placementID
            setAdSize(getAdSizeTest())
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.i("DP_ADS_TAG", "AdMob: BannerAd : onAdLoaded()")
                    shimmerContainer.visibility = View.INVISIBLE
                    bannerContainer.addView(this@apply)
                    isBannerLoaded = true
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.i("DP_ADS_TAG", "AdMob: BannerAd : onAdFailedToLoad: $error")
                    onAdFailed?.invoke()
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(it, "AdMob Banner Ad Failed to Load", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAdClicked() {
                    Log.i("DP_ADS_TAG", "AdMob: BannerAd : onAdClicked()")
                    onAdClicked?.invoke()
                }
            }
        }

        adView?.loadAd(AdRequest.Builder().build())
    }

    private fun getAdSizeTest(): AdSize {
        val display: Display = currentActivity!!.windowManager.getDefaultDisplay()
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(currentActivity!!, adWidth)
    }

    fun removeAd() {
        bannerContainer.removeAllViews()
        adView = null
        Log.i("DP_ADS_TAG", "AdMob: BannerAd : removeAd()")
    }
}