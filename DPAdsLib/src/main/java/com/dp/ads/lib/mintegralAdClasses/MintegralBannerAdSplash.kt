package com.dp.ads.lib.mintegralAdClasses

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.utils.NetworkCheck
import com.mbridge.msdk.out.BannerAdListener
import com.mbridge.msdk.out.BannerSize
import com.mbridge.msdk.out.MBBannerView
import com.mbridge.msdk.out.MBridgeIds

class MintegralBannerAdSplash(
    activity: Activity? = null,
    private val placementID: String,
    private val unitID: String,
    private val bannerContainer: FrameLayout,
    private val shimmerContainer: View,
    private val onAdFailed: (() -> Unit)? = null,
    private val onAdLoaded: (() -> Unit)? = null,
    private val onAdClicked: (() -> Unit)? = null) {

    private var currentActivity: Activity? = activity
    private var bannerView: MBBannerView? = null
    var isBannerLoaded = false

    init {
        currentActivity?.let {
            if (NetworkCheck.isNetworkAvailable(it)) {
                loadBannerAd(onAdFailed, onAdLoaded, onAdClicked)
            } else {
                Log.i("DP_ADS_TAG", "Mintegral: BannerAd : No Network Available")
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

        bannerView = MBBannerView(currentActivity!!).apply {
            init(BannerSize(BannerSize.DEV_SET_TYPE, 320, 50), placementID, unitID)
            setBannerAdListener(object : BannerAdListener {
                override fun onLoadSuccessed(ids: MBridgeIds?) {
                    Log.i("DP_ADS_TAG", "Mintegral: BannerAd : onLoadSuccessed()")
                    shimmerContainer.visibility = View.INVISIBLE
                    bannerContainer.addView(this@apply)
                    isBannerLoaded = true
                    onAdLoaded?.invoke()
                }

                override fun onLoadFailed(ids: MBridgeIds?, error: String?) {
                    Log.e("DP_ADS_TAG", "Mintegral: BannerAd : onLoadFailed() : $error")
                    onAdFailed?.invoke()
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(it, "Mintegral Banner Ad Failed to Load", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onLogImpression(ids: MBridgeIds?) {
                    Log.i("DP_ADS_TAG", "Mintegral: BannerAd : onLogImpression()")
                }

                override fun onClick(ids: MBridgeIds?) {
                    Log.i("DP_ADS_TAG", "Mintegral: BannerAd : onClick()")
                    onAdClicked?.invoke()
                }

                override fun onLeaveApp(ids: MBridgeIds?) {
                    Log.i("DP_ADS_TAG", "Mintegral: BannerAd : onLeaveApp()")
                }

                override fun showFullScreen(ids: MBridgeIds?) {
                    Log.i("DP_ADS_TAG", "Mintegral: BannerAd : showFullScreen()")
                }

                override fun closeFullScreen(ids: MBridgeIds?) {
                    Log.i("DP_ADS_TAG", "Mintegral: BannerAd : closeFullScreen()")
                }

                override fun onCloseBanner(ids: MBridgeIds?) {
                    Log.i("DP_ADS_TAG", "Mintegral: BannerAd : onCloseBanner()")
                }
            })
        }

        bannerView?.load()
    }
}