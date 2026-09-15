package com.dp.ads.lib.metaAdClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@SuppressLint("StaticFieldLeak")
object MetaRewardedInside : CoroutineScope by MainScope() {
    private var isRewardedAdVisible = false

    private var mContextMeta: Context? = null
    private var onAdLoadingCallBackMeta: (() -> Unit)? = null
    private var onRewardEarnedCallBackMeta: (() -> Unit)? = null
    private var onAdFailedOrNoRewardCallBackMeta: (() -> Unit)? = null

    fun requestAndShowRewardedAd(
        context: Context?,
        adName: String,
        adId: String,
        onAdLoading: (() -> Unit)? = null,
        onRewardEarned: () -> Unit,
        onAdFailedOrNoReward: () -> Unit
    ) {
        mContextMeta = context
        onAdLoadingCallBackMeta = onAdLoading
        onRewardEarnedCallBackMeta = onRewardEarned
        onAdFailedOrNoRewardCallBackMeta = onAdFailedOrNoReward

        if (!NetworkCheck.isNetworkAvailable(mContextMeta)) {
            Log.e("DP_ADS_TAG", "Meta Rewarded: no network. $adName")
            onAdFailedOrNoRewardCallBackMeta?.invoke()
            return
        }

        onAdLoadingCallBackMeta?.invoke()
        showWaitDialog()
        loadMetaRewarded(adName, adId)
    }

    private fun loadMetaRewarded(adName: String, adId: String) {
        Log.i("DP_ADS_TAG", "Requesting Meta Rewarded: $adName")
        val rewardedVideoAd = RewardedVideoAd(mContextMeta, adId)
        var isRewardEarned = false

        rewardedVideoAd.loadAd(
            rewardedVideoAd.buildLoadAdConfig()
                .withAdListener(object : RewardedVideoAdListener {
                    override fun onAdLoaded(ad: Ad) {
                        Log.i("DP_ADS_TAG", "Meta Rewarded Loaded: $adName")
                        dismissWaitDialog()

                        val activity = mContextMeta as? Activity
                        if (activity == null || activity.isFinishing || activity.isDestroyed) {
                            onAdFailedOrNoRewardCallBackMeta?.invoke()
                            return
                        }
                        rewardedVideoAd.show()
                    }

                    override fun onError(ad: Ad, adError: AdError) {
                        Log.e("DP_ADS_TAG", "Meta Rewarded Failed to Load: $adName. Error: ${adError.errorMessage}")
                        dismissWaitDialog()
                        isRewardedAdVisible = false
                        onAdFailedOrNoRewardCallBackMeta?.invoke()
                        onRewardEarnedCallBackMeta = null
                        onAdFailedOrNoRewardCallBackMeta = null
                    }

                    override fun onRewardedVideoCompleted() {
                        Log.i("DP_ADS_TAG", "Meta Rewarded Earned: $adName")
                        isRewardedAdVisible = true
                        isRewardEarned = true
                        onRewardEarnedCallBackMeta?.invoke()
                    }

                    override fun onRewardedVideoClosed() {
                        Log.i("DP_ADS_TAG", "Meta Rewarded Closed: $adName. Reward earned: $isRewardEarned")
                        isRewardedAdVisible = false
                        if (!isRewardEarned) {
                            onAdFailedOrNoRewardCallBackMeta?.invoke()
                        }
                        onRewardEarnedCallBackMeta = null
                        onAdFailedOrNoRewardCallBackMeta = null
                    }

                    override fun onAdClicked(ad: Ad) {}
                    override fun onLoggingImpression(ad: Ad) {}
                })
                .build()
        )
    }

    private fun showWaitDialog() {
        mContextMeta?.let {
            if (!(it as Activity).isFinishing && !it.isDestroyed) {
                val view = it.layoutInflater.inflate(R.layout.dialog_adloading, null, false)
                AdLoadingDialog.setContentView(it, view = view, isCancelable = false)
                    .showDialogInterstitial()
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
