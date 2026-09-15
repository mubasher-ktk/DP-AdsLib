package com.dp.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dp.ads.lib.callingClasses.AdsFreeManager
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.main.databinding.ActivityFinalBinding
import java.util.concurrent.TimeUnit

class FinalActivity : AppCompatActivity() {

    private var binding: ActivityFinalBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFinalBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding?.btnRewardedDemo?.setOnClickListener {
            launchAdsFreeRewardDemo()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAdsFreeStatusCard()
    }

    /**
     * Example host-app wiring for the "watch a rewarded ad, earn ad-free time" feature.
     * DPAdsManager.getConfigurations() already holds the DPAdsConfigurations built in
     * MainActivity, so this only ADDS the two new keys this feature needs - it never
     * calls setRemoteConfigData(...) again, since that method has side effects (it
     * re-triggers the old splash/resume/native ad flows).
     */
    private fun launchAdsFreeRewardDemo() {
        val dpAdsConfigurations = DPAdsManager.getConfigurations()
        val remoteConfigData = dpAdsConfigurations?.getRemoteConfigData()

        if (dpAdsConfigurations == null || remoteConfigData == null) {
            Toast.makeText(this, "DPAdsConfigurations isn't set up yet - go through MainActivity first.", Toast.LENGTH_SHORT).show()
            return
        }

        // Ad unit IDs - same firstOpenFlowAdIds map/convention every other ad slot uses.
        // AdMob: Google's official public test Rewarded ad unit ID (safe to ship in this demo).
        dpAdsConfigurations.firstOpenFlowAdIds["ADMOB_REWARDED_ADFREE"] = "ca-app-pub-3940256099942544/5224354917"
        // Meta: there's no universal public test placement ID the way AdMob has one - this is a
        // placeholder in the same style your other META_* entries use. It's never read below since
        // AdMob is set as the priority network, but replace it with a real placement ID before you
        // ever flip REWARDED_ADFREE_MED to "META".
        dpAdsConfigurations.firstOpenFlowAdIds["META_REWARDED_ADFREE"] = "VID_HD_9_16_39S_APP_INSTALL#YOUR_PLACEMENT_ID"

        // Network switch - same remoteConfigData map/convention every other slot uses.
        remoteConfigData["REWARDED_ADFREE"] = true
        remoteConfigData["REWARDED_ADFREE_MED"] = "META"

        AdsFreeManager.launch(this)
    }

    /**
     * Example of how a host app's own Home screen shows an "ad-free active" status without any
     * new library code - AdsFreeManager.isAdFreeActive/getRemainingMillis are plain functions.
     */
    private fun refreshAdsFreeStatusCard() {
        val statusView = binding?.tvAdsFreeStatus ?: return
        if (AdsFreeManager.isAdFreeActive(this)) {
            val remainingMillis = AdsFreeManager.getRemainingMillis(this)
            val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
            statusView.text = "Ad-free active - ${hours}h ${minutes}m left"
            statusView.visibility = View.VISIBLE
        } else {
            statusView.visibility = View.GONE
        }
    }
}
