package com.dp.ads.lib.adMobAdClasses

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AdmobResumeAdSplash(activity: Activity?=null, val adId: String, onAdDismissed: (() -> Unit)? = null, onAdFailed: (() -> Unit)? = null, onAdTimeout: (() -> Unit)? = null, onAdShowed: (() -> Unit)? = null) {

    var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = activity
    var isShowDialog = true
    private var isShowingDialog = false
    var isShowingAd = false
    private var fullScreenContentCallback: FullScreenContentCallback? = null

    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        if (appOpenAd == null) {
            onAdTimeout?.invoke()
            dismissWaitDialog()
            Log.i("DP_ADS_TAG", "Admob: Resume : Timeout()")
            currentActivity?.let {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(currentActivity, "OpenAd :: AdMob :: Timeout", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    init {
        currentActivity.let {
            if (currentActivity?.localClassName != null || currentActivity?.localClassName.equals("")) {
                fetchAd(onAdDismissed, onAdFailed, onAdShowed)
            }
        }
    }

    private fun fetchAd(onAdDismissed: (() -> Unit)? = null, onAdFailed: (() -> Unit)? = null, onAdShowed: (() -> Unit)? = null) {
        if (isAdAvailable()) {
            return
        }

        if (currentActivity != null) {
            if (!NetworkCheck.isNetworkAvailable(currentActivity)) {
                return
            }
        } else {
            return
        }

        val loadCallback: AppOpenAd.AppOpenAdLoadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.i("DP_ADS_TAG","Admob: Resume : onAdLoaded()")
                appOpenAd = ad
                if (currentActivity?.localClassName != null || currentActivity?.localClassName.equals("")) {
                    showAdIfAvailable(onAdDismissed, onAdFailed, onAdShowed)
                }
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                timeoutHandler.removeCallbacks(timeoutRunnable)
                onAdFailed.let {
                    onAdFailed?.invoke()
                }
                Log.i("DP_ADS_TAG", "Admob: Resume : onAdFailedToLoad()")
                currentActivity.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(currentActivity,"OpenAd :: AdMob :: Failed to load", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val request: AdRequest = getAdRequest()
        currentActivity!!.applicationContext.let {
            AppOpenAd.load(
                currentActivity!!.applicationContext,
                adId,
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                loadCallback)
        }
        timeoutHandler.postDelayed(timeoutRunnable,20000)
    }

    fun showAdIfAvailable(onAdDismissed: (() -> Unit)? = null, onAdFailed: (() -> Unit)? = null, onAdShowed: (() -> Unit)? = null) {
        if (!isShowingAd && isAdAvailable()) {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.i("DP_ADS_TAG","Admob: Resume : onAdDismissedFullScreenContent()")
                    isShowDialog = false
                    dismissWaitDialog()
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    onAdDismissed.let {
                        onAdDismissed?.invoke()
                    }
                    appOpenAd = null
                    isShowingAd = false
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(currentActivity,"OpenAd :: AdMob :: onAdDismissedFullScreenContent()", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Log.i("DP_ADS_TAG","Admob: Resume : onAdFailedToShowFullScreenContent()")
                    isShowDialog = false
                    dismissWaitDialog()
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    onAdFailed.let {
                        onAdFailed?.invoke()
                    }
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(currentActivity,"OpenAd :: AdMob :: Failed to show", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onAdShowedFullScreenContent() {
                    Log.i("DP_ADS_TAG","Admob: Resume : onAdShowedFullScreenContent()")
                    isShowingAd = true
                    isShowDialog = false
                    dismissWaitDialog()
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    onAdShowed.let {
                        onAdShowed?.invoke()
                    }
                }
            }
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            isShowDialog = true
            showWaitDialog()

            Handler(Looper.getMainLooper()).postDelayed({
                appOpenAd!!.show(currentActivity!!)
                dismissWaitDialog()
            },1500)
        }
    }

    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

    private fun showWaitDialog() {
        Log.i("DP_ADS_TAG", "Admob: Resume : showWaitDialog()")
        if (isShowingDialog) {
            currentActivity?.let {
                if (!(currentActivity as Activity).isFinishing) {
                    AdLoadingDialog.dismissDialog(currentActivity!!)
                }
            }
        }
        if (isShowDialog) {
            currentActivity?.let {
                if(!(currentActivity as Activity).isFinishing) {
                    val view = (currentActivity as Activity).layoutInflater.inflate(
                        R.layout.dialog_adloading,
                        null,
                        false)
                    isShowingDialog = true
                    AdLoadingDialog.setContentView(currentActivity!!, view = view, isCancelable = false).showDialogInterstitial()
                }
            }
        }
    }

    private fun dismissWaitDialog() {
        Log.i("DP_ADS_TAG", "Admob: Resume : dismissWaitDialog()")
        currentActivity?.let {
            if (!(currentActivity as Activity).isFinishing) {
                AdLoadingDialog.dismissDialog(currentActivity!!)
            }
        }
    }
}