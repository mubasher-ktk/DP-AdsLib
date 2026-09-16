package com.dp.ads.lib.adMobAdClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.dp.ads.lib.R
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@SuppressLint("StaticFieldLeak")
object AdMobRewardedInside : CoroutineScope by MainScope() {
    private var isRewardedAdVisible = false

    private var mContextAdmob: Context? = null
    private var onAdLoadingCallBackAdmob: (() -> Unit)? = null
    private var onRewardEarnedCallBackAdmob: (() -> Unit)? = null
    private var onNoNetworkCallBackAdmob: (() -> Unit)? = null
    private var onAdFailedToLoadOrShowCallBackAdmob: (() -> Unit)? = null
    private var onDismissedWithoutRewardCallBackAdmob: (() -> Unit)? = null

    /**
     * @param onNoNetwork no connectivity at request time - never a candidate for cross-network fallback.
     * @param onAdFailedToLoadOrShow the ad failed to load or failed to display - the fallback-eligible case.
     * @param onDismissedWithoutReward ad loaded and showed fine, but the user closed it before earning the reward.
     */
    fun requestAndShowRewardedAd(
        context: Context?,
        adName: String,
        adId: String,
        onAdLoading: (() -> Unit)? = null,
        onRewardEarned: () -> Unit,
        onNoNetwork: () -> Unit,
        onAdFailedToLoadOrShow: () -> Unit,
        onDismissedWithoutReward: () -> Unit
    ) {
        mContextAdmob = context
        onAdLoadingCallBackAdmob = onAdLoading
        onRewardEarnedCallBackAdmob = onRewardEarned
        onNoNetworkCallBackAdmob = onNoNetwork
        onAdFailedToLoadOrShowCallBackAdmob = onAdFailedToLoadOrShow
        onDismissedWithoutRewardCallBackAdmob = onDismissedWithoutReward

        if (!NetworkCheck.isNetworkAvailable(mContextAdmob)) {
            Log.e("DP_ADS_TAG", "AdMob Rewarded: no network. $adName")
            onNoNetworkCallBackAdmob?.invoke()
            return
        }

        onAdLoadingCallBackAdmob?.invoke()
        showWaitDialog()
        loadAdmobRewarded(adName, adId)
    }

    private fun loadAdmobRewarded(adName: String, adId: String) {
        Log.i("DP_ADS_TAG", "Requesting AdMob Rewarded: $adName")
        val adRequestRewarded = AdRequest.Builder().build()
        RewardedAd.load(
            mContextAdmob!!,
            adId,
            adRequestRewarded,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.i("DP_ADS_TAG", "AdMob Rewarded Loaded: $adName")
                    dismissWaitDialog()
                    showAdmobRewarded(rewardedAd, adName)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("DP_ADS_TAG", "AdMob Rewarded Failed to Load: $adName. Error: ${loadAdError.message}")
                    dismissWaitDialog()
                    onAdFailedToLoadOrShowCallBackAdmob?.invoke()
                    onAdFailedToLoadOrShowCallBackAdmob = null
                }
            }
        )
    }

    private fun showAdmobRewarded(rewardedAd: RewardedAd, adName: String) {
        val activity = mContextAdmob as? Activity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            onAdFailedToLoadOrShowCallBackAdmob?.invoke()
            return
        }

        var isRewardEarned = false

        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.i("DP_ADS_TAG", "AdMob Rewarded Dismissed: $adName. Reward earned: $isRewardEarned")
                isRewardedAdVisible = false
                if (!isRewardEarned) {
                    onDismissedWithoutRewardCallBackAdmob?.invoke()
                }
                onRewardEarnedCallBackAdmob = null
                onAdFailedToLoadOrShowCallBackAdmob = null
                onDismissedWithoutRewardCallBackAdmob = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e("DP_ADS_TAG", "Failed to Show AdMob Rewarded: $adName. Error: ${adError.message}")
                isRewardedAdVisible = false
                onAdFailedToLoadOrShowCallBackAdmob?.invoke()
                onRewardEarnedCallBackAdmob = null
                onAdFailedToLoadOrShowCallBackAdmob = null
                onDismissedWithoutRewardCallBackAdmob = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.i("DP_ADS_TAG", "AdMob Rewarded Shown: $adName")
                isRewardedAdVisible = true
            }
        }

        rewardedAd.show(activity) {
            Log.i("DP_ADS_TAG", "AdMob Rewarded Earned: $adName")
            isRewardEarned = true
            onRewardEarnedCallBackAdmob?.invoke()
        }
    }

    private fun showWaitDialog() {
        mContextAdmob?.let {
            if (!(it as Activity).isFinishing && !it.isDestroyed) {
                val view = it.layoutInflater.inflate(R.layout.dialog_adloading, null, false)
                AdLoadingDialog.setContentView(it, view = view, isCancelable = false)
                    .showDialogInterstitial()
            }
        }
    }

    private fun dismissWaitDialog() {
        mContextAdmob?.let {
            if (!(it as Activity).isFinishing && !it.isDestroyed) {
                AdLoadingDialog.dismissDialog(it)
            }
        }
    }
}
