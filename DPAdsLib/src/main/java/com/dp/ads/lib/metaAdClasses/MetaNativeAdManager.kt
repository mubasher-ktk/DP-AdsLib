package com.dp.ads.lib.metaAdClasses

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.NetworkCheck
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdOptionsView
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdLayout
import com.facebook.ads.NativeAdListener
import java.util.HashMap

object MetaNativeAdManager {
    private val nativeFbAdCache = HashMap<String, NativeAd?>()
    private val adLoadingState = HashMap<String, Boolean>()

    fun requestAd(
        mContext: Activity?,
        adId: String,
        adName: String = "",
        isMedia: Boolean,
        isMediumAd: Boolean = false,
        remoteConfig: Boolean = true,
        populateView: Boolean = false,
        nativeAdLayout: CardView? = null,
        onAdFailed: (() -> Unit)? = null,
        onAdLoaded: (() -> Unit)? = null) {

        if (mContext == null) {
            Log.i("DP_ADS_TAG", "Context or network unavailable; cannot load ad.")
            nativeAdLayout?.visibility = View.GONE
            onAdFailed?.invoke()
            return
        }

        if (populateView) {
            if (!NetworkCheck.isNetworkAvailable(mContext) || !remoteConfig) {
                nativeAdLayout?.visibility = View.GONE
                Log.i("DP_ADS_TAG","Native : Meta : View is gone")
                onAdFailed?.invoke()
                return
            } else {
                nativeAdLayout?.visibility = View.VISIBLE
                Log.i("DP_ADS_TAG","Native : Meta : View is VISIBLE")
            }
        } else {
            Log.i("DP_ADS_TAG","Native : Meta : populateView")
        }

        if (adLoadingState[adName] == true && nativeFbAdCache[adName] != null) {
            Log.i("DP_ADS_TAG", "Meta: Native : $adName : showCachedAd()")
            showCachedAd(adName, isMedia, nativeAdLayout, isMediumAd)
            return
        }

        adLoadingState[adName] = true

        val adView: NativeAdLayout = if (isMedia) {
            if (isMediumAd) {
                mContext.layoutInflater.inflate(
                    R.layout.meta_native_mediaview_large,
                    null
                ) as NativeAdLayout
            } else {
                mContext.layoutInflater.inflate(
                    R.layout.meta_native_mediaview_medium,
                    null
                ) as NativeAdLayout
            }
        } else {
            if (isMediumAd) {
                mContext.layoutInflater.inflate(
                    R.layout.meta_native_simple_large,
                    null
                ) as NativeAdLayout
            } else {
                mContext.layoutInflater.inflate(
                    R.layout.meta_native_simple_small,
                    null
                ) as NativeAdLayout
            }
        }

        if (NetworkCheck.isNetworkAvailable(mContext)) {
            val fbNativeAd = NativeAd(mContext, adId)

            val nativeAdListener = object : NativeAdListener {
                override fun onMediaDownloaded(ad: Ad) {
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onMediaDownloaded()")
                }

                override fun onError(ad: Ad, adError: AdError) {
                    nativeFbAdCache[adName] = null
                    adLoadingState[adName] = false
                    onAdFailed?.invoke()
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onError()\n${adError.errorMessage}")
                    mContext.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(mContext,"Meta: Native : $adName : Failed To Load() $adName", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAdLoaded(ad: Ad) {
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onAdLoaded()")
                    nativeFbAdCache[adName] = fbNativeAd
                    adLoadingState[adName] = true

                    if (populateView) {
                        nativeAdLayout.let {
                            if (isMedia) {
                                Log.i("DP_ADS_TAG", "Meta: Native : $adName : populateWithMediaView()")
                                populateWithMediaView(isMediumAd, fbNativeAd, adView)
                            } else {
                                Log.i("DP_ADS_TAG", "Meta: Native : $adName : populateSimpleAd()")
                                populateSimpleAd(isMediumAd, fbNativeAd, adView)
                            }

                            mContext.runOnUiThread {
                                nativeAdLayout?.removeAllViews()
                                nativeAdLayout?.addView(adView)
                            }
                        }
                    } else {
                        mContext.let {
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(mContext,"Meta: Native : onAdLoaded() $adName", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    onAdLoaded?.invoke()
                }

                override fun onAdClicked(ad: Ad) {
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onAdClicked()")
                    nativeFbAdCache[adName] = null
                    adLoadingState[adName] = false
                }

                override fun onLoggingImpression(ad: Ad) {
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onLoggingImpression()")
                    nativeFbAdCache[adName] = null
                    adLoadingState[adName] = false
                }
            }

            fbNativeAd.loadAd(
                fbNativeAd.buildLoadAdConfig()
                    .withAdListener(nativeAdListener)
                    .build()
            )

        } else {
            onAdFailed?.invoke()
        }
    }

    fun showCachedAd(
        adName: String,
        isMedia: Boolean,
        adContainer: CardView?,
        isMediumAd: Boolean
    ) {
        val cachedAd = nativeFbAdCache[adName]
        cachedAd?.let {
            val fbNativeAdView: NativeAdLayout = if (isMedia) {
                if (isMediumAd) {
                    LayoutInflater.from(adContainer?.context)
                        .inflate(R.layout.meta_native_mediaview_large, null) as NativeAdLayout
                } else {
                    LayoutInflater.from(adContainer?.context)
                        .inflate(R.layout.meta_native_mediaview_medium, null) as NativeAdLayout
                }
            } else {
                if (isMediumAd) {
                    LayoutInflater.from(adContainer?.context)
                        .inflate(R.layout.meta_native_simple_large, null) as NativeAdLayout
                } else {
                    LayoutInflater.from(adContainer?.context)
                        .inflate(R.layout.meta_native_simple_small, null) as NativeAdLayout
                }
            }

            if (isMedia) {
                Log.i("DP_ADS_TAG", "Meta: Native : $adName : populateWithMediaView()")
                populateWithMediaView(isMediumAd, cachedAd, fbNativeAdView)
            } else {
                Log.i("DP_ADS_TAG", "Meta: Native : $adName : populateSimpleAd()")
                populateSimpleAd(isMediumAd, cachedAd, fbNativeAdView)
            }

            adContainer?.removeAllViews()
            adContainer?.addView(fbNativeAdView)
        }
    }

    private fun populateWithMediaView(
        isMediumAd: Boolean,
        nativeAd: NativeAd?,
        adView: NativeAdLayout?
    ) {
        val nativeAdIcon = adView?.findViewById<com.facebook.ads.MediaView>(R.id.nativeAdIcon)
        val nativeAdTitle = adView?.findViewById<TextView>(R.id.nativeAdTitle)
        val nativeAdMedia = adView?.findViewById<com.facebook.ads.MediaView>(R.id.nativeAdMedia)
        val nativeAdBody = adView?.findViewById<TextView>(R.id.nativeAdBody)
        val sponsoredLabel = adView?.findViewById<TextView>(R.id.nativeAdSponsoredLabel)
        val nativeAdSocialContext = adView?.findViewById<TextView>(R.id.nativeAdSocialContext)
        val nativeAdCallToAction = adView?.findViewById<Button>(R.id.nativeAdCallToAction)

        val adChoicesContainer: LinearLayout? = adView?.findViewById(R.id.adChoicesContainer)
        val adOptionsView = AdOptionsView(adView?.context, nativeAd, adView)
        adChoicesContainer?.removeAllViews()
        adChoicesContainer?.addView(adOptionsView, 0)

        nativeAdTitle?.text = nativeAd?.advertiserName
        nativeAdSocialContext?.text = nativeAd?.adSocialContext
        nativeAdBody?.text = nativeAd?.adBodyText

        nativeAdCallToAction?.visibility =
            if (nativeAd!!.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction?.text = nativeAd.adCallToAction
        sponsoredLabel?.text = nativeAd.sponsoredTranslation

        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle!!)
        clickableViews.add(nativeAdCallToAction!!)
        nativeAd.registerViewForInteraction(adView, nativeAdMedia, nativeAdIcon, clickableViews)
    }

    private fun populateSimpleAd(
        isMediumAd: Boolean,
        nativeAd: NativeAd?,
        adView: NativeAdLayout?
    ) {
        val nativeAdIcon = adView?.findViewById<com.facebook.ads.MediaView>(R.id.nativeAdIcon)
        val nativeAdTitle = adView?.findViewById<TextView>(R.id.nativeAdTitle)
        val nativeAdBody = adView?.findViewById<TextView>(R.id.nativeAdBody)
        val sponsoredLabel = adView?.findViewById<TextView>(R.id.nativeAdSponsoredLabel)
        val nativeAdSocialContext = adView?.findViewById<TextView>(R.id.nativeAdSocialContext)
        val nativeAdCallToAction = adView?.findViewById<Button>(R.id.nativeAdCallToAction)

        val adChoicesContainer: LinearLayout? = adView?.findViewById(R.id.adChoicesContainer)
        val adOptionsView = AdOptionsView(adView?.context, nativeAd, adView)
        adChoicesContainer?.removeAllViews()
        adChoicesContainer?.addView(adOptionsView, 0)

        nativeAdTitle?.text = nativeAd?.advertiserName
        nativeAdSocialContext?.text = nativeAd?.adSocialContext
        nativeAdBody?.text = nativeAd?.adBodyText

        nativeAdCallToAction?.visibility =
            if (nativeAd!!.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction?.text = nativeAd.adCallToAction
        sponsoredLabel?.text = nativeAd.sponsoredTranslation

        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle!!)
        clickableViews.add(nativeAdCallToAction!!)
        nativeAd.registerViewForInteraction(adView, nativeAdIcon, clickableViews)
    }
}