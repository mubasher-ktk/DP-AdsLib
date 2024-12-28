package com.dp.ads.lib.vungleAdClasses

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.vungle.ads.AdConfig
import com.vungle.ads.BaseAd
import com.vungle.ads.BaseAdListener
import com.vungle.ads.InterstitialAd
import com.vungle.ads.VungleError

class VungleInterstitialAdSplash(
    activity: Activity? = null,
    private val placementId: String,
    private val onAdDismissed: (() -> Unit)? = null,
    private val onAdFailed: (() -> Unit)? = null,
    private val onAdTimeout: (() -> Unit)? = null,
    private val onAdShowed: (() -> Unit)? = null) {
    private var interstitialAd: InterstitialAd? = null
    private var currentActivity: Activity? = activity
    var isShowDialog = true
    private var isShowingDialog = false
    var isShowingAd = false

    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        if (interstitialAd == null) {
            onAdTimeout?.invoke()
            dismissWaitDialog()
            Log.i("DP_ADS_TAG", "Vungle: InterstitialAd : Timeout()")
            currentActivity?.let {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(it, "Vungle Interstitial Ad Timeout", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    init {
        currentActivity?.let {
            if (it.localClassName.isNotEmpty()) {
                loadAd(onAdDismissed, onAdFailed, onAdShowed)
            }
        }
    }

    private fun loadAd(onAdDismissed: (() -> Unit)? = null, onAdFailed: (() -> Unit)? = null, onAdShowed: (() -> Unit)? = null) {
        if (isAdAvailable()) {
            return
        }

        currentActivity?.let {
            if (!NetworkCheck.isNetworkAvailable(it)) {
                return
            }
        }

        interstitialAd = InterstitialAd(currentActivity!!, placementId, AdConfig()).apply {
            adListener = object : BaseAdListener {
                override fun onAdLoaded(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: InterstitialAd : onAdLoaded()")
                    showAdIfAvailable()
                }

                override fun onAdStart(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: InterstitialAd : onAdStart()")
                }

                override fun onAdEnd(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: InterstitialAd : onAdEnd()")
                    dismissWaitDialog()
                    isShowingAd = false
                    onAdDismissed?.invoke()
                }

                override fun onAdFailedToLoad(baseAd: BaseAd, adError: VungleError) {
                    Log.i("DP_ADS_TAG", "Vungle: InterstitialAd : onAdFailedToLoad: $adError")
                    onAdFailed?.invoke()
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(it, "Vungle Ad Failed to Load", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAdFailedToPlay(baseAd: BaseAd, adError: VungleError) {
                    Log.i("DP_ADS_TAG", "Vungle: InterstitialAd : onAdFailedToPlay: $adError")
                    onAdFailed?.invoke()
                    dismissWaitDialog()
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(it, "Vungle Ad Failed to Play", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAdClicked(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: InterstitialAd : onAdClicked()")
                }

                override fun onAdLeftApplication(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: InterstitialAd : onAdLeftApplication()")
                }

                override fun onAdImpression(baseAd: BaseAd) {
                    Log.i("DP_ADS_TAG", "Vungle: InterstitialAd : onAdImpression()")
                }
            }
            load()
        }
        timeoutHandler.postDelayed(timeoutRunnable, 20000)
    }

    private fun isAdAvailable(): Boolean {
        return interstitialAd?.canPlayAd() == true
    }

    private fun showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            showWaitDialog()

            Handler(Looper.getMainLooper()).postDelayed({
                interstitialAd?.play()
                dismissWaitDialog()
                isShowingAd = true
                onAdShowed?.invoke()
            }, 1500)
        }
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