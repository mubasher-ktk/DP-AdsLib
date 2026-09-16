package com.dp.ads.lib.activities

import android.app.Dialog
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.dp.ads.lib.R
import com.dp.ads.lib.adMobAdClasses.AdMobRewardedInside
import com.dp.ads.lib.callingClasses.AdsFreeManager
import com.dp.ads.lib.callingClasses.DPAdsManager
import com.dp.ads.lib.databinding.ActivityAdsFreeRewardBinding
import com.dp.ads.lib.databinding.DialogAdsFreeRewardClaimedBinding
import com.dp.ads.lib.databinding.ItemAdsFreeStepBinding
import com.dp.ads.lib.metaAdClasses.MetaRewardedInside
import com.dp.ads.lib.receivers.ShareCompletionReceiver
import com.dp.ads.lib.utils.NetworkCheck
import com.dp.ads.lib.utils.hideSystemUIUpdated
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.concurrent.TimeUnit

/**
 * Earn screen for the "watch a rewarded ad, get ad-free time" flow.
 * Launch only via AdsFreeManager.launch(activity, ...) - never start this Activity directly,
 * since it depends on DPAdsManager.getConfigurations() already being set by the host app
 * (firstOpenFlowAdIds keys ADMOB_REWARDED_ADFREE / META_REWARDED_ADFREE, and remoteConfigData
 * keys REWARDED_ADFREE / REWARDED_ADFREE_MED).
 */
class AdsFreeRewardActivity : AppCompatBaseActivity() {

    companion object {
        const val EXTRA_BACKGROUND_COLOR = "EXTRA_BACKGROUND_COLOR"
        const val EXTRA_BACKGROUND_DRAWABLE_RES = "EXTRA_BACKGROUND_DRAWABLE_RES"
        private const val AD_NAME = "REWARDED_ADFREE"
        private const val SHARE_REQUEST_CODE = 4821
    }

    private lateinit var binding: ActivityAdsFreeRewardBinding
    private var isRequestingAd = false
    private var pendingRateRewardOnResume = false
    private var fullScreenAdLoaderDialog: Dialog? = null

    private val tickHandler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            refreshUi()
            tickHandler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityAdsFreeRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyBackground()

        binding.btnClose.setOnClickListener { finish() }
        binding.btnWatchVideo.setOnClickListener { onWatchVideoClicked() }

        binding.taskCheckIn.ivTaskIcon.setImageResource(R.drawable.ic_ads_free_calendar)
        binding.taskCheckIn.tvTaskTitle.text = getString(R.string.ads_free_checkin_title)
        binding.taskCheckIn.tvTaskAction.setOnClickListener { onCheckInClicked() }

        binding.taskRate.ivTaskIcon.setImageResource(R.drawable.ic_ads_free_star)
        binding.taskRate.tvTaskTitle.text = getString(R.string.ads_free_rate_title)
        binding.taskRate.tvTaskAction.setOnClickListener { onRateAppClicked() }

