package com.dp.ads.lib.adMobAdClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dp.ads.lib.R
import com.dp.ads.lib.data.InterstitialMaster.interstitialAdMobHashMap
import com.dp.ads.lib.utils.AdLoadingDialog
import com.dp.ads.lib.utils.NetworkCheck
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@SuppressLint("StaticFieldLeak")
object AdMobInterstitialInside : CoroutineScope by MainScope() {
    private const val adShowingDelayTime = 1500
    private var isShowDialog = true

    private var mContextAdmob: Context? = null
    private var onAdClosedCallBackAdmob: (() -> Unit)? = null
    private var onAdLoadedCallBackAdmob: (() -> Unit)? = null

    fun checkAndLoadAdMobInterstitial(
        context: Context?,
        nameFragment: String,
        adId: String,
        onAdLoadedCallAdmob: (() -> Unit)? = null
    ) {
        mContextAdmob = context
        onAdLoadedCallBackAdmob = onAdLoadedCallAdmob

        if (NetworkCheck.isNetworkAvailable(mContextAdmob)) {
            if (!interstitialAdMobHashMap.containsKey(nameFragment)) {
                loadAdmobInterstitial(nameFragment, adId)
            }
        } else {
            return
        }
    }

    private fun loadAdmobInterstitial(nameFragment: String, adId: String) {
        Log.i("DP_ADS_TAG", "Requesting AdMob Interstitial: $nameFragment")
        if (!interstitialAdMobHashMap.containsKey(nameFragment)) {
            val adRequestInterstitial = AdRequest.Builder().build()
            InterstitialAd.load(
                mContextAdmob!!,
                adId,
                adRequestInterstitial,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.i("DP_ADS_TAG", "AdMob Interstitial Loaded: $nameFragment")
                        interstitialAdMobHashMap[nameFragment] = interstitialAd
                        onAdLoadedCallBackAdmob?.invoke()
                        onAdLoadedCallBackAdmob = null
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(
                            "DP_ADS_TAG",
                            "AdMob Interstitial Failed to Load: $nameFragment. Error: ${loadAdError.message}"
                        )
                    }
                }
            )
        }
    }

    fun showIfAvailableOrLoadAdMobInterstitial(
        context: Context?,
        nameFragment: String,
        adId: String,
        onAdClosedCallBackAdmob: () -> Unit,
        onAdShowedCallBackAdmob: () -> Unit
    ) {
        mContextAdmob = context
        isShowDialog = true
        this.onAdClosedCallBackAdmob = onAdClosedCallBackAdmob

        if (interstitialAdMobHashMap.containsKey(nameFragment)) {
            showAdmobInterstitial(onAdShowedCallBackAdmob, nameFragment)
        } else {
            Log.i("DP_ADS_TAG", "Ad not available. Requesting new ad: $nameFragment")
            checkAndLoadAdMobInterstitial(context, nameFragment, adId)
            onAdClosedCallBackAdmob.invoke()
        }
    }

    private fun showAdmobInterstitial(onAdShowedCallBackAdmob: () -> Unit, nameFragment: String) {
        showWaitDialog()
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                dismissWaitDialog()
                val interstitialAd = interstitialAdMobHashMap[nameFragment]
                if (interstitialAd != null) {
                    interstitialAd.show(mContextAdmob as Activity)
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.i("DP_ADS_TAG", "AdMob Interstitial Dismissed: $nameFragment")
                                onAdClosedCallBackAdmob?.invoke()
                                interstitialAdMobHashMap.remove(nameFragment)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                Log.e(
                                    "DP_ADS_TAG",
                                    "Failed to Show AdMob Interstitial: $nameFragment. Error: ${adError.message}"
                                )
                                onAdClosedCallBackAdmob?.invoke()
                                interstitialAdMobHashMap.remove(nameFragment)
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.i("DP_ADS_TAG", "AdMob Interstitial Shown: $nameFragment")
                                onAdShowedCallBackAdmob.invoke()
                            }
                        }
                } else {
                    onAdClosedCallBackAdmob?.invoke()
                }
            }, adShowingDelayTime.toLong())
        } catch (e: Exception) {
            dismissWaitDialog()
            Log.e("DP_ADS_TAG", "Error showing AdMob Interstitial: ${e.message}")
        }
    }

    private fun showWaitDialog() {
        if (isShowDialog) {
            mContextAdmob?.let {
                if (!(it as Activity).isFinishing && !it.isDestroyed) {
                    val view = it.layoutInflater.inflate(R.layout.dialog_adloading,null,false)
                    AdLoadingDialog.setContentView(it, view = view, isCancelable = false)
                        .showDialogInterstitial()
                }
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