package com.dp.ads.lib.mintegralAdClasses

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.dp.ads.lib.BuildConfig
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.mbridge.msdk.newinterstitial.out.MBNewInterstitialHandler
import com.mbridge.msdk.newinterstitial.out.NewInterstitialListener
import com.mbridge.msdk.out.MBridgeIds
import com.mbridge.msdk.out.RewardInfo

class MintegralInterstitialAdSplash(
    activity: Activity? = null,
    private val placementId: String,
    private val unitId: String,
    onAdDismissed: (() -> Unit)? = null,
    onAdFailed: (() -> Unit)? = null,
    onAdTimeout: (() -> Unit)? = null,
    onAdShowed: (() -> Unit)? = null
) {
    private var mMBNewInterstitialHandler: MBNewInterstitialHandler? = null
    private var currentActivity: Activity? = activity
    private var isShowingAd = false
    private var isShowingDialog = false
    private val timeoutHandler = Handler(Looper.getMainLooper())

    private val timeoutRunnable = Runnable {
        if (mMBNewInterstitialHandler == null || !mMBNewInterstitialHandler!!.isReady) {
            onAdTimeout?.invoke()
            dismissWaitDialog()
            Log.i("DP_ADS_TAG", "Mintegral: Interstitial : Timeout()")
            currentActivity?.let {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(it, "Mintegral: Interstitial : Timeout", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    init {
        currentActivity?.let {
            setupAd(onAdDismissed, onAdFailed, onAdShowed)
        }
    }

    private fun setupAd(
        onAdDismissed: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null,
        onAdShowed: (() -> Unit)? = null
    ) {
        if (!NetworkCheck.isNetworkAvailable(currentActivity)) {
            return
        }

        currentActivity?.let {
            if (BuildConfig.DEBUG) {
                Toast.makeText(it, "Mintegral: Interstitial : Request", Toast.LENGTH_SHORT).show()
            }
        }

        mMBNewInterstitialHandler = MBNewInterstitialHandler(currentActivity, placementId, unitId)
        mMBNewInterstitialHandler?.setInterstitialVideoListener(object : NewInterstitialListener {
            override fun onLoadCampaignSuccess(ids: MBridgeIds) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onLoadCampaignSuccess()")
            }

            override fun onResourceLoadSuccess(ids: MBridgeIds) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onResourceLoadSuccess()")
                showAdIfAvailable()
            }

            override fun onResourceLoadFail(ids: MBridgeIds, errorMsg: String) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onResourceLoadFail()")
                timeoutHandler.removeCallbacks(timeoutRunnable)
                dismissWaitDialog()
                onAdFailed?.invoke()
            }

            override fun onShowFail(ids: MBridgeIds, errorMsg: String) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onShowFail()")
                isShowingAd = false
                timeoutHandler.removeCallbacks(timeoutRunnable)
                dismissWaitDialog()
                onAdFailed?.invoke()
            }

            override fun onAdShow(ids: MBridgeIds) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onAdShow()")
                isShowingAd = true
                timeoutHandler.removeCallbacks(timeoutRunnable)
                dismissWaitDialog()
                onAdShowed?.invoke()
            }

            override fun onAdClose(ids: MBridgeIds, info: RewardInfo) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onAdClose()")
                isShowingAd = false
                timeoutHandler.removeCallbacks(timeoutRunnable)
                dismissWaitDialog()
                onAdDismissed?.invoke()
            }

            override fun onAdClicked(ids: MBridgeIds) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onAdClicked()")
            }

            override fun onVideoComplete(ids: MBridgeIds) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onVideoComplete()")
            }

            override fun onAdCloseWithNIReward(ids: MBridgeIds, info: RewardInfo) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onAdCloseWithNIReward()")
            }

            override fun onEndcardShow(ids: MBridgeIds) {
                Log.i("DP_ADS_TAG", "Mintegral: Interstitial : onEndCardShow()")
            }
        })

        mMBNewInterstitialHandler?.load()
        timeoutHandler.postDelayed(timeoutRunnable, 20000)
    }

    private fun showAdIfAvailable() {
        if (!isShowingAd && mMBNewInterstitialHandler?.isReady == true) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (mMBNewInterstitialHandler != null) {
                    showWaitDialog()
                    Handler(Looper.getMainLooper()).postDelayed({
                        mMBNewInterstitialHandler?.show()
                    }, 1500)
                }
            },7000)
        }
    }

    private fun showWaitDialog() {
        currentActivity?.let {
            val view = it.layoutInflater.inflate(R.layout.dialog_adloading, null, false)
            isShowingDialog = true
            AdLoadingDialog.setContentView(it, view = view, isCancelable = false)
                .showDialogInterstitial()
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