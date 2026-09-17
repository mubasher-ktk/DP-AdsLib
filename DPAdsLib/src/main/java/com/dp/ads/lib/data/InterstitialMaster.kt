package com.dp.ads.lib.data

import com.google.android.gms.ads.interstitial.InterstitialAd

object InterstitialMaster {
    var interstitialAdMobHashMap: HashMap<String, InterstitialAd> = HashMap()
    val interstitialMetaHashMap = HashMap<String, com.facebook.ads.InterstitialAd>()
}