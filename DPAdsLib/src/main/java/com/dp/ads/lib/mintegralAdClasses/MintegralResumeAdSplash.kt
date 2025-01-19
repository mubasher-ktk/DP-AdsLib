package com.dp.ads.lib.mintegralAdClasses

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck.Companion.isNetworkAvailable
import com.mbridge.msdk.out.MBSplashHandler
import com.mbridge.msdk.out.MBSplashLoadListener
import com.mbridge.msdk.out.MBSplashShowListener
import com.mbridge.msdk.out.MBridgeIds

class MintegralResumeAdSplash(
    activity: Activity? = null,
    private val placementId: String,
    private val unitId: String,
    private val canSkip: Boolean,
    private val timeoutSkip: Int,
    private val onAdDismissed: (() -> Unit)? = null,
    private val onAdFailed: (() -> Unit)? = null,
    private val onAdTimeout: (() -> Unit)? = null,
    private val onAdShowed: (() -> Unit)? = null) {

    private var currentActivity: Activity? = activity
    private var mMBSplashHandler: MBSplashHandler? = null
    private var isShowingAd = false
    private var isShowingDialog = false
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        if (mMBSplashHandler == null) {
            onAdTimeout?.invoke()
            Log.i("MintegralResumeAd", "Mintegral: Timeout()")
            currentActivity?.let {
                Toast.makeText(currentActivity, "Mintegral :: Timeout", Toast.LENGTH_SHORT).show()
            }
        }
    }

    init {
        fetchAd()
    }

    private fun fetchAd() {
        if (!isNetworkAvailable(currentActivity)) {
            Log.i("MintegralResumeAd", "No network available.")
            return
        }

        currentActivity?.let {
            if (BuildConfig.DEBUG) {
                Toast.makeText(currentActivity, "Mintegral: Resume : Request", Toast.LENGTH_SHORT).show()
            }
        }

        mMBSplashHandler = MBSplashHandler(currentActivity, placementId, unitId, canSkip, timeoutSkip).apply {
            setLoadTimeOut(20000)
            setSplashLoadListener(object : MBSplashLoadListener {
                override fun onLoadSuccessed(ids: MBridgeIds, reqType: Int) {
                    Log.i("MintegralResumeAd", "onLoadSuccess: $reqType $ids")
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(currentActivity, "Mintegral: Resume : Loaded", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showAdIfAvailable()
                }

                override fun onLoadFailed(ids: MBridgeIds, msg: String, reqType: Int) {
                    currentActivity?.let {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(currentActivity, "Mintegral: Resume : Request", Toast.LENGTH_SHORT).show()
                        }
                    }
                    dismissWaitDialog()
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    onAdFailed?.invoke()
                    Log.e("MintegralResumeAd", "onLoadFailed: $msg  $reqType $ids")
                }

                override fun isSupportZoomOut(ids: MBridgeIds, support: Boolean) {
                    Log.i("MintegralResumeAd", "isSupportZoomOut: $support $ids")
                }
            })
            setSplashShowListener(object : MBSplashShowListener {
                override fun onShowSuccessed(ids: MBridgeIds) {
                    Log.i("MintegralResumeAd", "onShowSuccess: $ids")
                    isShowingAd = true
                    dismissWaitDialog()
                    onAdShowed?.invoke()
                }

                override fun onShowFailed(ids: MBridgeIds, msg: String) {
                    Log.e("MintegralResumeAd", "onShowFailed: $msg $ids")
                    onAdFailed?.invoke()
                    dismissAdView()
                    dismissWaitDialog()
                }

                override fun onAdClicked(ids: MBridgeIds) {
                    Log.i("MintegralResumeAd", "onAdClicked: $ids")
                }

                override fun onDismiss(ids: MBridgeIds, type: Int) {
                    Log.i("MintegralResumeAd", "onDismiss: $type $ids")
                    onAdDismissed?.invoke()
                    dismissWaitDialog()
                    dismissAdView()
                }

                override fun onAdTick(ids: MBridgeIds, millisUntilFinished: Long) {
                    Log.i("MintegralResumeAd", "onAdTick: $millisUntilFinished $ids")
                }

                override fun onZoomOutPlayStart(ids: MBridgeIds) {
                    Log.i("MintegralResumeAd", "onZoomOutPlayStart: $ids")
                }

                override fun onZoomOutPlayFinish(ids: MBridgeIds) {
                    Log.i("MintegralResumeAd", "onZoomOutPlayFinish: $ids")
                }
            })
        }

        mMBSplashHandler?.preLoad()
        timeoutHandler.postDelayed(timeoutRunnable, 20000)
    }

    private fun showAdIfAvailable() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        Log.i("MintegralResumeAd", "Ad is available and being shown.")

        if (!isShowingAd) {
            Handler(Looper.getMainLooper()).postDelayed({
                showWaitDialog()
                Handler(Looper.getMainLooper()).postDelayed({
                    mMBSplashHandler?.show(currentActivity)
                }, 1500)
            },7000)
        }
    }

    private fun dismissAdView() {
        mMBSplashHandler?.onDestroy()
        isShowingAd = false
    }

    private fun showWaitDialog() {
        currentActivity?.let {
            val view = it.layoutInflater.inflate(R.layout.dialog_adloading, null, false)
            isShowingDialog = true
            AdLoadingDialog.setContentView(it, view = view, isCancelable = false).showDialogInterstitial()
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