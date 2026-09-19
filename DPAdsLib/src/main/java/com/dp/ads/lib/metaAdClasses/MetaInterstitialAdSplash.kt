package com.dp.ads.lib.metaAdClasses

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.R
import com.dp.ads.lib.callingClasses.AdsFreeManager
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
    // Unlike AdMob's InterstitialAd (only assigned inside its onAdLoaded callback,
    // so "interstitialAd == null" correctly means "still loading"), Meta's
    // InterstitialAd is constructed synchronously in fetchAd() before loadAd() is
    // even called, so it's already non-null well before the timeout could ever
    // fire. That made the timeout below dead code: it could never detect a stalled
    // load, so a silently-stuck Meta request left the user on the splash screen
    // with no way out. This flag tracks the real "load finished (success or
    // error)" state instead.
    private var adLoadCompleted = false
    private val timeoutHandler = Handler(Looper.getMainLooper())

    private val timeoutRunnable = Runnable {
        if (!adLoadCompleted) {
            adLoadCompleted = true
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

        if (currentActivity != null && AdsFreeManager.isAdFreeActive(currentActivity!!)) {
            onAdFailed?.invoke()
            return
        }

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
                MetaInterstitialInside.isInterstitialAdVisible = false
                interstitialAd = null
            }

            override fun onError(ad: Ad?, adError: AdError) {
                Log.i("DP_ADS_TAG", "Meta: Interstitial : onError() - ${adError.errorMessage}")
                adLoadCompleted = true
                MetaInterstitialInside.isInterstitialAdVisible = false
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
                adLoadCompleted = true
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
                // Now that the timeout above can actually fire, the app may have
                // already moved past this splash (proceedNext()) by the time a
                // late-arriving load finishes here. Guard against calling show()
                // on a finished/destroyed activity, matching the same check
                // AdmobInterstitialAdSplash already has.
                currentActivity?.let { activity ->
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        MetaInterstitialInside.isInterstitialAdVisible = true
                        interstitialAd?.show()
                    }
                }
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