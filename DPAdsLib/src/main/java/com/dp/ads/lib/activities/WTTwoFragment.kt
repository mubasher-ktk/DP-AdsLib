package com.dp.ads.lib.activities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dp.ads.lib.R
import com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager
import com.dp.ads.lib.callingClasses.DPAdsConfigurations
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.databinding.FragmentWTTwoBinding
import com.dp.ads.lib.data.WalkThroughItem
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.mintegralAdClasses.MintegralBannerAdManager
import com.dp.ads.lib.utils.NetworkCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WTTwoFragment(val item: WalkThroughItem) : Fragment() {

    lateinit var binding: FragmentWTTwoBinding
    private var dpAdsConfigurations: DPAdsConfigurations? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWTTwoBinding.inflate(inflater, container, false)
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

        if (!NetworkCheck.isNetworkAvailable(context)) {
            binding.glOne.setGuidelinePercent(0.8f)
        }
        binding.btnNext.setOnClickListener {
            val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
            viewPager?.currentItem = 2
        }

        val nativeWalkThrough3Enabled = dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_3") as? Boolean ?: false
        if (nativeWalkThrough3Enabled) {
            when (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_3_MED")) {
                "ADMOB" -> loadAdmobWTThreeNatives()
                "META" -> loadMetaWTThreeNatives()
                "MINTEGRAL" -> loadMintegralWTThreeBanner()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!NetworkCheck.isNetworkAvailable(context)) {
            binding.glOne.setGuidelinePercent(0.8f)
            binding.nativeAdContainerAd.visibility = View.GONE
        }

        val nativeWalkThrough1Enabled = dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_2") as? Boolean ?: false
        if (nativeWalkThrough1Enabled) {
            when (dpAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_2_MED")) {
                "ADMOB" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showAdmobWTTwoNatives()
                }
                "META" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showMetaWTTwoNatives()
                }
                "MINTEGRAL" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showMintegralWTTwoBanner()
                }
            }
        } else {
            binding.nativeAdContainerAd.visibility = View.GONE
        }
    }

    private fun loadAdmobWTThreeNatives() {
        dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_WALKTHROUGH_3")?.let { adId ->
            AdmobNativeAdManager.requestOrShowAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_3",
                populateView = false
            )
        } ?: Log.i("WTTwoFragment","ADMOB_NATIVE_WALKTHROUGH_3 ad ID is missing.")
    }
    private fun loadMetaWTThreeNatives() {
        dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_WALKTHROUGH_3")?.let { adId ->
            MetaNativeAdManager.requestOrShowAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_3",
                populateView = false
            )
        } ?: Log.i("WTTwoFragment","META_NATIVE_WALKTHROUGH_3 ad ID is missing.")
    }
    private fun loadMintegralWTThreeBanner() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_WALKTHROUGH_3")?.split("-")?.size == 2) {
            MintegralBannerAdManager.requestBannerAd(
                activity = requireActivity(),
                placementId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_BANNER_WALKTHROUGH_3").split("-")[0],
                unitId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_BANNER_WALKTHROUGH_3").split("-")[1],
                adName = "WALKTHROUGH_3",
                populateView = false)
        } else {
            Log.e("DP_ADS_TAG","BANNER : Mintegral : MAY WALKTHROUGH_2 Incorrect ID Format (placementID-unitID)")
        }
    }

    private fun showAdmobWTTwoNatives() {
        dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_WALKTHROUGH_2")?.let { adId ->
            AdmobNativeAdManager.requestOrShowAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_2",
                isMediaWithCtaOnTop = true,
                remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_2").toString().toBoolean(),
                populateView = true,
                adContainer = binding.nativeAdContainerAd,
                onAdFailed = {
                    binding.nativeAdContainerAd.visibility = View.GONE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_2: Admob: onAdFailed()")
                },
                onAdLoaded = {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_2: Admob: onAdLoaded()")
                }
            )
        } ?: Log.w("WTOneFragment", "ADMOB_NATIVE_WALKTHROUGH_2 ad ID is missing.")
    }
    private fun showMetaWTTwoNatives() {
        dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_WALKTHROUGH_2")?.let { adId ->
            MetaNativeAdManager.requestOrShowAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_2",
                isMediaWithCtaOnTop = true,
                remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_2").toString().toBoolean(),
                populateView = true,
                adContainer = binding.nativeAdContainerAd,
                onAdFailed = {
                    binding.nativeAdContainerAd.visibility = View.GONE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_2: Meta: onAdFailed()")
                },
                onAdLoaded = {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_2: Meta: onAdLoaded()")
                }
            )
        } ?: Log.w("WTOneFragment", "META_NATIVE_WALKTHROUGH_2 ad ID is missing.")
    }
    private fun showMintegralWTTwoBanner() {
        if (dpAdsConfigurations?.firstOpenFlowAdIds?.getValue("MINTEGRAL_BANNER_WALKTHROUGH_2")?.split("-")?.size == 2) {
            MintegralBannerAdManager.requestBannerAd(
                activity = requireActivity(),
                placementId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_BANNER_WALKTHROUGH_2").split("-")[0],
                unitId = dpAdsConfigurations!!.firstOpenFlowAdIds.getValue("MINTEGRAL_BANNER_WALKTHROUGH_2").split("-")[1],
                adName = "WALKTHROUGH_2",
                remoteConfig = dpAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_2").toString().toBoolean(),
                populateView = true,
                bannerContainer = binding.bannerAdMint,
                shimmerContainer = binding.shimmerLayout,
                onAdFailed = {
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_2: MINTEGRAL: onAdFailed()")
                },
                onAdLoaded = {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.INVISIBLE
                    binding.bannerAdMint.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_2: MINTEGRAL: onAdLoaded()")
                }
            )
        } else {
            Log.i("DP_ADS_TAG", "BANNER : Mintegral : MAY WALKTHROUGH_2 Incorrect ID Format (placementID-unitID)")
        }
    }

}