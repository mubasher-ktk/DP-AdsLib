package com.dp.ads.lib.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dp.ads.lib.R
import com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager
import com.dp.ads.lib.callingClasses.DPAdsConfigurations
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.databinding.FragmentWTOneBinding
import com.dp.ads.lib.data.WalkThroughItem
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.utils.NetworkCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WTOneFragment(val item: WalkThroughItem) : Fragment() {

    lateinit var binding: FragmentWTOneBinding
    private var DPAdsConfigurations: DPAdsConfigurations? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWTOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DPAdsConfigurations = DPAdsManager.getConfigurations()

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
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Glide.with(requireActivity())
                    .asDrawable()
                    .load(item.drawableBubble)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(true)
                    .into(binding.bubbleDup)
            }
        }

        binding.txtHeading.text = item.heading
        binding.txtDescription.text = item.description

        binding.btnNext.setOnClickListener {
            val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
            viewPager?.currentItem = 1
        }

        binding.btnNextDup.setOnClickListener {
            val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
            viewPager?.currentItem = 1
        }
    }

    override fun onResume() {
        super.onResume()
        if (NetworkCheck.isNetworkAvailable(context)) {
            binding.glOne.setGuidelinePercent(0.35f)
            binding.glTwo.setGuidelinePercent(0.5f)
            binding.cl2.visibility = View.VISIBLE
            binding.cl2Dup.visibility = View.GONE
        } else {
            binding.glOne.setGuidelinePercent(0.45f)
            binding.glTwo.setGuidelinePercent(0.8f)
            binding.cl2Dup.visibility = View.VISIBLE
            binding.cl2.visibility = View.GONE
        }

        val nativeSurvey2Enabled = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_1") as? Boolean ?: false
        if (nativeSurvey2Enabled) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_1_MED")) {
                "ADMOB" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showAdmobWTOneNatives()
                }
                "META" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showMetaWTOneNatives()
                }
            }
        } else {
            binding.nativeAdContainerAd.visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e("reloadAdAfterClickOrResume", "onStop: ")
    }

    private fun showMetaWTOneNatives() {
        DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("META_NATIVE_WALKTHROUGH_1")?.let { adId ->
            MetaNativeAdManager.requestAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_1",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_1").toString().toBoolean(),
                populateView = true,
                nativeAdLayout = binding.nativeAdContainerAd,
                onAdFailed = {
                    binding.nativeAdContainerAd.visibility = View.GONE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_1: Meta: onAdFailed()")
                },
                onAdLoaded = {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_1: Meta: onAdLoaded()")
                }
            )
        } ?: Log.w("WTOneFragment", "META_NATIVE_WALKTHROUGH_1 ad ID is missing.")
    }

    private fun showAdmobWTOneNatives() {
        DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_WALKTHROUGH_1")?.let { adId ->
            AdmobNativeAdManager.requestAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_1",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_1").toString().toBoolean(),
                populateView = true,
                adContainer = binding.nativeAdContainerAd,
                onAdFailed = {
                    binding.nativeAdContainerAd.visibility = View.GONE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_1: Admob: onAdFailed()")
                },
                onAdLoaded = {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    Log.i("DP_ADS_TAG", "WALKTHROUGH_1: Admob: onAdLoaded()")
                }
            )
        } ?: Log.w("WTOneFragment", "ADMOB_NATIVE_WALKTHROUGH_1 ad ID is missing.")
    }
}