        binding.taskShare.ivTaskIcon.setImageResource(R.drawable.ic_ads_free_share)
        binding.taskShare.tvTaskTitle.text = getString(R.string.ads_free_share_title)
        binding.taskShare.tvTaskAction.setOnClickListener { onShareClicked() }

        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        if (pendingRateRewardOnResume) {
            pendingRateRewardOnResume = false
            grantRateReward()
        }
        tickHandler.removeCallbacks(tickRunnable)
        tickHandler.post(tickRunnable)
    }

    override fun onPause() {
        super.onPause()
        tickHandler.removeCallbacks(tickRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideFullScreenAdLoader()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUIUpdated()
    }

    private fun applyBackground() {
        val colorExtra = intent.getIntExtra(EXTRA_BACKGROUND_COLOR, 0)
        val drawableRes = intent.getIntExtra(EXTRA_BACKGROUND_DRAWABLE_RES, 0)
        if (drawableRes != 0) {
            binding.root.setBackgroundResource(drawableRes)
        } else if (colorExtra != 0) {
            binding.root.setBackgroundColor(colorExtra)
        }
    }

    private fun onWatchVideoClicked() {
        if (isRequestingAd) return

        if (!NetworkCheck.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.ads_free_network_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        val completed = AdsFreeManager.getStepsCompletedToday(this)
        if (completed >= AdsFreeManager.getTotalSteps()) return

        val dpAdsConfigurations = DPAdsManager.getConfigurations()
        val remoteConfigData = dpAdsConfigurations?.getRemoteConfigData()
        val isEnabled = remoteConfigData?.get(AD_NAME) as? Boolean ?: false
        if (!isEnabled) {
            Toast.makeText(this, R.string.ads_free_network_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        val adIds = dpAdsConfigurations?.firstOpenFlowAdIds
        val primaryNetwork = remoteConfigData?.get("${AD_NAME}_MED") as? String
        val primaryAdId = rewardedAdIdFor(adIds, primaryNetwork)
        if (primaryNetwork == null || primaryAdId.isNullOrBlank()) {
            Toast.makeText(this, R.string.ads_free_network_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        // The other network, held in reserve: if the backend-selected network fails to load or
        // show (anything short of "no internet"), we retry once on this one before giving up.
        val fallbackNetwork = if (primaryNetwork == "ADMOB") "META" else "ADMOB"
        val fallbackAdId = rewardedAdIdFor(adIds, fallbackNetwork)

        isRequestingAd = true
        binding.btnWatchVideo.isEnabled = false
        showFullScreenAdLoader()

        runRewardedAdFlow(
            network = primaryNetwork,
            adId = primaryAdId,
            fallbackNetwork = fallbackNetwork,
            fallbackAdId = fallbackAdId,
            isFallbackAttempt = false
        )
    }

    private fun rewardedAdIdFor(adIds: Map<String, String>?, network: String?): String? = when (network) {
        "ADMOB" -> adIds?.get("ADMOB_REWARDED_ADFREE")
        "META" -> adIds?.get("META_REWARDED_ADFREE")
        else -> null
    }

    /**
     * Runs the rewarded flow on [network]/[adId]. On a load-or-show failure (never on "no
     * network", never on "user closed before earning it") this retries exactly once on
     * [fallbackNetwork]/[fallbackAdId] before treating it as a final failure - so it recurses at
     * most one level deep (isFallbackAttempt guards against a second fallback).
     */
    private fun runRewardedAdFlow(
        network: String,
        adId: String,
        fallbackNetwork: String,
        fallbackAdId: String?,
        isFallbackAttempt: Boolean
    ) {
        val onRewardEarned: () -> Unit = {
            isRequestingAd = false
            hideFullScreenAdLoader()
            val earnedMillis = AdsFreeManager.grantNextStepReward(this)
            refreshUi()
            showRewardClaimedDialog(earnedMillis)
        }

        val onTerminalFailure: () -> Unit = {
            isRequestingAd = false
            hideFullScreenAdLoader()
            refreshUi()
            Toast.makeText(this, R.string.ads_free_no_reward, Toast.LENGTH_SHORT).show()
        }

        val onAdFailedToLoadOrShow: () -> Unit = {
            if (!isFallbackAttempt && !fallbackAdId.isNullOrBlank() && !isFinishing && !isDestroyed) {
                Log.i("DP_ADS_TAG", "$network rewarded failed, falling back to $fallbackNetwork: $AD_NAME")
                runRewardedAdFlow(
                    network = fallbackNetwork,
                    adId = fallbackAdId,
                    fallbackNetwork = network,
                    fallbackAdId = adId,
                    isFallbackAttempt = true
                )
            } else {
                onTerminalFailure()
            }
        }

        when (network) {
            "ADMOB" -> AdMobRewardedInside.requestAndShowRewardedAd(
                context = this,
                adName = AD_NAME,
                adId = adId,
                onRewardEarned = onRewardEarned,
                onNoNetwork = onTerminalFailure,
                onAdFailedToLoadOrShow = onAdFailedToLoadOrShow,
                onDismissedWithoutReward = onTerminalFailure
            )
            "META" -> MetaRewardedInside.requestAndShowRewardedAd(
                context = this,
                adName = AD_NAME,
                adId = adId,
                onRewardEarned = onRewardEarned,
                onNoNetwork = onTerminalFailure,
                onAdFailedToLoadOrShow = onAdFailedToLoadOrShow,
                onDismissedWithoutReward = onTerminalFailure
            )
        }
    }

    /** Fullscreen, non-cancelable loader shown for the whole click-to-resolution window (including
     * a fallback attempt) so a monkey-clicked tap on Watch Ad has nothing left to hit. */
    private fun showFullScreenAdLoader() {
        if (isFinishing || isDestroyed) return
        if (fullScreenAdLoaderDialog?.isShowing == true) return
        val view = layoutInflater.inflate(R.layout.dialog_adloading, null, false)
        fullScreenAdLoaderDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(view)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
            show()
        }
    }

    private fun hideFullScreenAdLoader() {
        try {
            fullScreenAdLoaderDialog?.let { if (it.isShowing) it.dismiss() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fullScreenAdLoaderDialog = null
    }

    private fun refreshUi() {
        val remainingMillis = AdsFreeManager.getRemainingMillis(this)
        binding.tvBankedToday.text = getString(R.string.ads_free_banked_today, formatDuration(remainingMillis))

        val completed = AdsFreeManager.getStepsCompletedToday(this)
        val total = AdsFreeManager.getTotalSteps()
        binding.tvStepsProgress.text = getString(R.string.ads_free_steps_progress, completed, total)

        val progressFraction = if (total > 1) completed.toFloat() / (total - 1).toFloat() else 0f
        binding.viewTrackBg.post {
            val trackWidth = binding.viewTrackBg.width
            val params = binding.viewTrackProgress.layoutParams
            params.width = (trackWidth * progressFraction.coerceIn(0f, 1f)).toInt()
            binding.viewTrackProgress.layoutParams = params
        }

        val stepBindings = listOf(binding.step1, binding.step2, binding.step3, binding.step4, binding.step5)
        stepBindings.forEachIndexed { index, stepBinding ->
            renderStep(stepBinding, index, completed, total)
        }

        val allDone = completed >= total
        binding.btnWatchVideo.isEnabled = !allDone && !isRequestingAd
        binding.tvWatchVideoLabel.text = if (allDone) {
            getString(R.string.ads_free_all_done_countdown, formatDuration(AdsFreeManager.millisUntilStepsReset(this)))
        } else {
            getString(R.string.ads_free_watch_video, completed, total)
        }
        binding.btnWatchVideo.setBackgroundResource(
            if (allDone) R.drawable.bg_ads_free_watch_button_disabled else R.drawable.bg_ads_free_watch_button
        )
        val watchButtonContentColorRes = if (allDone) R.color.adsFreeStepLockedIcon else R.color.White
        binding.tvWatchVideoLabel.setTextColor(ContextCompat.getColor(this, watchButtonContentColorRes))
        binding.ivWatchVideoIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, watchButtonContentColorRes))
        binding.ivWatchVideoIcon.visibility = if (allDone) View.GONE else View.VISIBLE

        refreshBonusTasks()
    }

    private fun refreshBonusTasks() {
        refreshCheckInTask()
        refreshRateTask()
        refreshShareTask()
    }

    private fun refreshCheckInTask() {
        val claimedToday = AdsFreeManager.isCheckInClaimedToday(this)
        val streak = AdsFreeManager.getCheckInStreak(this)
        val target = AdsFreeManager.CHECK_IN_STREAK_TARGET
        val displayDay = if (claimedToday) {
            if (streak == 0) target else streak
        } else {
            streak + 1
        }

        binding.taskCheckIn.tvTaskSubtitle.text = if (claimedToday) {
            getString(R.string.ads_free_checkin_subtitle_next, displayDay, target, formatHours(AdsFreeManager.millisUntilNextCheckIn(this)))
        } else {
            getString(R.string.ads_free_checkin_subtitle_progress, displayDay, target)
        }

        setTaskActionState(binding.taskCheckIn.tvTaskAction, isClaimed = claimedToday, doneLabel = getString(R.string.ads_free_checkin_action_done), goLabel = getString(R.string.ads_free_checkin_action_go))
    }

    private fun refreshRateTask() {
        val claimed = AdsFreeManager.isRateRewardClaimed(this)
        binding.taskRate.tvTaskSubtitle.text = getString(
            if (claimed) R.string.ads_free_rate_subtitle_claimed else R.string.ads_free_rate_subtitle
        )
        setTaskActionState(binding.taskRate.tvTaskAction, isClaimed = claimed, doneLabel = getString(R.string.ads_free_rate_action_done), goLabel = getString(R.string.ads_free_rate_action_go))
    }

    private fun refreshShareTask() {
        val claimed = AdsFreeManager.isShareRewardClaimed(this)
        binding.taskShare.tvTaskSubtitle.text = getString(
            if (claimed) R.string.ads_free_share_subtitle_claimed else R.string.ads_free_share_subtitle
        )
        setTaskActionState(binding.taskShare.tvTaskAction, isClaimed = claimed, doneLabel = getString(R.string.ads_free_share_action_done), goLabel = getString(R.string.ads_free_share_action_go))
    }

    private fun setTaskActionState(actionView: android.widget.TextView, isClaimed: Boolean, doneLabel: String, goLabel: String) {
        actionView.text = if (isClaimed) doneLabel else goLabel
        actionView.isEnabled = !isClaimed
        actionView.setBackgroundResource(if (isClaimed) R.drawable.bg_ads_free_task_action_done else R.drawable.bg_ads_free_task_action_go)
        actionView.setTextColor(
            ContextCompat.getColor(this, if (isClaimed) R.color.adsFreeStepLockedIcon else R.color.White)
        )
    }

    private fun onCheckInClicked() {
        if (AdsFreeManager.isCheckInClaimedToday(this)) return
        val earnedMillis = AdsFreeManager.claimDailyCheckIn(this)
        refreshUi()
        if (earnedMillis > 0L) {
            Toast.makeText(this, R.string.ads_free_bonus_reward_granted, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onRateAppClicked() {
        if (AdsFreeManager.isRateRewardClaimed(this)) return

        val reviewManager = ReviewManagerFactory.create(this)
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Play Core's own flow was actually requested and run (shown-or-silently-skipped
                // is entirely Play Core's call) - safe to grant as soon as it finishes.
                reviewManager.launchReviewFlow(this, task.result).addOnCompleteListener {
                    grantRateReward()
                }
            } else {
                // No in-app review available here (e.g. not installed from Play Store, as on a
                // sideloaded/debug build). Send the user to the Play Store listing instead, and
                // only grant the reward once they've actually left and come back to the app -
                // never synchronously in this same click.
                openPlayStoreListing()
                pendingRateRewardOnResume = true
            }
        }
    }

    private fun grantRateReward() {
        val earnedMillis = AdsFreeManager.claimRateReward(this)
        refreshUi()
        if (earnedMillis > 0L) {
            Toast.makeText(this, R.string.ads_free_bonus_reward_granted, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPlayStoreListing() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreWebUrl())))
        }
    }

    /** market:// isn't renderable/clickable in most share targets (WhatsApp, SMS, etc.) - shared text always needs the plain https:// listing link. */
    private fun playStoreWebUrl(): String = "https://play.google.com/store/apps/details?id=$packageName"

    private fun onShareClicked() {
        if (AdsFreeManager.isShareRewardClaimed(this)) return

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.ads_free_share_message, playStoreWebUrl()))
        }

        val receiverIntent = Intent(this, ShareCompletionReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(this, SHARE_REQUEST_CODE, receiverIntent, flags)

        val chooserIntent = Intent.createChooser(
            sendIntent,
            getString(R.string.ads_free_share_chooser_title),
            pendingIntent.intentSender
        )
        startActivity(chooserIntent)
    }

    private fun renderStep(stepBinding: ItemAdsFreeStepBinding, index: Int, completed: Int, total: Int) {
        val isFinalStep = index == total - 1
        val isCompleted = index < completed
        val isCurrent = index == completed

        stepBinding.tvStepLabel.text = if (isFinalStep) {
            getString(R.string.ads_free_step_full_day)
        } else {
            getString(R.string.ads_free_step_hours, TimeUnit.MILLISECONDS.toHours(AdsFreeManager.stepRewardsMillis[index]))
        }

        when {
            isCompleted -> {
                stepBinding.flStepCircle.setBackgroundResource(R.drawable.bg_ads_free_step_completed)
                stepBinding.ivStepIcon.setImageResource(R.drawable.ic_ads_free_check)
                stepBinding.ivStepIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.White))
                stepBinding.tvStepLabel.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            isCurrent -> {
                stepBinding.flStepCircle.setBackgroundResource(R.drawable.bg_ads_free_step_current)
                stepBinding.ivStepIcon.setImageResource(R.drawable.ic_ads_free_clock)
                stepBinding.ivStepIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue))
                stepBinding.tvStepLabel.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            else -> {
                val lockedBgRes = if (isFinalStep) R.drawable.bg_ads_free_step_premium else R.drawable.bg_ads_free_step_locked
                val lockedIconColorRes = if (isFinalStep) R.color.adsFreePremiumIcon else R.color.adsFreeStepLockedIcon
                stepBinding.flStepCircle.setBackgroundResource(lockedBgRes)
                stepBinding.ivStepIcon.setImageResource(if (isFinalStep) R.drawable.ic_premium_crown else R.drawable.ic_ads_free_lock)
                stepBinding.ivStepIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, lockedIconColorRes))
                stepBinding.tvStepLabel.setTextColor(ContextCompat.getColor(this, R.color.adsFreeStepLockedIcon))
            }
        }
    }

    private fun showRewardClaimedDialog(earnedMillis: Long) {
        if (isFinishing || isDestroyed) return

        val dialogBinding = DialogAdsFreeRewardClaimedBinding.inflate(layoutInflater)
        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(dialogBinding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setElevation(0f)
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.88f).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
            setCancelable(true)
        }

        val earnedLabel = formatHours(earnedMillis)
        dialogBinding.tvDialogSubtitle.text = getString(R.string.ads_free_reward_subtitle, earnedLabel)
        dialogBinding.tvRewardPill.text = getString(R.string.ads_free_reward_pill_hours, earnedLabel)

        val completed = AdsFreeManager.getStepsCompletedToday(this)
        val total = AdsFreeManager.getTotalSteps()
        if (completed >= total) {
            dialogBinding.btnWatchAnother.visibility = View.GONE
            dialogBinding.tvDialogSubtitle.text = getString(R.string.ads_free_all_claimed_today)
        } else {
            val nextRewardLabel = formatHours(AdsFreeManager.stepRewardsMillis[completed])
            dialogBinding.tvWatchAnotherLabel.text = getString(R.string.ads_free_watch_another, nextRewardLabel)
            dialogBinding.btnWatchAnother.setOnClickListener {
                dialog.dismiss()
                onWatchVideoClicked()
            }
        }

        dialogBinding.btnDialogClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnMaybeLater.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun formatHours(millis: Long): String {
        // Ceiling, not truncating: a few ms of real time always pass between granting a reward
        // and reading it back here, so a plain toHours() would floor a just-banked "1h" to "0h".
        val hourMillis = TimeUnit.HOURS.toMillis(1)
        val hours = if (millis <= 0L) 0L else (millis + hourMillis - 1) / hourMillis
        return getString(R.string.ads_free_hours_short, hours)
    }

    /** Live HH:MM:SS countdown display - used for the ticking banked-time pill and the full-track countdown. */
    private fun formatDuration(millis: Long): String {
        val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
