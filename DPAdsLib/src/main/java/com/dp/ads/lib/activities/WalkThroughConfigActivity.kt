package com.dp.ads.lib.activities

import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.dp.ads.lib.R
import com.dp.ads.lib.adapters.WalkThroughAdapter
import com.dp.ads.lib.callingClasses.DPAdsConfigurations
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.databinding.ActivityWalkThroughConfigBinding
import com.dp.ads.lib.utils.NetworkCheck
import com.dp.ads.lib.utils.hideSystemUI

class WalkThroughConfigActivity : AppCompatBaseActivity() {

    lateinit var binding: ActivityWalkThroughConfigBinding
    private var DPAdsConfigurations: DPAdsConfigurations? = null
    private lateinit var viewPager: ViewPager2
    private var previousPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        hideSystemUI()
        binding = ActivityWalkThroughConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        DPAdsConfigurations = DPAdsManager.getConfigurations()
        viewPager = findViewById(R.id.viewPager)

        val myNoOfFrag = DPAdsConfigurations?.getRemoteConfigData()?.get("NATIVE_WALKTHROUGH_FULLSCR")
        val noOfFragment = if (NetworkCheck.isNetworkAvailable(this) && myNoOfFrag == true) {
            4
        } else {
            3
        }

        viewPager.adapter = WalkThroughAdapter(fragmentActivity = this, DPAdsConfigurations?.walkThroughScreensConfiguration?.walkThroughList!!, noOfFragment)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (previousPosition != -1) {
                    when (previousPosition) {
                        0 -> if (position == 1) {
                            Log.i("WalkThroughConfigActivity","previousPosition: $previousPosition :: $position")
                        }
                        1 -> if (position == 2) {
                            Log.i("WalkThroughConfigActivity","previousPosition: $previousPosition :: $position")
                        } else if (position == 0) {
                            Log.i("WalkThroughConfigActivity","previousPosition: $previousPosition :: $position")
                        }
                        2 -> if (position == 1) {
                            Log.i("WalkThroughConfigActivity","previousPosition: $previousPosition :: $position")
                        }
                    }
                }
                previousPosition = position
            }
        })
    }
}