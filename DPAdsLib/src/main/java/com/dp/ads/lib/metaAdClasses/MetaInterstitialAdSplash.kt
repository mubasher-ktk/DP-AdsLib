package com.dp.ads.lib.metaAdClasses

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener

class MetaInterstitialAdSplash(
    activity: Activity? = null,
    val adId: String,
    onAdDismissed: (() -> Unit)? = null,
    onAdFailed: (() -> Unit)? = null,
    onAdTimeout: (() -> Unit)? = null,
    onAdShowed: (() -> Unit)? = null
) {
    private var interstitialAd: InterstitialAd? = null
    private var currentActivity: Activity? = activity
    private var isShowingAd = false
    private var isShowingDialog = false
    private var isShowDialog = true
    private val timeoutHandler = Handler(Looper.getMainLooper())

    private val timeoutRunnable = Runnable {
        if (interstitialAd == null) {
            onAdTimeout?.invoke()
            dismissWaitDialog()
            Log.i("DP_ADS_TAG", "Meta: Interstitial : Timeout()")
            currentActivity.let {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(currentActivity,"Meta: Interstitial : Timeout()", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    init {
        currentActivity?.let {
            fetchAd(onAdDismissed, onAdFailed, onAdShowed)
        }
    }

    private fun fetchAd(
        onAdDismissed: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null,
        onAdShowed: (() -> Unit)? = null
    ) {
        if (isAdAvailable()) return

        if (!NetworkCheck.isNetworkAvailable(currentActivity)) {
            return
        }

        interstitialAd = InterstitialAd(currentActivity, adId)
        val interstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad?) {
                Log.i("DP_ADS_TAG", "Meta: Interstitial : onInterstitialDisplayed()")
                timeoutHandler.removeCallbacks(timeoutRunnable)
                dismissWaitDialog()
                onAdShowed?.invoke()
                isShowingAd = true
            }

            override fun onInterstitialDismissed(ad: Ad?) {
                Log.i("DP_ADS_TAG", "Meta: Interstitial : onInterstitialDismissed()")
                timeoutHandler.removeCallbacks(timeoutRunnable)
                dismissWaitDialog()
                onAdDismissed?.invoke()
                isShowingAd = false
                interstitialAd = null
            }

            override fun onError(ad: Ad?, adError: AdError) {
                Log.i("DP_ADS_TAG", "Meta: Interstitial : onError() - ${adError.errorMessage}")
                timeoutHandler.removeCallbacks(timeoutRunnable)
                dismissWaitDialog()
                onAdFailed?.invoke()
                currentActivity.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(currentActivity,"Meta: Interstitial : Failed To Load", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAdLoaded(ad: Ad?) {
                Log.i("DP_ADS_TAG", "Meta: Interstitial : onAdLoaded()")
                timeoutHandler.removeCallbacks(timeoutRunnable)
                showAdIfAvailable()
            }

            override fun onAdClicked(ad: Ad?) {}
            override fun onLoggingImpression(ad: Ad?) {}
        }

        interstitialAd?.loadAd(
            interstitialAd?.buildLoadAdConfig()
                ?.withAdListener(interstitialAdListener)
                ?.build()
        )

        timeoutHandler.postDelayed(timeoutRunnable, 20000)
        showWaitDialog()
    }

    private fun showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            Handler(Looper.getMainLooper()).postDelayed({
                interstitialAd?.show()
            }, 1500)
        }
    }

    private fun isAdAvailable(): Boolean {
        return interstitialAd?.isAdLoaded == true && !interstitialAd?.isAdInvalidated!!
    }

    private fun showWaitDialog() {
        if (isShowDialog) {
            currentActivity?.let {
                val view = it.layoutInflater.inflate(R.layout.dialog_adloading, null, false)
                isShowingDialog = true
                AdLoadingDialog.setContentView(it, view = view, isCancelable = false).showDialogInterstitial()
            }
        }
    }

    private fun dismissWaitDialog() {
        if (isShowingDialog) {
            currentActivity?.let {
                AdLoadingDialog.dismissDialog(it)
            }
            isShowingDialog = false
        }
    }
}