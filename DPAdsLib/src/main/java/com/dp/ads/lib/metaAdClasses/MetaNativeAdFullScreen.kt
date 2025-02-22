package com.dp.ads.lib.metaAdClasses

import android.app.Activity
import android.util.Log
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

object MetaNativeAdFullScreen {
    private val nativeAdCache = HashMap<String, NativeAd?>()
    private val adLoadingState = HashMap<String, Boolean>()

    fun requestAd(
        mContext: Activity?,
        adId: String,
        adName: String = "",
        remoteConfig: Boolean = true,
        populateView: Boolean = false,
        adContainer: CardView? = null,
        onAdFailed: (() -> Unit)? = null,
        onAdLoaded: (() -> Unit)? = null
    ) {
        if (mContext == null) {
            Log.i("META_ADS_TAG", "Context is null; cannot load ad.")
            onAdFailed?.invoke()
            return
        }

        if (populateView) {
            if (!NetworkCheck.isNetworkAvailable(mContext) || !remoteConfig) {
                adContainer?.visibility = View.GONE
                Log.i("META_ADS_TAG", "Native : Meta : View is gone")
                onAdFailed?.invoke()
                return
            } else {
                adContainer?.visibility = View.VISIBLE
                Log.i("META_ADS_TAG", "Native : Meta : View is VISIBLE")
            }
        }

        if (adLoadingState[adName] == true && nativeAdCache[adName] != null) {
            Log.i("META_ADS_TAG", "Meta: Native : $adName : showCachedAd()")
            showCachedAd(adName, adContainer)
            return
        }

        adLoadingState[adName] = true

        val adView = adContainer?.findViewById(R.id.fbNativeAdContainer) as? NativeAdLayout ?: return

        val nativeAd = NativeAd(mContext, adId)
        nativeAd.loadAd(
            nativeAd.buildLoadAdConfig()
                .withAdListener(object : NativeAdListener {
                    override fun onMediaDownloaded(ad: Ad?) {
                        Log.i("META_ADS_TAG", "Meta: Native : $adName : onMediaDownloaded()")
                    }

                    override fun onAdLoaded(ad: Ad?) {
                        if (nativeAd != ad) return
                        nativeAdCache[adName] = nativeAd
                        adLoadingState[adName] = true
                        if (populateView) {
                            adContainer?.let {
                                Log.i("META_ADS_TAG", "Meta: Native : $adName : populateAdView()")
                                populateNativeAd(nativeAd, adView)
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

                    override fun onError(ad: Ad?, adError: AdError?) {
                        nativeAdCache[adName] = null
                        adLoadingState[adName] = false
                        onAdFailed?.invoke()
                        Log.i("META_ADS_TAG", "Meta: Native : $adName : onError()\n${adError?.errorMessage}")
                    }

                    override fun onAdClicked(ad: Ad?) {
                        Log.i("META_ADS_TAG", "Meta: Native : $adName : onAdClicked()")
                        nativeAdCache[adName] = null
                        adLoadingState[adName] = false
                    }

                    override fun onLoggingImpression(ad: Ad?) {
                        Log.i("META_ADS_TAG", "Meta: Native : $adName : onLoggingImpression()")
                        nativeAdCache[adName] = null
                        adLoadingState[adName] = false
                    }
                })
                .build()
        )
    }

    private fun showCachedAd(adName: String, adContainer: CardView?) {
        adContainer?.context?.let {
            nativeAdCache[adName]?.let { cachedAd ->
                val adView = adContainer.findViewById(R.id.fbNativeAdContainer) as? NativeAdLayout ?: return
                populateNativeAd(cachedAd, adView)
            } ?: run {
                Log.i("META_ADS_TAG", "Ad is not available in cache for adName: $adName")
            }
        } ?: Log.i("META_ADS_TAG", "Ad container or context is null; cannot load ad.")
    }

    private fun populateNativeAd(nativeAd: NativeAd, adView: NativeAdLayout) {
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
        nativeAd.registerViewForInteraction(adView, nativeAdMedia, nativeAdIcon, clickableViews)
    }
}