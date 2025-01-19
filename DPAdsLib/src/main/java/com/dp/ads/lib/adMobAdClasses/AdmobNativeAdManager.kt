package com.dp.ads.lib.adMobAdClasses

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Guideline
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.NetworkCheck
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

object AdmobNativeAdManager {
    private val nativeAdCache = HashMap<String, NativeAd?>()
    private val adLoadingState = HashMap<String, Boolean>()

    fun requestAd(
        mContext: Activity?,
        adId: String,
        adName: String = "",
        isMedia: Boolean,
        isMediumAd: Boolean = false,
        remoteConfig: Boolean = true,
        populateView: Boolean = false,
        adContainer: CardView? = null,
        onAdFailed: (() -> Unit)? = null,
        onAdLoaded: (() -> Unit)? = null) {

        if (mContext == null) {
            Log.i("DP_ADS_TAG", "Context is null; cannot load ad.")
            onAdFailed?.invoke()
            return
        }

        if (populateView) {
            if (!NetworkCheck.isNetworkAvailable(mContext) || !remoteConfig) {
                adContainer?.visibility = View.GONE
                Log.i("DP_ADS_TAG","Native : Admob : View is gone")
                onAdFailed?.invoke()
                return
            } else {
                adContainer?.visibility = View.VISIBLE
                Log.i("DP_ADS_TAG","Native : Admob : View is VISIBLE")
            }
        } else {
            Log.i("DP_ADS_TAG","Native : Admob : populateView")
        }

        if (adLoadingState[adName] == true && nativeAdCache[adName] != null) {
            Log.i("DP_ADS_TAG", "Admob: Native : $adName : showCachedAd()")
            showCachedAd(adName, isMedia, adContainer, isMediumAd)
            return
        }

        adLoadingState[adName] = true

        val adView = mContext.layoutInflater.inflate(
            when {
                isMedia && isMediumAd -> R.layout.admob_native_mediaview_large
                isMedia -> R.layout.admob_native_mediaview_medium
                isMediumAd -> R.layout.admob_native_simple_large
                else -> R.layout.admob_native_simple_small
            },
            null
        ) as? NativeAdView ?: return

        if (NetworkCheck.isNetworkAvailable(mContext)) {
            val adLoader = AdLoader.Builder(mContext, adId)
                .forNativeAd { nativeAd ->
                    nativeAdCache[adName] = nativeAd
                    adLoadingState[adName] = true
                    if (populateView) {
                        adContainer?.let { container ->
                            populateNativeAd(isMediumAd, nativeAd, adView, isMedia)
                            container.removeAllViews()
                            container.addView(adView)
                        }
                    } else {
                        mContext.let {
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(mContext,"Admob: Native : Loaded()\n$adName", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    onAdLoaded?.invoke()
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: LoadAdError) {
                        nativeAdCache[adName] = null
                        adLoadingState[adName] = false
                        onAdFailed?.invoke()
                        mContext.let {
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(mContext,"Admob: Native : onAdFailedToLoad() $adName", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Log.i("DP_ADS_TAG", "Admob: Native : $adName : onAdFailedToLoad()\n$errorCode")
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.i("DP_ADS_TAG", "Admob: Native : $adName : onAdLoaded()")
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        Log.i("DP_ADS_TAG", "Admob: Native : $adName : onAdClicked()")
                        nativeAdCache[adName] = null
                        adLoadingState[adName] = false
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        Log.i("DP_ADS_TAG", "Admob: Native : $adName : onAdImpression()")
                        nativeAdCache[adName] = null
                        adLoadingState[adName] = false
                    }

                    override fun onAdOpened() {
                        super.onAdOpened()
                        Log.i("DP_ADS_TAG", "Admob: Native : $adName : onAdOpened()")
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        Log.i("DP_ADS_TAG", "Admob: Native : $adName : onAdClosed()")
                    }
                })
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } else {
            onAdFailed?.invoke()
        }
    }

    private fun showCachedAd(adName: String, isMedia: Boolean, adContainer: CardView?, isMediumAd: Boolean) {
        adContainer?.context?.let { context ->
            nativeAdCache[adName]?.let { cachedAd ->
                val adView = LayoutInflater.from(context).inflate(
                    when {
                        isMedia && isMediumAd -> R.layout.admob_native_mediaview_large
                        isMedia -> R.layout.admob_native_mediaview_medium
                        isMediumAd -> R.layout.admob_native_simple_large
                        else -> R.layout.admob_native_simple_small
                    },
                    null
                ) as? NativeAdView ?: return

                populateNativeAd(isMediumAd, cachedAd, adView, isMedia)
                adContainer.removeAllViews()
                adContainer.addView(adView)
            } ?: run {
                Log.i("DP_ADS_TAG", "Ad is not available in cache for adName: $adName")
            }
        } ?: Log.i("DP_ADS_TAG", "Ad container or context is null; cannot load ad.")
    }


    private fun populateNativeAd(
        isMediumAd: Boolean,
        nativeAd: NativeAd,
        adView: NativeAdView,
        hasMediaView: Boolean
    ) {
        adView.headlineView = adView.findViewById(R.id.adHeadline)
        adView.bodyView = adView.findViewById(R.id.adBody)
        adView.callToActionView = adView.findViewById(R.id.adCallToAction)
        adView.iconView = adView.findViewById(R.id.adAppIcon)

        (adView.headlineView as TextView).text = nativeAd.headline

        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            handleIconVisibility(isMediumAd, adView, false)
        } else {
            handleIconVisibility(isMediumAd, adView, true)
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon!!.drawable)
            adView.iconView?.visibility = View.VISIBLE
        }

        if (hasMediaView) {
            configureMediaView(nativeAd, adView)
        }

        adView.setNativeAd(nativeAd)
        adView.visibility = View.VISIBLE
    }

    private fun handleIconVisibility(isMediumAd: Boolean, adView: NativeAdView, isVisible: Boolean) {
        val guidelineId = if (isMediumAd) R.id.glNativeAdmobMedium1 else R.id.glNativeAdmobBannerNormal1
        val guidelinePercent = if (isVisible) if (isMediumAd) 0.17f else 0.15f else 0f

        adView.findViewById<Guideline>(guidelineId).setGuidelinePercent(guidelinePercent)
        adView.findViewById<CardView>(R.id.adIconCard).visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun configureMediaView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById<View>(R.id.adMedia) as MediaView
        adView.mediaView?.mediaContent = nativeAd.mediaContent!!

        (adView.findViewById<View>(R.id.adMedia) as MediaView).setOnHierarchyChangeListener(object :
            ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                if (child is ImageView) {
                    child.scaleType = ImageView.ScaleType.FIT_XY
                }
            }

            override fun onChildViewRemoved(view: View, view1: View) {}
        })

        val videoController = nativeAd.mediaContent!!.videoController
        if (videoController.hasVideoContent()) {
            videoController.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {

            }
        }
    }
}