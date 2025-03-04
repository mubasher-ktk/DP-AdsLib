package com.dp.ads.lib.metaAdClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dp.ads.lib.R
import com.dp.ads.lib.data.InterstitialMaster.interstitialMetaHashMap
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@SuppressLint("StaticFieldLeak")
object MetaInterstitialInside : CoroutineScope by MainScope() {
    private const val adShowingDelayTime = 1500
    private var isShowDialog = true
    var isInterstitialAdVisible = false

    private var mContextMeta: Context? = null
    private var onAdClosedCallBackMeta: (() -> Unit)? = null
    private var onAdLoadedCallBackMeta: (() -> Unit)? = null

    fun checkAndLoadMetaInterstitial(
        context: Context?,
        nameFragment: String,
        adId: String,
        onAdLoadedCallMeta: (() -> Unit)? = null
    ) {
        mContextMeta = context
        onAdLoadedCallBackMeta = onAdLoadedCallMeta

        if (NetworkCheck.isNetworkAvailable(mContextMeta)) {
            if (!interstitialMetaHashMap.containsKey(nameFragment)) {
                loadMetaInterstitial(nameFragment, adId)
            }
        } else {
            return
        }
    }

    private fun loadMetaInterstitial(nameFragment: String, adId: String) {
        Log.i("DP_ADS_TAG", "Requesting Meta Interstitial: $nameFragment")

        if (!interstitialMetaHashMap.containsKey(nameFragment)) {
            val interstitialAd = InterstitialAd(mContextMeta, adId)

            interstitialAd.loadAd(
                interstitialAd.buildLoadAdConfig()
                    .withAdListener(object : InterstitialAdListener {
                        override fun onInterstitialDisplayed(ad: Ad) {
                            isInterstitialAdVisible = true
                            Log.i("DP_ADS_TAG", "Meta Interstitial Displayed: $nameFragment")
                        }

                        override fun onInterstitialDismissed(ad: Ad) {
                            Log.i("DP_ADS_TAG", "Meta Interstitial Dismissed: $nameFragment")
                            isInterstitialAdVisible = false
                            onAdClosedCallBackMeta?.invoke()
                            interstitialMetaHashMap.remove(nameFragment)
                        }

                        override fun onError(ad: Ad, adError: AdError) {
                            Log.e("DP_ADS_TAG", "Meta Interstitial Failed to Load: $nameFragment. Error: ${adError.errorMessage}")
                            isInterstitialAdVisible = false
                            onAdClosedCallBackMeta?.invoke()
                            interstitialMetaHashMap.remove(nameFragment)
                        }

                        override fun onAdLoaded(ad: Ad) {
                            Log.i("DP_ADS_TAG", "Meta Interstitial Loaded: $nameFragment")
                            interstitialMetaHashMap[nameFragment] = interstitialAd
                            onAdLoadedCallBackMeta?.invoke()
                            onAdLoadedCallBackMeta = null
                        }

                        override fun onAdClicked(ad: Ad) {}
                        override fun onLoggingImpression(ad: Ad) {}
                    })
                    .build()
            )
        }
    }

    fun showIfAvailableOrLoadMetaInterstitial(
        context: Context?,
        nameFragment: String,
        adId: String,
        onAdClosedCallBackMeta: () -> Unit,
        onAdShowedCallBackMeta: () -> Unit
    ) {
        mContextMeta = context
        isShowDialog = true
        this.onAdClosedCallBackMeta = onAdClosedCallBackMeta

        if (interstitialMetaHashMap.containsKey(nameFragment)) {
            showMetaInterstitial(onAdShowedCallBackMeta, nameFragment)
        } else {
            Log.i("DP_ADS_TAG", "Ad not available. Requesting new Meta ad: $nameFragment")
            checkAndLoadMetaInterstitial(context, nameFragment, adId)
            onAdClosedCallBackMeta.invoke()
            this.onAdClosedCallBackMeta = null
        }
    }

    private fun showMetaInterstitial(onAdShowedCallBackMeta: () -> Unit, nameFragment: String) {
        showWaitDialog()
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                dismissWaitDialog()
                val interstitialAd = interstitialMetaHashMap[nameFragment]
                if (interstitialAd != null && interstitialAd.isAdLoaded) {
                    interstitialAd.show()
                    onAdShowedCallBackMeta.invoke()
                } else {
                    onAdClosedCallBackMeta?.invoke()
                }
            }, adShowingDelayTime.toLong())
        } catch (e: Exception) {
            dismissWaitDialog()
            Log.e("DP_ADS_TAG", "Error showing Meta Interstitial: ${e.message}")
        }
    }

    private fun showWaitDialog() {
        if (isShowDialog) {
            mContextMeta?.let {
                if (!(it as Activity).isFinishing && !it.isDestroyed) {
                    val view = it.layoutInflater.inflate(R.layout.dialog_adloading, null, false)
                    AdLoadingDialog.setContentView(it, view = view, isCancelable = false)
                        .showDialogInterstitial()
                }
            }
        }
    }

    private fun dismissWaitDialog() {
        mContextMeta?.let {
            if (!(it as Activity).isFinishing && !it.isDestroyed) {
                AdLoadingDialog.dismissDialog(it)
            }
        }
    }
}