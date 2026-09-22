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
import com.dp.ads.lib.callingClasses.AdsFreeManager
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.metaAdClasses.MetaInterstitialInside
import java.util.concurrent.atomic.AtomicBoolean

class ResumeAdApplication(val globalClass: Application?=null, val adId: String) : Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private var adVisible = false
    var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    var isShowDialog = true
    private var isShowingDialog = false
    var isShowingAd = false
//    private var myElephant: Application? = globalClass
    var fullScreenContentCallback: FullScreenContentCallback? = null
    private val fetchRequestInFlight = AtomicBoolean(false)
    private val showRequestInFlight = AtomicBoolean(false)
    private val fetchTimeoutHandler = Handler(Looper.getMainLooper())
    private val fetchTimeoutRunnable = Runnable {
        Log.i("DP_ADS_TAG", "Admob: Resume : fetchAd() timed out (20s), resetting in-flight guard")
        fetchRequestInFlight.set(false)
    }

    companion object {
        // The single app-wide owner of the ADMOB_SPLASH_RESUME ad unit, once one is
        // constructed. DPAdsConfigurations.showAdMobResumeAdSplash() routes through
        // this instead of building its own separate AdmobResumeAdSplash, so the
        // first-open flow and the background/foreground resume flow never load or
        // show the same ad unit independently of each other.
        @Volatile var instance: ResumeAdApplication? = null
    }

    init {
        instance = this
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

    // Called from the first-open flow (DPAdsConfigurations.showAdMobResumeAdSplash())
    // instead of that flow building its own AdmobResumeAdSplash for the same ad unit.
    // currentActivity is set here because on a genuine cold start this instance can be
    // constructed before any ActivityLifecycleCallbacks/ProcessLifecycleOwner event has
    // delivered an activity yet, which otherwise leaves fetchAd() a no-op from init{}.
    fun showForFirstOpen(activity: Activity, timeoutMs: Long = 20000, onFinished: () -> Unit) {
        if (globalClass != null && AdsFreeManager.isAdFreeActive(globalClass)) {
            onFinished()
            return
        }
        currentActivity = activity
        fetchAd()

        val resolved = AtomicBoolean(false)
        fun finishOnce() {
            if (!resolved.getAndSet(true)) onFinished()
        }
        fun tryShowOrFinish() {
            // isShowingAd/showRequestInFlight true means another path (e.g. the
            // lifecycle-driven onAppForegrounded()) already owns showing this ad right
            // now - showAdIfAvailable() would otherwise silently no-op without ever
            // calling onFinished(), so treat that as an immediate resolution instead.
            if (isShowingAd || showRequestInFlight.get()) {
                finishOnce()
                return
            }
            if (isAdAvailable()) {
                showAdIfAvailable(onAdNotAvailableOrShown = { finishOnce() })
            } else {
                finishOnce()
            }
        }

        if (isAdAvailable() || isShowingAd || showRequestInFlight.get()) {
            tryShowOrFinish()
            return
        }

        val waitHandler = Handler(Looper.getMainLooper())
        val deadline = System.currentTimeMillis() + timeoutMs
        val waitRunnable = object : Runnable {
            override fun run() {
                if (resolved.get()) return
                when {
                    isAdAvailable() || isShowingAd || showRequestInFlight.get() -> tryShowOrFinish()
                    System.currentTimeMillis() >= deadline -> finishOnce()
                    else -> waitHandler.postDelayed(this, 250)
                }
            }
        }
        waitHandler.postDelayed(waitRunnable, 250)
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

        if (!fetchRequestInFlight.compareAndSet(false, true)) {
            Log.i("DP_ADS_TAG", "Admob: Resume : request already in-flight, skipping duplicate fetchAd()")
            return
        }

        val loadCallback: AppOpenAd.AppOpenAdLoadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                fetchTimeoutHandler.removeCallbacks(fetchTimeoutRunnable)
                fetchRequestInFlight.set(false)
                appOpenAd = ad
                globalClass.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(globalClass, "OpenAd :: AdMob :: Loaded", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                fetchTimeoutHandler.removeCallbacks(fetchTimeoutRunnable)
                fetchRequestInFlight.set(false)
                globalClass.let {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(globalClass, "OpenAd :: AdMob :: Failed to Load", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        val request: AdRequest = getAdRequest()

        globalClass.applicationContext?.apply {
            AppOpenAd.load(
                this,
                adId,
                request,
                loadCallback
            )
            if (BuildConfig.DEBUG) {
                Toast.makeText(globalClass, "OpenAd :: AdMob :: Request", Toast.LENGTH_SHORT).show()
            }
        }
        fetchTimeoutHandler.postDelayed(fetchTimeoutRunnable, 20000)
    }

    fun showAdIfAvailable(onAdNotAvailableOrShown: (() -> Unit)? = null) {
        if (!isShowingAd && isAdAvailable()) {
            if (!showRequestInFlight.compareAndSet(false, true)) {
                Log.i("DP_ADS_TAG", "Admob: Resume : show already in-flight, skipping duplicate showAdIfAvailable()")
                return
            }
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    showRequestInFlight.set(false)
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
                    showRequestInFlight.set(false)
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
                    showRequestInFlight.set(false)
                    isShowingAd = true
                    isShowDialog = false
                    dismissWaitDialog()
                }
            }
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            if (!AdMobInterstitialInside.isInterstitialAdVisible && !MetaInterstitialInside.isInterstitialAdVisible) {
                adVisible = true
                isShowDialog = true
                showWaitDialog()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (currentActivity != null && !(currentActivity as Activity).isFinishing) {
                        appOpenAd!!.show(currentActivity!!)
                    } else {
                        Log.i("DP_ADS_TAG", "Admob: Resume : skipped show(), currentActivity is null/finishing")
                        showRequestInFlight.set(false)
                        onAdNotAvailableOrShown?.invoke()
                    }
                    dismissWaitDialog()
                }, 1500)
            } else {
                Log.i("DP_ADS_TAG", "ResumeAdApplication : skipped show(), currentActivity is null/finishing or an interstitial is visible")
                showRequestInFlight.set(false)
            }
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
        if (globalClass != null && AdsFreeManager.isAdFreeActive(globalClass)) {
            return
        }
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