package com.dp.ads.lib.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dp.ads.lib.adMobAdClasses.AdMobInterstitialInside
import com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager
import com.dp.ads.lib.callingClasses.DPAdsConfigurations
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.databinding.FragmentWTThreeBinding
import com.dp.ads.lib.data.WalkThroughItem
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.mintegralAdClasses.MintegralBannerAdManager
import com.dp.ads.lib.mintegralAdClasses.MintegralInterstitialInside
import com.dp.ads.lib.utils.NetworkCheck
import com.dp.ads.lib.utils.PrefHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WTThreeFragment(private val fragmentActivity: FragmentActivity, val item: WalkThroughItem) : Fragment() {

    lateinit var binding: FragmentWTThreeBinding
    private var dpAdsConfigurations: DPAdsConfigurations? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWTThreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dpAdsConfigurations = DPAdsManager.getConfigurations()

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Glide.with(requireActivity())
                    .asDrawable()
                    .load(item.drawable)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(true)
                    .into(binding.main)
            }
        }
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Glide.with(requireActivity())
                    .asDrawable()
                    .load(item.drawableBubble)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(true)
                    .into(binding.bubble)
            }
        }

        binding.txtHeading.setTextColor(ContextCompat.getColor(requireActivity(), item.headingColor))
        binding.txtDescription.setTextColor(ContextCompat.getColor(requireActivity(), item.descriptionColor))
        binding.btnNext.setTextColor(ContextCompat.getColor(requireActivity(), item.nextColor))

        binding.txtHeading.text = item.heading
        binding.txtDescription.text = item.description

        val interstitialLetsStartEnabled = dpAdsConfigurations?.getRemoteConfigData()?.get("INTERSTITIAL_LETS_START") as? Boolean ?: false

        binding.btnNext.setOnClickListener {
            if (interstitialLetsStartEnabled) {
                when (dpAdsConfigurations?.getRemoteConfigData()?.get("INTERSTITIAL_LETS_START_MED")) {
                    "ADMOB" -> {
                        showAdmobWTThreeInterstitial()
                    }
                    "MINTEGRAL" -> {
                        showMintegralWTThreeInterstitial()
                    }
                }
            } else {
                onNextClick()
            }
        }
    }

    private fun showMintegralWTThreeInterstitial() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_INTERSTITIAL_LETS_START")?.split("-")?.size == 2) {
            MintegralInterstitialInside.showIfAvailableOrLoadMintegralInterstitial(
                context = requireActivity(),
                nameFragment = "WALKTHROUGH_3",
                placementId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_INTERSTITIAL_LETS_START").split("-")[0],
                unitId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_INTERSTITIAL_LETS_START").split("-")[1],
                onAdClosedCallback = {
                    Log.i("DP_ADS_TAG","Interstitial : WALKTHROUGH_3 : onAdClosedCallBackAdmob()")
                    Handler(Looper.getMainLooper()).postDelayed({
                        onNextClick()
                    },300)
                },
                onAdShowedCallback = {
                    Log.i("DP_ADS_TAG", "Interstitial : WALKTHROUGH_3 : onAdShowedCallBackAdmob()")
                }
            )
        } else {
            Log.e("DP_ADS_TAG","Mintegral: Interstitial ad ID not found for WALKTHROUGH_3")
        }
    }

    private fun onNextClick() {
        PrefHelper(requireContext()).putBoolean(dpAdsConfigurations!!.shouldShowStartScreens, value = true)
        DPAdsManager.notifyFlowFinished()
        fragmentActivity.finish()
    }

    private fun showAdmobWTThreeInterstitial() {
        AdMobInterstitialInside.showIfAvailableOrLoadAdMobInterstitial(
            context = requireActivity(),
            nameFragment = "WALKTHROUGH_3",
            adId = dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_INTERSTITIAL_LETS_START")!!,
            onAdClosedCallBackAdmob = {
                Log.i("DP_ADS_TAG","Interstitial : WALKTHROUGH_3 : onAdClosedCallBackAdmob()")
                Handler(Looper.getMainLooper()).postDelayed({
                    onNextClick()
                },300)
            },
            onAdShowedCallBackAdmob = {
                Log.i("DP_ADS_TAG", "Interstitial : WALKTHROUGH_3 : onAdShowedCallBackAdmob()")
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (!NetworkCheck.isNetworkAvailable(context)) {
            binding.glOne.setGuidelinePercent(0.8f)
            binding.nativeAdContainerAd.visibility = View.GONE
        }

        val nativeWalkThrough3Enabled = dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_3") as? Boolean ?: false
        if (nativeWalkThrough3Enabled) {
            when (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_3_MED")) {
                "ADMOB" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showAdmobWTThreeNatives()
                }
                "META" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showMetaWTThreeNatives()
                }
                "MINTEGRAL" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showMintegralWTThreeBanner()
                }
            }
        } else {
            binding.nativeAdContainerAd.visibility = View.GONE
        }
    }

    private fun showMetaWTThreeNatives() {
        dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("NATIVE_WALKTHROUGH_3")?.let { adId ->
            MetaNativeAdManager.requestAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_3",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_3").toString().toBoolean(),
                populateView = true,
                nativeAdLayout = binding.nativeAdContainerAd,
                onAdFailed = {
                    binding.nativeAdContainerAd.visibility = View.GONE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_3: Meta: onAdFailed()")
                },
                onAdLoaded = {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_3: Meta: onAdLoaded()")
                }
            )
        }
    }
    private fun showAdmobWTThreeNatives() {
        dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_WALKTHROUGH_3")?.let { adId ->
            AdmobNativeAdManager.requestOrShowAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_3",
                isMediaWithCtaOnTop = true,
                remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_3").toString().toBoolean(),
                populateView = true,
                adContainer = binding.nativeAdContainerAd,
                onAdFailed = {
                    binding.nativeAdContainerAd.visibility = View.GONE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_3: Admob: onAdFailed()")
                },
                onAdLoaded = {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_3: Admob: onAdLoaded()")
                }
            )
        }
    }
    private fun showMintegralWTThreeBanner() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_WALKTHROUGH_3")?.split("-")?.size == 2) {
            MintegralBannerAdManager.requestBannerAd(
                activity = requireActivity(),
                placementId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_BANNER_WALKTHROUGH_3").split("-")[0],
                unitId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_BANNER_WALKTHROUGH_3").split("-")[1],
                adName = "WALKTHROUGH_3",
                remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_3").toString().toBoolean(),
                populateView = true,
                bannerContainer = binding.bannerAdMint,
                shimmerContainer = binding.shimmerLayout,
                onAdFailed = {
//                    findViewById<CardView>(R.id.nativeAdContainerAd).visibility = View.GONE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_3: MINTEGRAL: onAdFailed()")
                },
                onAdLoaded = {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.INVISIBLE
                    binding.bannerAdMint.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_3: MINTEGRAL: onAdLoaded()")
                }
            )
        } else {
            Log.i("DP_ADS_TAG", "BANNER : Mintegral : MAY WALKTHROUGH_3 Incorrect ID Format (placementID-unitID)")
        }
    }
}