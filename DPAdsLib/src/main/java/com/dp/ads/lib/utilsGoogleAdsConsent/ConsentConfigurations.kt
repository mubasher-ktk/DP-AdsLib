package com.dp.ads.lib.utilsGoogleAdsConsent

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.dp.ads.lib.utils.NetworkCheck
import com.facebook.ads.AdSettings
import com.google.android.gms.ads.MobileAds
import com.mbridge.msdk.MBridgeSDK
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.mbridge.msdk.out.SDKInitStatusListener
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
import java.util.concurrent.atomic.AtomicBoolean

class ConsentConfigurations private constructor(
    private val activityContext: Activity,
    private val applicationContext: Application,
    private val appId: String,
    private val appKey: String,
    private val gameId: String,
    private val testMode: Boolean,
    private val testDeviceHashedIdList: ArrayList<String>,
    private val onConsentGathered: () -> Unit) {

    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val slowInternetHandler = Handler()

    init {
        consentInitializationSetup()
    }

    private fun consentInitializationSetup() {
        Log.i("ConsentMessage", "ConsentConfigurations: consentInitializationSetup called")
        slowInternetHandler.postDelayed(kotlinx.coroutines.Runnable { onConsentGathered.invoke() },15000)

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
                        onConsentGathered.invoke()
                    })
            },
            onConsentGatheringCompleteListener = { error ->
                if (googleMobileAdsConsentManager.canRequestAds) {
                    initializeMobileAdsSdk(initializeMobileAds = {
                        onConsentGathered.invoke()
                    })
                } else {
                    if (error != null) {
                        Log.i("ConsentMessage","ConsentConfigurations: error:: "+error.message)
                            initializeMobileAdsSdk(initializeMobileAds = {
                                onConsentGathered.invoke()
                            })
                    }
                }
            })

        if (appId != "" && appKey != "") {
            val sdk: MBridgeSDK = MBridgeSDKFactory.getMBridgeSDK()
            val map = sdk.getMBConfigurationMap(appId, appKey)
            sdk.init(map, applicationContext, object : SDKInitStatusListener {
                override fun onInitSuccess() {
                    Log.i("DP_ADS_TAG","Mintegral :: Init Success")
                }

                override fun onInitFail(errorMsg: String) {
                    Log.i("DP_ADS_TAG", "Mintegral :: Init Fail :: $errorMsg")
                }
            })
        }

        if (gameId != "") {
            UnityAds.initialize(applicationContext, gameId, testMode, object :
                IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.e("DP_ADS_TAG", "UnityAds: onInitializationComplete()")
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                    Log.e("DP_ADS_TAG", "UnityAds: onInitializationFailed() \n\n$error\n\n$message")
                }
            })
        }
    }

    private fun initializeMobileAdsSdk(initializeMobileAds: () -> Unit) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            Log.i("ConsentMessage","initializeMobileAdsSdk()")
            initializeMobileAds.invoke()
            return
        }
        Log.i("ConsentMessage","initializeMobileAdsSdk(): rem")
        if (NetworkCheck.isNetworkAvailable(activityContext)) {
            activityContext.getSharedPreferences("ConsentMessage", MODE_PRIVATE).edit().putBoolean("FirstTime", true).apply()
            MobileAds.initialize(activityContext)
            AdSettings.addTestDevice("0984fdbc-e473-40e8-91f5-b6b46ebc85b5")
            AdSettings.addTestDevice("240faf54-381a-4269-bbc6-713aed8a4b4b")
            AdSettings.addTestDevice("0f01a5f6-802a-4743-ae14-8e6a7a360965")
            AdSettings.addTestDevice("bba88f94-ecc3-4c56-bac8-8683f76946f9")
            AdSettings.addTestDevice("67e557c7-c6ee-4209-9e84-7e5b60546400") // G-Pixel
            AdSettings.addTestDevice("937cc986-d628-450b-ae61-f6ad32e3b6a2") // LG - Red Device
            // AudienceNetworkAds.initialize(activityContext)

        }
        initializeMobileAds.invoke()
        slowInternetHandler.removeCallbacksAndMessages(null)
    }

    class Builder {
        private lateinit var activityContext: Activity
        private lateinit var applicationContext: Application
        private var appKey: String = ""
        private var appId: String = ""
        private var gameId: String = ""
        private var testMode: Boolean = true
        private var testDeviceHashedIdList: ArrayList<String> = ArrayList()
        private lateinit var onConsentGathered: () -> Unit

        fun setApplicationContext(applicationContext: Application) = apply {
            this.applicationContext = applicationContext
        }

        fun setMintegralInitializationId(appKey: String, appId: String) = apply {
            this.appKey = appKey
            this.appId = appId
        }

        fun setUnityInitializationId(gameId: String, testMode: Boolean) = apply {
            this.gameId = gameId
            this.testMode = testMode
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
            return ConsentConfigurations(activityContext, applicationContext, appId, appKey, gameId, testMode, testDeviceHashedIdList, onConsentGathered)
        }
    }
}
