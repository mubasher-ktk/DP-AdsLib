package com.dp.ads.lib.adMobAdClasses

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import com.dp.ads.lib.utils.NetworkCheck
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

object AdmobCollapsibleBanner {

    var collapsibleBannerAdMobHashMap: HashMap<String, AdView>? = HashMap()

    fun checkAndLoadCollapsibleBanner(
        activity: Activity,
        adViewContainer: ViewGroup,
        adID: String,
        remoteConfig: Boolean,
        adName: String,
        setShimmerToInvisible: Boolean,
        shimmerLayoutBanner: ShimmerFrameLayout,
        adSize: AdSize
    ) {
        if (NetworkCheck.isNetworkAvailable(activity) && remoteConfig) {
            if (collapsibleBannerAdMobHashMap!!.containsKey(adName)) {
                val collapsibleAdView: AdView? = collapsibleBannerAdMobHashMap!![adName]
                shimmerLayoutBanner.stopShimmer()
                if (setShimmerToInvisible) {
                    shimmerLayoutBanner.visibility = View.INVISIBLE
                } else {
                    shimmerLayoutBanner.visibility = View.GONE
                }
                adViewContainer.removeView(shimmerLayoutBanner)

                val parent = collapsibleAdView?.parent as? ViewGroup
                parent?.removeView(collapsibleAdView)

                adViewContainer.addView(collapsibleAdView)
            } else {
                loadBanner(
                    activity,
                    adViewContainer,
                    adID,
                    remoteConfig,
                    adName,
                    shimmerLayoutBanner,
                    setShimmerToInvisible,
                    adSize
                )
            }
        } else {
            adViewContainer.visibility = View.GONE
            shimmerLayoutBanner.stopShimmer()
            shimmerLayoutBanner.visibility = View.GONE
        }
    }

    private fun loadBanner(
        activity: Activity,
        adViewContainer: ViewGroup,
        adID: String,
        remoteConfig: Boolean,
        adName: String,
        shimmerLayoutBanner: ShimmerFrameLayout,
        setShimmerToInvisible: Boolean,
        adSize: AdSize) {
        val adView = AdView(activity).apply {
            setAdSize(adSize)
            adUnitId = adID
        }

        val extras = Bundle().apply { putString("collapsible", "bottom") }

        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adViewContainer.removeAllViews()
                adViewContainer.addView(adView)
                if (remoteConfig.equals("SAVE")) {
                    collapsibleBannerAdMobHashMap!![adName] = adView
                }
                shimmerLayoutBanner.stopShimmer()
                if (setShimmerToInvisible) {
                    shimmerLayoutBanner.visibility = View.INVISIBLE
                } else {
                    shimmerLayoutBanner.visibility = View.GONE
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                shimmerLayoutBanner.stopShimmer()
                if (setShimmerToInvisible) {
                    shimmerLayoutBanner.visibility = View.INVISIBLE
                } else {
                    shimmerLayoutBanner.visibility = View.GONE
                }
            }
        }
    }

    fun getAdSize(activity: Activity, adViewContainer: ViewGroup): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density
        var adWidthPixels = adViewContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun clearCacheAds() {
        collapsibleBannerAdMobHashMap?.clear()
    }
}