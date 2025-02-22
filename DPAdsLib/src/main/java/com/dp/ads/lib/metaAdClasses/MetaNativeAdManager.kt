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
import com.facebook.ads.MediaView
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdLayout
import com.facebook.ads.NativeAdListener
import java.util.HashMap

object MetaNativeAdManager {
    private val nativeAdCache = HashMap<String, NativeAd?>()
    private val adLoadingState = HashMap<String, Boolean>()

    fun requestOrShowAd(
        mContext: Activity?,
        adId: String,
        adName: String = "",
        saveAdsToCache: String = "SAVE",
        isMediaWithCtaOnTop: Boolean = false,
        isMediaWithCtaOnBottom: Boolean = false,
        isMediaOnRight: Boolean = false,
        isMediumAd: Boolean = false,
        remoteConfig: Boolean = true,
        populateView: Boolean = false,
        adContainer: CardView? = null,
        onAdFailed: (() -> Unit)? = null,
        onAdLoaded: (() -> Unit)? = null
    ) {
        if (mContext == null) {
            Log.i("DP_ADS_TAG", "Context is null; cannot load ad.")
            onAdFailed?.invoke()
            return
        }

        if (populateView) {
            if (!NetworkCheck.isNetworkAvailable(mContext) || !remoteConfig) {
                adContainer?.visibility = View.GONE
                Log.i("DP_ADS_TAG", "Native : Meta : View is gone")
                onAdFailed?.invoke()
                return
            } else {
                adContainer?.visibility = View.VISIBLE
                Log.i("DP_ADS_TAG", "Native : Meta : View is VISIBLE")
            }
        } else {
            Log.i("DP_ADS_TAG", "Native : Meta : populateView")
        }

        if (adLoadingState[adName] == true && nativeAdCache[adName] != null) {
            Log.i("DP_ADS_TAG", "Meta: Native : $adName : showCachedAd()")
            showCachedAd(
                adName,
                adContainer,
                isMediaWithCtaOnTop,
                isMediaWithCtaOnBottom,
                isMediaOnRight,
                isMediumAd
            )
            return
        }

        adLoadingState[adName] = true

        val adView = mContext.layoutInflater.inflate(
            when {
                isMediaWithCtaOnTop -> R.layout.meta_native_large_cta_top
                isMediaWithCtaOnBottom -> R.layout.meta_native_large_cta_bottom
                isMediaOnRight -> R.layout.meta_native_media_right_side
                isMediumAd -> R.layout.meta_native_simple_large
                else -> R.layout.meta_native_simple_small
            },
            null
        ) as? NativeAdLayout ?: return

        if (NetworkCheck.isNetworkAvailable(mContext)) {
            val fbNativeAd = NativeAd(mContext, adId)

            val nativeAdListener = object : NativeAdListener {
                override fun onMediaDownloaded(ad: Ad) {
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onMediaDownloaded()")
                }

                override fun onError(ad: Ad, adError: AdError) {
                    nativeAdCache[adName] = null
                    adLoadingState[adName] = false
                    onAdFailed?.invoke()
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onError()\n${adError.errorMessage}")
                    mContext.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(mContext, "Meta: Native : $adName : Failed To Load() $adName", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAdLoaded(ad: Ad) {
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onAdLoaded()")
                    if (saveAdsToCache == "SAVE") {
                        nativeAdCache[adName] = fbNativeAd
                        adLoadingState[adName] = true
                    }
                    if (populateView) {
                        adContainer?.let { container ->
                            populateNativeAd(
                                fbNativeAd,
                                adView,
                                isMediaWithCtaOnTop,
                                isMediaWithCtaOnBottom,
                                isMediaOnRight
                            )
                            container.removeAllViews()
                            container.addView(adView)
                        }
                    } else {
                        mContext.let {
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(
                                    mContext,
                                    "Meta: Native : Loaded()\n$adName",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    onAdLoaded?.invoke()
                }

                override fun onAdClicked(ad: Ad) {
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onAdClicked()")
                    nativeAdCache[adName] = null
                    adLoadingState[adName] = false
                }

                override fun onLoggingImpression(ad: Ad) {
                    Log.i("DP_ADS_TAG", "Meta: Native : $adName : onLoggingImpression()")
                    nativeAdCache[adName] = null
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

    private fun showCachedAd(
        adName: String,
        adContainer: CardView?,
        isMediaWithCtaOnTop: Boolean,
        isMediaWithCtaOnBottom: Boolean,
        isMediaOnRight: Boolean,
        isMediumAd: Boolean
    ) {
        adContainer?.context?.let { context ->
            nativeAdCache[adName]?.let { cachedAd ->
                val adView = LayoutInflater.from(context).inflate(
                    when {
                        isMediaWithCtaOnTop -> R.layout.meta_native_large_cta_top
                        isMediaWithCtaOnBottom -> R.layout.meta_native_large_cta_bottom
                        isMediaOnRight -> R.layout.meta_native_media_right_side
                        isMediumAd -> R.layout.meta_native_simple_large
                        else -> R.layout.meta_native_simple_small
                    },
                    null
                ) as? NativeAdLayout ?: return

                populateNativeAd(
                    cachedAd,
                    adView,
                    isMediaWithCtaOnTop,
                    isMediaWithCtaOnBottom,
                    isMediaOnRight
                )
                adContainer.removeAllViews()
                adContainer.addView(adView)
            } ?: run {
                Log.i("DP_ADS_TAG", "Ad is not available in cache for adName: $adName")
            }
        } ?: Log.i("DP_ADS_TAG", "Ad container or context is null; cannot load ad.")
    }

    private fun populateNativeAd(
        nativeAd: NativeAd,
        adView: NativeAdLayout,
        isMediaWithCtaOnTop: Boolean,
        isMediaWithCtaOnBottom: Boolean,
        isMediaOnRight: Boolean
    ) {
        val nativeAdIcon = adView.findViewById<MediaView>(R.id.nativeAdIcon)
        val nativeAdTitle = adView.findViewById<TextView>(R.id.nativeAdTitle)
        val nativeAdMedia = adView.findViewById<MediaView>(R.id.nativeAdMedia)
        val nativeAdBody = adView.findViewById<TextView>(R.id.nativeAdBody)
        val sponsoredLabel = adView.findViewById<TextView>(R.id.nativeAdSponsoredLabel)
        val nativeAdSocialContext = adView.findViewById<TextView>(R.id.nativeAdSocialContext)
        val nativeAdCallToAction = adView.findViewById<Button>(R.id.nativeAdCallToAction)

        val adChoicesContainer: LinearLayout? = adView.findViewById(R.id.adChoicesContainer)
        val adOptionsView = AdOptionsView(adView.context, nativeAd, adView)
        adChoicesContainer?.removeAllViews()
        adChoicesContainer?.addView(adOptionsView, 0)

        nativeAdTitle?.text = nativeAd.advertiserName
        nativeAdSocialContext?.text = nativeAd.adSocialContext
        nativeAdBody?.text = nativeAd.adBodyText

        nativeAdCallToAction?.visibility = if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction?.text = nativeAd.adCallToAction
        sponsoredLabel?.text = nativeAd.sponsoredTranslation

        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle!!)
        clickableViews.add(nativeAdCallToAction!!)
        if (isMediaWithCtaOnTop || isMediaWithCtaOnBottom || isMediaOnRight) {
            nativeAd.registerViewForInteraction(adView, nativeAdMedia, nativeAdIcon, clickableViews)
        } else {
            nativeAd.registerViewForInteraction(adView, nativeAdIcon, clickableViews)
        }
    }

    fun clearNativeCache() {
        nativeAdCache.clear()
        adLoadingState.clear()
    }
}