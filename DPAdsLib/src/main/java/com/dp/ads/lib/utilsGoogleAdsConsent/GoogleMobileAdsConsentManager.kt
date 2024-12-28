package com.dp.ads.lib.utilsGoogleAdsConsent

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

class GoogleMobileAdsConsentManager private constructor(context: Context) {

    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun isUserInConsentRequiredRegion(): Boolean {
        return consentInformation.isConsentFormAvailable
    }

    private fun setDeviceHashedIDs(activity: Activity, testDeviceHashedIdList: ArrayList<String>): ConsentDebugSettings {
        val debugSettingsBuilder = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)

        for (hashedId in testDeviceHashedIdList) {
            debugSettingsBuilder.addTestDeviceHashedId(hashedId)
        }

        return debugSettingsBuilder.build()
    }

    fun gatherConsent(activity: Activity, testDeviceHashedIdList: ArrayList<String>, removeSlowInternetCallBack: (() -> Unit)? = null, errorMakingRequest: (() -> Unit)? = null, onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener) {
        val params = ConsentRequestParameters.Builder().setConsentDebugSettings(setDeviceHashedIDs(activity, testDeviceHashedIdList)).build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params, {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    Log.i("ConsentMessage", "GoogleMobileAdsConsentManager: loadAndShowConsentFormIfRequired: Gathered")
                    onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                }
                Log.i("ConsentMessage", "GoogleMobileAdsConsentManager: loadAndShowConsentFormIfRequired")
                removeSlowInternetCallBack!!.invoke()
            }, { requestConsentError ->
                Log.i("ConsentMessage", "GoogleMobileAdsConsentManager: requestConsentError: "+requestConsentError.message)
                if (requestConsentError.message.equals("Error making request.") || requestConsentError.message.equals("The server timed out.")) {
                    errorMakingRequest!!.invoke()
                } else {
                    onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
                }
            }
        )
    }

    fun showPrivacyOptionsForm(activity: Activity, onConsentFormDismissedListener: OnConsentFormDismissedListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }

    companion object {

        @Volatile private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: GoogleMobileAdsConsentManager(context).also { instance = it }
        }
    }
}