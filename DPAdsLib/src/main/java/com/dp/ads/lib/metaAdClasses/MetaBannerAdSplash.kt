package com.dp.ads.lib.metaAdClasses

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.utils.NetworkCheck
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView

class MetaBannerAdSplash(
    activity: Activity? = null,
    private val placementID: String,
    private val bannerContainer: FrameLayout,
    private val shimmerContainer: View,
    private val onAdFailed: (() -> Unit)? = null,
    private val onAdLoaded: (() -> Unit)? = null,
    private val onAdClicked: (() -> Unit)? = null
) {
    private var adView: AdView? = null
    private var currentActivity: Activity? = activity
    var isBannerLoaded = false

    init {
        currentActivity?.let {
            if (NetworkCheck.isNetworkAvailable(it)) {
                loadBannerAd(onAdFailed, onAdLoaded, onAdClicked)
            } else {
                Log.i("DP_ADS_TAG", "Meta: BannerAd : No Network Available")
            }
        }
    }

    private fun loadBannerAd(
        onAdFailed: (() -> Unit)? = null,
        onAdLoaded: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null
    ) {
        if (isBannerLoaded) {
            return
        }

        adView = AdView(currentActivity, placementID, AdSize.BANNER_HEIGHT_50)
        val adListener = object : AdListener {
            override fun onError(ad: Ad?, adError: AdError) {
                Log.i("DP_ADS_TAG", "Meta: BannerAd : onAdFailedToLoad: ${adError.errorMessage}")
                onAdFailed?.invoke()
                currentActivity.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(currentActivity,"Meta: Banner : Failed To Load\n$adError", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAdLoaded(ad: Ad?) {
                Log.i("DP_ADS_TAG", "Meta: BannerAd : onAdLoaded()")
                currentActivity.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(currentActivity,"Meta: Banner : Loaded()", Toast.LENGTH_SHORT).show()
                    }
                }
                shimmerContainer.visibility = View.INVISIBLE
                bannerContainer.addView(adView)
                isBannerLoaded = true
                onAdLoaded?.invoke()
            }

            override fun onAdClicked(ad: Ad?) {
                Log.i("DP_ADS_TAG", "Meta: BannerAd : onAdClicked()")
                onAdClicked?.invoke()
            }

            override fun onLoggingImpression(ad: Ad?) {
                // Optional: Log impression if needed
            }
        }

        adView?.loadAd(adView?.buildLoadAdConfig()?.withAdListener(adListener)?.build())
    }

    fun removeAd() {
        bannerContainer.removeAllViews()
        adView?.destroy()
        adView = null
        Log.i("DP_ADS_TAG", "Meta: BannerAd : removeAd()")
    }
}