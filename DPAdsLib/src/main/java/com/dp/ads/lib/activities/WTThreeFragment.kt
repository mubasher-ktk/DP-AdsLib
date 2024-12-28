package com.dp.ads.lib.activities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager
import com.dp.ads.lib.callingClasses.DPAdsConfigurations
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.databinding.FragmentWTThreeBinding
import com.dp.ads.lib.data.WalkThroughItem
import com.dp.ads.lib.metaAdClasses.MetaNativeAdManager
import com.dp.ads.lib.utils.NetworkCheck
import com.dp.ads.lib.utils.PrefHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WTThreeFragment(private val fragmentActivity: FragmentActivity, val item: WalkThroughItem) : Fragment() {

    lateinit var binding: FragmentWTThreeBinding
    private var DPAdsConfigurations: DPAdsConfigurations? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWTThreeBinding.inflate(inflater, container, false)
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
            PrefHelper(requireContext()).putBoolean("StartScreens", value = true)
            DPAdsManager.notifyFlowFinished()
            fragmentActivity.finish()
        }

        binding.btnNextDup.setOnClickListener {
            PrefHelper(requireContext()).putBoolean("StartScreens", value = true)
            DPAdsManager.notifyFlowFinished()
            fragmentActivity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val nativeSurvey2Enabled = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_3") as? Boolean ?: false
        if (nativeSurvey2Enabled) {
            when (DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_3_MED")) {
                "ADMOB" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showAdmobWTThreeNatives()
                }
                "META" -> {
                    binding.nativeAdContainerAd.visibility = View.VISIBLE
                    showMetaWTThreeNatives()
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

    private fun showMetaWTThreeNatives() {
        DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("NATIVE_WALKTHROUGH_3")?.let { adId ->
            MetaNativeAdManager.requestAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_3",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_3").toString().toBoolean(),
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
        DPAdsConfigurations?.firstOpenFlowAdIds?.getValue("ADMOB_NATIVE_WALKTHROUGH_3")?.let { adId ->
            AdmobNativeAdManager.requestAd(
                mContext = requireActivity(),
                adId = adId,
                adName = "WALKTHROUGH_3",
                isMedia = true,
                isMediumAd = true,
                remoteConfig = DPAdsConfigurations?.getRemoteConfigData()?.getValue("NATIVE_WALKTHROUGH_3").toString().toBoolean(),
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
}