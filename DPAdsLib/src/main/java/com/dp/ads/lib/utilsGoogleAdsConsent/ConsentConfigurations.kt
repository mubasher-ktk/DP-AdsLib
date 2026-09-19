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
    private val metaInitStarted = AtomicBoolean(false)
    private val sdkInitProceeded = AtomicBoolean(false)
    private val sdkInitResolved = AtomicBoolean(false)
    private val pendingSdkInitCallbacks = java.util.concurrent.CopyOnWriteArrayList<() -> Unit>()
    private val slowInternetHandler = Handler()
    private val sdkInitFallbackHandler = Handler()
    // Set by initializeMobileAdsSdk() while it is waiting on both SDKs, so Meta's own
    // listener can resolve things if Meta finishes after AdMob rather than before it.
    private var activeProceedOnce: (() -> Unit)? = null

    init {
        // Meta gets no benefit from waiting on consent gathering or AdMob - it only
        // needs to be told the result once ads are actually requested. Firing it here,
        // at construction, gives it the whole consent-gathering wait (which can run
        // several seconds) as head start instead of starting only after consent
        // resolves. Fully async and fire-and-forget: nothing else in this class or in
        // consentInitializationSetup() waits on it or is gated by it.
        ensureMetaInitStarted()
        consentInitializationSetup()
    }

    private fun ensureMetaInitStarted() {
        if (!metaInitStarted.compareAndSet(false, true)) return
        AdSettings.addTestDevice("0984fdbc-e473-40e8-91f5-b6b46ebc85b5")
        AdSettings.addTestDevice("240faf54-381a-4269-bbc6-713aed8a4b4b")
        AdSettings.addTestDevice("0f01a5f6-802a-4743-ae14-8e6a7a360965")
        AdSettings.addTestDevice("bba88f94-ecc3-4c56-bac8-8683f76946f9")
        AdSettings.addTestDevice("67e557c7-c6ee-4209-9e84-7e5b60546400")
        AdSettings.addTestDevice("937cc986-d628-450b-ae61-f6ad32e3b6a2")
        AudienceNetworkAds.buildInitSettings(activityContext)
            .withInitListener { result ->
                Log.i("ConsentMessage", "ensureMetaInitStarted(): Meta initialized, success=${result.isSuccess}")
                metaSdkReady.set(true)
                if (admobSdkReady.get()) activeProceedOnce?.invoke()
            }
            .initialize()
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
            if (sdkInitResolved.get()) {
                Log.i("ConsentMessage","initializeMobileAdsSdk()")
                initializeMobileAds.invoke()
            } else {
                Log.i("ConsentMessage","initializeMobileAdsSdk(): SDK init still in progress, queuing callback")
                pendingSdkInitCallbacks.add(initializeMobileAds)
            }
            return
        }
        Log.i("ConsentMessage","initializeMobileAdsSdk(): rem")

        fun proceedOnce() {
            if (!sdkInitProceeded.getAndSet(true)) {
                sdkInitFallbackHandler.removeCallbacksAndMessages(null)
                initializeMobileAds.invoke()
                sdkInitResolved.set(true)
                pendingSdkInitCallbacks.forEach { it.invoke() }
                pendingSdkInitCallbacks.clear()
            }
        }
        activeProceedOnce = { proceedOnce() }

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
            // Meta init already started at construction (ensureMetaInitStarted()) so it
            // gets the head start; this is only a safety net in case that early call
            // somehow never fired, and metaInitStarted makes it a guaranteed no-op
            // otherwise - it will never call AudienceNetworkAds.initialize() twice.
            ensureMetaInitStarted()
            if (metaSdkReady.get() && admobSdkReady.get()) proceedOnce()
        } else {
            initializeMobileAds.invoke()
            sdkInitResolved.set(true)
            pendingSdkInitCallbacks.forEach { it.invoke() }
            pendingSdkInitCallbacks.clear()
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
