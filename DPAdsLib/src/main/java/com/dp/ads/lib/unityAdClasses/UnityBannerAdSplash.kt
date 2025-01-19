package com.dp.ads.lib.unityAdClasses

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

object UnityBannerAdSplash {
    fun showBannerAds(
        activity: Activity,
        bannerContainer: FrameLayout,
        placementId: String) {
        val adView = BannerView(activity, placementId, UnityBannerSize.standard)
        val adListener = object : BannerView.IListener {
            override fun onBannerLoaded(bannerAdView: BannerView?) {
                Log.i("DP_ADS_TAG", "Unity :: Banner :: Splash: onBannerLoaded")
            }

            override fun onBannerShown(bannerAdView: BannerView?) {
                Log.i("DP_ADS_TAG", "Unity :: Banner :: Splash: onBannerShown")
            }

            override fun onBannerClick(bannerAdView: BannerView?) {
                Log.i("DP_ADS_TAG", "Unity :: Banner :: Splash: onBannerClick")
            }

            override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: BannerErrorInfo?) {
                Log.e("DP_ADS_TAG", "Unity :: Banner :: Splash: onBannerFailedToLoad - Error: ${errorInfo?.errorMessage}")
            }

            override fun onBannerLeftApplication(bannerView: BannerView?) {
                Log.i("DP_ADS_TAG", "Unity :: Banner :: Splash: onBannerLeftApplication")
            }
        }

        adView.listener = adListener
        adView.load()
        bannerContainer.addView(adView)
        Log.i("DP_ADS_TAG", "Unity :: Banner :: Splash is loading...")
    }
}