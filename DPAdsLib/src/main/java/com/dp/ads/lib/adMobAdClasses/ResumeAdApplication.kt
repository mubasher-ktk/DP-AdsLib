package com.dp.ads.lib.adMobAdClasses

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.metaAdClasses.MetaInterstitialInside

class ResumeAdApplication(val globalClass: Application?=null, val adId: String) : Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private var adVisible = false
    var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    var isShowDialog = true
    private var isShowingDialog = false
    var isShowingAd = false
//    private var myElephant: Application? = globalClass
    var fullScreenContentCallback: FullScreenContentCallback? = null

    init {
        globalClass.let {
            this.globalClass?.registerActivityLifecycleCallbacks(this)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
        currentActivity.let {
            if (currentActivity?.localClassName != null || currentActivity?.localClassName.equals("")) {
                fetchAd()
            }
        }
    }

    fun fetchAd() {
        if (isAdAvailable()) {
            return
        }

        if (globalClass != null) {
            if (!NetworkCheck.isNetworkAvailable(globalClass)) {
                return
            }
        } else {
            return
        }

        val loadCallback: AppOpenAd.AppOpenAdLoadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                globalClass.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(globalClass, "OpenAd :: AdMob :: Loaded", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                globalClass.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(globalClass, "OpenAd :: AdMob :: Failed to Load", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        val request: AdRequest = getAdRequest()

        globalClass?.applicationContext?.apply {
            AppOpenAd.load(
                this,
                adId,
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                loadCallback
            )
            if (BuildConfig.DEBUG) {
                Toast.makeText(globalClass, "OpenAd :: AdMob :: Request", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showAdIfAvailable(onAdNotAvailableOrShown: (() -> Unit)? = null) {
        if (!isShowingAd && isAdAvailable()) {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isShowDialog = false
                    dismissWaitDialog()
                    onAdNotAvailableOrShown.let {
                        onAdNotAvailableOrShown?.invoke()
                    }
                    appOpenAd = null
                    isShowingAd = false
                    adVisible = false
                    fetchAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    isShowDialog = false
                    dismissWaitDialog()
                    onAdNotAvailableOrShown.let {
                        onAdNotAvailableOrShown?.invoke()
                    }
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(globalClass, "OpenAd :: AdMob :: onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                    isShowDialog = false
                    dismissWaitDialog()
                }
            }
            adVisible = true
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            isShowDialog = true
            showWaitDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                appOpenAd!!.show(currentActivity!!)
                dismissWaitDialog()
            }, 1500)
        } else {
            isShowDialog = false
            dismissWaitDialog()
            if (BuildConfig.DEBUG) {
                Toast.makeText(globalClass, "OpenAd :: AdMob :: Not Available", Toast.LENGTH_SHORT).show()
            }
            onAdNotAvailableOrShown.let {
                onAdNotAvailableOrShown?.invoke()
            }
            if (currentActivity?.localClassName != null || currentActivity?.localClassName.equals("")) {
                fetchAd()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        if (currentActivity?.localClassName != null || currentActivity?.localClassName.equals("")) {
            if (!AdMobInterstitialInside.isInterstitialAdVisible && !MetaInterstitialInside.isInterstitialAdVisible) {
                showAdIfAvailable()
            }
        }
    }

    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {
        dismissWaitDialog()
    }

    override fun onActivityStopped(p0: Activity) {
        dismissWaitDialog()
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
        dismissWaitDialog()
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
                isShowingDialog = false
                AdLoadingDialog.dismissDialog(currentActivity!!)
            }
        }
    }
}