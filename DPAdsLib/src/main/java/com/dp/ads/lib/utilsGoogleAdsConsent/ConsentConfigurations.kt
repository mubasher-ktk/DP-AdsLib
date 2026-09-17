package com.dp.ads.lib.utilsGoogleAdsConsent

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.dp.ads.lib.utils.NetworkCheck
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.concurrent.atomic.AtomicBoolean

class ConsentConfigurations private constructor(
    private val activityContext: Activity,
    private val applicationContext: Application,
    private val testDeviceHashedIdList: ArrayList<String>,
    private val onConsentGathered: () -> Unit) {

    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val onConsentGatheredInvoked = AtomicBoolean(false)
    private val admobSdkReady = AtomicBoolean(false)
    private val metaSdkReady = AtomicBoolean(false)
    private val sdkInitProceeded = AtomicBoolean(false)
    private val slowInternetHandler = Handler()
    private val sdkInitFallbackHandler = Handler()

    init {
        consentInitializationSetup()
    }

    private fun consentInitializationSetup() {
        Log.i("ConsentMessage", "ConsentConfigurations: consentInitializationSetup called")
        slowInternetHandler.postDelayed(kotlinx.coroutines.Runnable {
            Log.i("ConsentMessage", "ConsentConfigurations: consent fallback fired (15s)")
            initializeMobileAdsSdk(initializeMobileAds = { onConsentGatheredOnce() })
        },15000)
        RequestConfiguration.Builder().setTestDeviceIds(listOf("63AD3B7607632FF4E6693BF74FE883FC"))
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(activityContext)
        googleMobileAdsConsentManager.gatherConsent(
            activity = activityContext,
            testDeviceHashedIdList = testDeviceHashedIdList,
            removeSlowInternetCallBack = {
            Log.i("ConsentMessage", "ConsentConfigurations: removeSlowInternetCallBack")
            slowInternetHandler.removeCallbacksAndMessages(null)
            },
            errorMakingRequest = {
                Log.i("ConsentMessage","ConsentConfigurations: ")
                    initializeMobileAdsSdk(initializeMobileAds = {
                        onConsentGatheredOnce()
                    })
            },
            onConsentGatheringCompleteListener = { error ->
                if (googleMobileAdsConsentManager.canRequestAds) {
                    initializeMobileAdsSdk(initializeMobileAds = {
                        onConsentGatheredOnce()
                    })
                } else {
                    if (error != null) {
                        Log.i("ConsentMessage","ConsentConfigurations: error:: "+error.message)
                            initializeMobileAdsSdk(initializeMobileAds = {
                                onConsentGatheredOnce()
                            })
                    }
                }
            })
    }

    private fun onConsentGatheredOnce() {
        if (!onConsentGatheredInvoked.getAndSet(true)) {
            onConsentGathered.invoke()
        }
    }

    private fun initializeMobileAdsSdk(initializeMobileAds: () -> Unit) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            Log.i("ConsentMessage","initializeMobileAdsSdk()")
            initializeMobileAds.invoke()
            return
        }
        Log.i("ConsentMessage","initializeMobileAdsSdk(): rem")

        fun proceedOnce() {
            if (!sdkInitProceeded.getAndSet(true)) {
                sdkInitFallbackHandler.removeCallbacksAndMessages(null)
                initializeMobileAds.invoke()
            }
        }

        if (NetworkCheck.isNetworkAvailable(activityContext)) {
            activityContext.getSharedPreferences("ConsentMessage", MODE_PRIVATE).edit().putBoolean("FirstTime", true).apply()

            // Both SDKs' initialize() calls are async; without waiting for their
            // completion listeners, ad requests fired right after this point race
            // a still-initializing SDK, which is what caused the slow first-load
            // batch. This fallback bounds that wait so a stuck listener can't
            // block ads forever.
            sdkInitFallbackHandler.postDelayed(kotlinx.coroutines.Runnable {
                Log.i("ConsentMessage", "initializeMobileAdsSdk(): SDK init fallback fired (6s), proceeding without waiting further")
                proceedOnce()
            }, 6000)

            MobileAds.initialize(activityContext) { status ->
                Log.i("ConsentMessage", "initializeMobileAdsSdk(): AdMob initialized: $status")
                admobSdkReady.set(true)
                if (metaSdkReady.get()) proceedOnce()
            }
            AdSettings.addTestDevice("0984fdbc-e473-40e8-91f5-b6b46ebc85b5")
            AdSettings.addTestDevice("240faf54-381a-4269-bbc6-713aed8a4b4b")
            AdSettings.addTestDevice("0f01a5f6-802a-4743-ae14-8e6a7a360965")
            AdSettings.addTestDevice("bba88f94-ecc3-4c56-bac8-8683f76946f9")
            AdSettings.addTestDevice("67e557c7-c6ee-4209-9e84-7e5b60546400")
            AdSettings.addTestDevice("937cc986-d628-450b-ae61-f6ad32e3b6a2")
            AudienceNetworkAds.buildInitSettings(activityContext)
                .withInitListener { result ->
                    Log.i("ConsentMessage", "initializeMobileAdsSdk(): Meta initialized, success=${result.isSuccess}")
                    metaSdkReady.set(true)
                    if (admobSdkReady.get()) proceedOnce()
                }
                .initialize()
        } else {
            initializeMobileAds.invoke()
        }
        slowInternetHandler.removeCallbacksAndMessages(null)
    }

    class Builder {
        private lateinit var activityContext: Activity
        private lateinit var applicationContext: Application
        private var testDeviceHashedIdList: ArrayList<String> = ArrayList()
        private lateinit var onConsentGathered: () -> Unit

        fun setApplicationContext(applicationContext: Application) = apply {
            this.applicationContext = applicationContext
        }

        fun setActivityContext(activity: Activity) = apply {
            this.activityContext = activity
        }

        fun setTestDeviceHashedIdList(ids: ArrayList<String>) = apply {
            this.testDeviceHashedIdList = ids
        }

        fun setOnConsentGatheredCallback(callback: () -> Unit) = apply {
            this.onConsentGathered = callback
        }

        fun build(): ConsentConfigurations {
            if (!::activityContext.isInitialized) {
                throw IllegalStateException("Activity context must be provided")
            }
            if (!::onConsentGathered.isInitialized) {
                throw IllegalStateException("OnConsentGathered callback must be provided")
            }
            return ConsentConfigurations(activityContext, applicationContext, testDeviceHashedIdList, onConsentGathered)
        }
    }
}
