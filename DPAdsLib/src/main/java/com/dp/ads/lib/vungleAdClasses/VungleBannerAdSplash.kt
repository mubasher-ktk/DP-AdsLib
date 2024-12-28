package com.dp.ads.lib.vungleAdClasses

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.utils.NetworkCheck
import com.vungle.ads.BannerAd
import com.vungle.ads.BannerAdSize
import com.vungle.ads.BaseAd
import com.vungle.ads.BannerAdListener
import com.vungle.ads.VungleError

class VungleBannerAdSplash(
    activity: Activity? = null,
    private val placementId: String,
    private val bannerContainer: FrameLayout,
    private val shimmerContainer: View,
    private val onAdFailed: (() -> Unit)? = null,
    private val onAdLoaded: (() -> Unit)? = null,
    private val onAdClicked: (() -> Unit)? = null
) {

    private var bannerAd: BannerAd? = null
    private var currentActivity: Activity? = activity
    var isBannerLoaded = false

    init {
        currentActivity?.let {
            if (NetworkCheck.isNetworkAvailable(it)) {
                loadBannerAd(onAdFailed, onAdLoaded, onAdClicked)
            } else {
                Log.i("DP_ADS_TAG", "Vungle: BannerAd : No Network Available")
            }
        }
    }

    private fun loadBannerAd(onAdFailed: (() -> Unit)? = null, onAdLoaded: (() -> Unit)? = null, onAdClicked: (() -> Unit)? = null) {
        if (isBannerLoaded) {
            return
        }

        bannerAd = BannerAd(currentActivity!!, placementId, BannerAdSize.BANNER).apply {
            adListener = object : BannerAdListener {
                override fun onAdClicked(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: BannerAd : onAdClicked()")
                    onAdClicked?.invoke()
                }

                override fun onAdEnd(baseAd: BaseAd) {}

                override fun onAdFailedToLoad(baseAd: BaseAd, adError: VungleError) {
                    Log.i("DP_ADS_TAG", "Vungle: BannerAd : onAdFailedToLoad: $adError")
                    onAdFailed?.invoke()
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(it, "Vungle Banner Ad Failed to Load", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAdFailedToPlay(baseAd: BaseAd, adError: VungleError) {}

                override fun onAdImpression(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: BannerAd : onAdImpression()")
                }

                override fun onAdLeftApplication(baseAd: BaseAd) {}

                override fun onAdLoaded(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: BannerAd : onAdLoaded()")
                    Log.i("DP_ADS_TAG", "Creative id: ${baseAd.creativeId}")

                    shimmerContainer.visibility = View.INVISIBLE
                    val params = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL
                    )
                    bannerContainer.addView(bannerAd?.getBannerView(), params)

                    isBannerLoaded = true
                    onAdLoaded?.invoke()
                }

                override fun onAdStart(baseAd: BaseAd) {}
            }
            load()
        }
    }
}


/* import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.vungle.ads.BannerAd
import com.vungle.ads.BannerAdListener
import com.vungle.ads.BannerAdSize
import com.vungle.ads.BaseAd
import com.vungle.ads.VungleError


class VungleBannerAd(
    activity: Activity? = null,
    private val placementId: String,
    private val bannerContainer: FrameLayout,
    private val shimmerContainer: View,
    private val onAdFailed: (() -> Unit)? = null,
    private val onAdLoaded: (() -> Unit)? = null,
    private val onAdClicked: (() -> Unit)? = null
) {

    private var bannerAd: BannerAd? = null
    private var currentActivity: Activity? = activity
    var isBannerLoaded = false

    init {
        currentActivity?.let {
            if (NetworkCheck.isNetworkAvailable(it)) {
                loadBannerAd(onAdFailed, onAdLoaded, onAdClicked)
            } else {
                Log.i("DP_ADS_TAG", "Vungle: BannerAd : No Network Available")
            }
        }
    }

    private fun loadBannerAd(onAdFailed: (() -> Unit)? = null, onAdLoaded: (() -> Unit)? = null, onAdClicked: (() -> Unit)? = null) {
        if (isBannerLoaded) {
            return
        }

        bannerAd = BannerAd(currentActivity!!, placementId, BannerAdSize.BANNER).apply {
            adListener = object : BannerAdListener {
                override fun onAdClicked(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: BannerAd : onAdClicked()")
                    onAdClicked?.invoke()
                }

                override fun onAdEnd(baseAd: BaseAd) {

                }

                override fun onAdFailedToLoad(baseAd: BaseAd, adError: VungleError) {
                    Log.i("DP_ADS_TAG","Vungle: BannerAd : onAdFailedToLoad$adError")
                    onAdFailed?.invoke()
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(it, "Vungle Banner Ad Failed to Load", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAdFailedToPlay(baseAd: BaseAd, adError: VungleError) {

                }

                override fun onAdImpression(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: BannerAd : onAdImpression()")
                }

                override fun onAdLeftApplication(baseAd: BaseAd) {

                }

                override fun onAdLoaded(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: BannerAd : onAdLoaded()")
                    shimmerContainer.visibility = View.INVISIBLE
                    bannerContainer.addView(bannerAd?.getBannerView())
                    isBannerLoaded = true
                    onAdLoaded?.invoke()
                }

                override fun onAdStart(baseAd: BaseAd) {

                }
            }
            load()
        }
    }
}*/
