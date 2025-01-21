package com.dp.ads.lib.adMobAdClasses

import android.app.Activity
import android.util.Log
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
import com.dp.ads.lib.utils.ForegroundCheckTask
import com.dp.ads.lib.utils.NetworkCheck
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import java.util.HashMap

object AdmobNativeAdClass {
    var nativeAdMobHashMap: HashMap<String, NativeAd>? = HashMap()
    fun checkAdRequestAdmob(
        mContext: Activity?,
        adId: String,
        fragmentName: String = "",
        isMedia: Boolean,
        isMediaOnRight: Boolean = true,
        overallNativeReloading: String = "SAVE",
        adContainer: CardView?,
        isMediumAd: Boolean = false,
        onFailed: () -> Unit,
        onAddLoaded: (() -> Unit)? = null
    ) {
        lateinit var defaultAdviewBanner: NativeAdView

        if (isMedia) {
            if (mContext != null) {
                defaultAdviewBanner = if (isMediaOnRight) {
                    mContext.layoutInflater.inflate(
                        R.layout.native_admob_media_right_side,
                        null
                    ) as NativeAdView
                } else {
                    if (isMediumAd) {
                        mContext.layoutInflater.inflate(
                            R.layout.native_admob_large_medium,
                            null
                        ) as NativeAdView
                    } else {
                        mContext.layoutInflater.inflate(
                            R.layout.native_admob_large_normal,
                            null
                        ) as NativeAdView
                    }
                }
            }
        } else {
            if (mContext != null) {
                defaultAdviewBanner = if (isMediumAd) {
                    mContext.layoutInflater.inflate(
                        R.layout.native_admob_banner_small,
                        null
                    ) as NativeAdView
                } else {
                    mContext.layoutInflater.inflate(
                        R.layout.native_admob_banner_normal,
                        null
                    ) as NativeAdView
                }
            }
        }

        if (NetworkCheck.isNetworkAvailable(mContext)) {
            if (mContext != null) {
                if (adContainer != null) {
                    adContainer.visibility = View.VISIBLE
                }
                if (nativeAdMobHashMap!!.containsKey(fragmentName)) {
                    onAddLoaded?.let {
                        onAddLoaded.invoke()
                    }
                    val nativeAd: NativeAd? = nativeAdMobHashMap!![fragmentName]
                    populateNativeAd(isMediaOnRight, isMediumAd, nativeAd!!, defaultAdviewBanner, isMedia)
                    adContainer?.removeAllViews()
                    adContainer?.addView(defaultAdviewBanner)
                } else {
                    if (adContainer != null) {
                        if (ForegroundCheckTask().execute(mContext).get()) {
                            loadNativeAdmob(mContext, adId, fragmentName, adContainer, isMedia, isMediaOnRight, overallNativeReloading, defaultAdviewBanner, isMediumAd, onAddLoaded)
                        }
                    }
                }
            } else {
                onFailed()
            }
        } else {
            onFailed()
        }
    }

    private fun loadNativeAdmob(
        mContext: Activity?,
        adId: String,
        fragmentName: String,
        frameLayout: CardView,
        isMedia: Boolean,
        isMediaOnRight: Boolean,
        overallNativeReloading: String,
        defaultAdview: NativeAdView,
        isMediumAd: Boolean,
        onAddLoaded: (() -> Unit)? = null
    ) {
        val builder: AdLoader.Builder = AdLoader.Builder(mContext!!, adId)
        frameLayout.visibility = View.VISIBLE
        builder.forNativeAd { mNativeAd: NativeAd ->
            if (mContext != null) {
                if (overallNativeReloading.equals("SAVE")) {
                    nativeAdMobHashMap!![fragmentName] = mNativeAd
                }
                populateNativeAd(isMediaOnRight, isMediumAd, mNativeAd, defaultAdview, isMedia)
                frameLayout.removeAllViews()
                frameLayout.addView(defaultAdview)
            }
        }
        val videoOptions: VideoOptions = VideoOptions.Builder()
            .setStartMuted(false)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .setVideoOptions(videoOptions)
            .build()
        builder.withNativeAdOptions(adOptions)
        mContext.let {
            if (BuildConfig.DEBUG) {
                Toast.makeText(mContext, "Native :: AdMob :: Requesting", Toast.LENGTH_SHORT).show()
            }
        }
        Log.i("DP_ADS_TAG", "Native :: AdMob :: Requesting")
        val adLoader: AdLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(errorCode: LoadAdError) {
                Log.i("DP_ADS_TAG", "Admob Native onAdFailedToLoad..")
                mContext.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(mContext,"Native :: AdMob :: Failed To Load", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.i("DP_ADS_TAG", "Admob Native onAdClicked..")
                if (nativeAdMobHashMap!!.containsKey(fragmentName)) {
                    nativeAdMobHashMap!!.remove(fragmentName)
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                onAddLoaded.let {
                    onAddLoaded?.invoke()
                }
                mContext.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(mContext, "Native :: AdMob :: Loaded", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.i("DP_ADS_TAG", "Admob Native onAdLoaded..")
                frameLayout.visibility = View.VISIBLE
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAd(
        isMediaOnRight: Boolean,
        isMediumAd: Boolean,
        nativeAd: NativeAd,
        adView: NativeAdView,
        hasMediaView: Boolean
    ) {
        Log.i("DP_ADS_TAG", "Admob Native populateNativeAd..")
        adView.headlineView = adView.findViewById(R.id.adHeadline)
        adView.bodyView = adView.findViewById(R.id.adBody)
        adView.callToActionView = adView.findViewById(R.id.adCallToAction)
        adView.iconView = adView.findViewById(R.id.adAppIcon)

        if (nativeAd.headline == null) {
            adView.headlineView?.visibility = View.INVISIBLE
        } else {
            adView.headlineView?.visibility = View.VISIBLE
            (adView.headlineView as TextView).text = nativeAd.headline
        }

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
            handleIconVisibility(isMediaOnRight, isMediumAd, adView, false)
        } else {
            handleIconVisibility(isMediaOnRight, isMediumAd, adView, true)
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon!!.drawable)
            adView.iconView?.visibility = View.VISIBLE
        }

        if (hasMediaView) {
            configureMediaView(nativeAd, adView)
        }

        adView.setNativeAd(nativeAd)
        adView.visibility = View.VISIBLE
    }

    private fun handleIconVisibility(
        isMediaOnRight: Boolean,
        isMediumAd: Boolean,
        adView: NativeAdView,
        isVisible: Boolean
    ) {
        if (!isMediaOnRight) {
            val guidelineId = if (isMediumAd) R.id.glNativeAdmobMedium1 else R.id.glNativeAdmobBannerNormal1
            val guidelinePercent = if (isVisible) if (isMediumAd) 0.17f else 0.15f else 0f
            adView.findViewById<Guideline>(guidelineId).setGuidelinePercent(guidelinePercent)
        }
        adView.findViewById<CardView>(R.id.adIconCard).visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun configureMediaView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById<View>(R.id.adMedia) as MediaView
        adView.mediaView?.mediaContent = nativeAd.mediaContent!!

        (adView.findViewById<View>(R.id.adMedia) as MediaView).setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                if (child is ImageView) {
                    child.scaleType = ImageView.ScaleType.FIT_XY
                }
            }

            override fun onChildViewRemoved(view: View, view1: View) {}
        })

        val videoController = nativeAd.mediaContent!!.videoController
        if (videoController.hasVideoContent()) {
            videoController.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() { }
        }
    }
}