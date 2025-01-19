package com.dp.ads.lib.mintegralAdClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dp.ads.lib.R
import com.dp.ads.lib.data.InterstitialMaster.interstitialMintegralHashMap
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.mbridge.msdk.newinterstitial.out.MBNewInterstitialHandler
import com.mbridge.msdk.newinterstitial.out.NewInterstitialListener
import com.mbridge.msdk.out.MBridgeIds
import com.mbridge.msdk.out.RewardInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@SuppressLint("StaticFieldLeak")
object MintegralInterstitialInside : CoroutineScope by MainScope() {
    private const val adShowingDelayTime = 1500
    private var isShowDialog = true

    private var mContextMintegral: Context? = null
    private var onAdClosedCallbackMintegral: (() -> Unit)? = null
    private var onAdLoadedCallbackMintegral: (() -> Unit)? = null

    fun checkAndLoadMintegralInterstitial(
        context: Context?,
        nameFragment: String,
        placementId: String,
        unitId: String,
        onAdLoadedCallback: (() -> Unit)? = null
    ) {
        mContextMintegral = context
        onAdLoadedCallbackMintegral = onAdLoadedCallback

        if (NetworkCheck.isNetworkAvailable(mContextMintegral)) {
            if (!interstitialMintegralHashMap.containsKey(nameFragment)) {
                loadMintegralInterstitial(nameFragment, placementId, unitId)
            }
        } else {
            return
        }
    }

    private fun loadMintegralInterstitial(
        nameFragment: String,
        placementId: String,
        unitId: String
    ) {
        Log.i("DP_ADS_TAG", "Requesting Mintegral Interstitial: $nameFragment")

        if (!interstitialMintegralHashMap.containsKey(nameFragment)) {
            val handler = MBNewInterstitialHandler(mContextMintegral, placementId, unitId)
            interstitialMintegralHashMap[nameFragment] = handler

            handler.setInterstitialVideoListener(object : NewInterstitialListener {
                override fun onLoadCampaignSuccess(ids: MBridgeIds) {
                    Log.i("DP_ADS_TAG", "Mintegral Interstitial Campaign Loaded: $nameFragment")
                }

                override fun onResourceLoadSuccess(ids: MBridgeIds) {
                    Log.i("DP_ADS_TAG", "Mintegral Interstitial Loaded: $nameFragment")
                    onAdLoadedCallbackMintegral?.invoke()
                    onAdLoadedCallbackMintegral = null
                }

                override fun onResourceLoadFail(ids: MBridgeIds, errorMsg: String) {
                    Log.e("DP_ADS_TAG", "Mintegral Interstitial Failed to Load: $errorMsg")
                    interstitialMintegralHashMap.remove(nameFragment)
                }

                override fun onAdShow(ids: MBridgeIds) {
                    Log.i("DP_ADS_TAG", "Mintegral Interstitial Shown: $nameFragment")
                }

                override fun onAdClose(ids: MBridgeIds, info: RewardInfo) {
                    Log.i("DP_ADS_TAG", "Mintegral Interstitial Dismissed: $nameFragment")
                    onAdClosedCallbackMintegral?.invoke()
                    interstitialMintegralHashMap.remove(nameFragment)
                }

                override fun onShowFail(ids: MBridgeIds, errorMsg: String) {
                    Log.e("DP_ADS_TAG", "Mintegral Interstitial Failed to Show: $errorMsg")
                    onAdClosedCallbackMintegral?.invoke()
                    interstitialMintegralHashMap.remove(nameFragment)
                }

                override fun onAdClicked(ids: MBridgeIds) {
                    Log.i("DP_ADS_TAG", "Mintegral Interstitial Clicked: $nameFragment")
                }

                override fun onVideoComplete(ids: MBridgeIds) {
                    Log.i("DP_ADS_TAG", "Mintegral Interstitial Video Completed: $nameFragment")
                }

                override fun onAdCloseWithNIReward(ids: MBridgeIds, info: RewardInfo) {
                    Log.i("DP_ADS_TAG", "Mintegral Interstitial Closed with Reward: $nameFragment")
                }

                override fun onEndcardShow(p0: MBridgeIds?) {
                    Log.i(
                        "DP_ADS_TAG",
                        "Mintegral Interstitial Closed onEndCardShow: $nameFragment"
                    )
                }
            })
            handler.load()
        }
    }

    fun showIfAvailableOrLoadMintegralInterstitial(
        context: Context?,
        nameFragment: String,
        placementId: String,
        unitId: String,
        onAdClosedCallback: () -> Unit,
        onAdShowedCallback: () -> Unit
    ) {
        mContextMintegral = context
        isShowDialog = true
        this.onAdClosedCallbackMintegral = onAdClosedCallback

        if (interstitialMintegralHashMap.containsKey(nameFragment)) {
            showMintegralInterstitial(onAdShowedCallback, nameFragment)
        } else {
            Log.i("DP_ADS_TAG", "Ad not available. Requesting new ad: $nameFragment")
            checkAndLoadMintegralInterstitial(context, nameFragment, placementId, unitId)
            onAdClosedCallback.invoke()
        }
    }

    private fun showMintegralInterstitial(onAdShowedCallback: () -> Unit, nameFragment: String) {
        showWaitDialog()
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                dismissWaitDialog()
                val handler = interstitialMintegralHashMap[nameFragment]
                if (handler != null && handler.isReady) {
                    handler.show()
                    onAdShowedCallback.invoke()
                } else {
                    onAdClosedCallbackMintegral?.invoke()
                }
            }, adShowingDelayTime.toLong())
        } catch (e: Exception) {
            dismissWaitDialog()
            Log.e("DP_ADS_TAG", "Error showing Mintegral Interstitial: ${e.message}")
        }
    }

    private fun showWaitDialog() {
        if (isShowDialog) {
            mContextMintegral?.let {
                val view = (it as Activity).layoutInflater.inflate(
                    R.layout.dialog_adloading,
                    null,
                    false
                )
                AdLoadingDialog.setContentView(it, view = view, isCancelable = false)
                    .showDialogInterstitial()
            }
        }
    }

    private fun dismissWaitDialog() {
        mContextMintegral?.let {
            AdLoadingDialog.dismissDialog(it as Activity)
        }
    }
}
