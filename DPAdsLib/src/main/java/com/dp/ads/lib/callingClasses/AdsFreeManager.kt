package com.dp.ads.lib.callingClasses

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.dp.ads.lib.activities.AdsFreeRewardActivity
import com.dp.ads.lib.utils.PrefHelper

/**
 * Single entry point host apps use for the "watch a rewarded ad, earn ad-free time" feature.
 * Everything the host app needs is these three calls:
 *  - launch(activity)               -> open the earn screen
 *  - isAdFreeActive(context)        -> gate before showing any other ad format
 *  - getRemainingMillis(context)    -> optional, for a host-side countdown UI
 */
object AdsFreeManager {

    private const val PREF_AD_FREE_EXPIRY_MILLIS = "ads_free_expiry_millis"
    private const val PREF_STEPS_COMPLETED = "ads_free_steps_completed_today"
    private const val PREF_STEPS_DAY_STAMP = "ads_free_steps_day_stamp"

    private const val PREF_CHECKIN_LAST_MILLIS = "ads_free_checkin_last_millis"
    private const val PREF_CHECKIN_STREAK_COUNT = "ads_free_checkin_streak_count"
    private const val PREF_RATE_CLAIMED = "ads_free_rate_claimed"
    private const val PREF_SHARE_CLAIMED = "ads_free_share_claimed"

    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    private const val HOUR_MILLIS = 60L * 60L * 1000L
    private const val MINUTE_MILLIS = 60L * 1000L

    /** Shared one-time/streak-completion reward for all three bonus tasks below. */
    const val BONUS_TASK_REWARD_MILLIS = 30L * MINUTE_MILLIS
    const val CHECK_IN_STREAK_TARGET = 3

    /** Rolling cooldown between check-ins - a full 24h from the last check-in, not a calendar-day reset. */
    private const val CHECK_IN_COOLDOWN_MILLIS = DAY_MILLIS
    /** A gap of 2 full cooldown windows without a check-in breaks the streak. */
    private const val CHECK_IN_STREAK_BREAK_MILLIS = 2L * DAY_MILLIS

    /** One reward, in millis, per milestone step. The last step is the "full day" milestone. */
    val stepRewardsMillis: LongArray = longArrayOf(
        1L * HOUR_MILLIS,
        3L * HOUR_MILLIS,
        6L * HOUR_MILLIS,
        12L * HOUR_MILLIS,
        DAY_MILLIS
    )

    fun getTotalSteps(): Int = stepRewardsMillis.size

    fun isAdFreeActive(context: Context): Boolean = getRemainingMillis(context) > 0L

    fun getRemainingMillis(context: Context): Long {
        val expiryMillis = PrefHelper(context).getLongDefault(PREF_AD_FREE_EXPIRY_MILLIS, 0L)
        return (expiryMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun getStepsCompletedToday(context: Context): Int {
        resetStepsIfNewDay(context)
        return PrefHelper(context).getIntDefault(PREF_STEPS_COMPLETED, 0)
    }

    /**
     * Advances the milestone track by one step and banks that step's reward.
     * Returns the millis just earned, or 0L if today's steps are already exhausted.
     * Call this ONLY from the ad-network's earned-reward callback, never on a plain dismiss.
     */
    fun grantNextStepReward(context: Context): Long {
        resetStepsIfNewDay(context)
        val prefHelper = PrefHelper(context)
        val completed = prefHelper.getIntDefault(PREF_STEPS_COMPLETED, 0)
        if (completed >= stepRewardsMillis.size) return 0L

        val rewardMillis = stepRewardsMillis[completed]
        val currentExpiryMillis = prefHelper.getLongDefault(PREF_AD_FREE_EXPIRY_MILLIS, 0L)
        val newExpiryMillis = maxOf(System.currentTimeMillis(), currentExpiryMillis) + rewardMillis

        prefHelper.putLong(PREF_AD_FREE_EXPIRY_MILLIS, newExpiryMillis)
        prefHelper.putInt(PREF_STEPS_COMPLETED, completed + 1)
        return rewardMillis
    }

    private fun resetStepsIfNewDay(context: Context) {
        val prefHelper = PrefHelper(context)
        val todayStamp = System.currentTimeMillis() / DAY_MILLIS
        val storedStamp = prefHelper.getLongDefault(PREF_STEPS_DAY_STAMP, -1L)
        if (storedStamp != todayStamp) {
            prefHelper.putLong(PREF_STEPS_DAY_STAMP, todayStamp)
            prefHelper.putInt(PREF_STEPS_COMPLETED, 0)
        }
    }

    /** Extends the banked ad-free expiry by [millis] without touching the milestone-step counter. */
    private fun grantBonusMillis(context: Context, millis: Long): Long {
        val prefHelper = PrefHelper(context)
        val currentExpiryMillis = prefHelper.getLongDefault(PREF_AD_FREE_EXPIRY_MILLIS, 0L)
        val newExpiryMillis = maxOf(System.currentTimeMillis(), currentExpiryMillis) + millis
        prefHelper.putLong(PREF_AD_FREE_EXPIRY_MILLIS, newExpiryMillis)
        return millis
    }

    // ---- Bonus task 1: daily check-in streak (pays out only when the streak hits 3) ----
    // Cooldown and streak-break are both rolling windows measured from the last check-in's real
    // timestamp - NOT a calendar-day stamp - so "next check-in" is always a full 24h away.

    /** 1-based day count within the current streak (0 if no check-in has happened yet or the streak just paid out). */
    fun getCheckInStreak(context: Context): Int {
        resetCheckInStreakIfBroken(context)
        return PrefHelper(context).getIntDefault(PREF_CHECKIN_STREAK_COUNT, 0)
    }

    fun isCheckInClaimedToday(context: Context): Boolean {
        val lastMillis = PrefHelper(context).getLongDefault(PREF_CHECKIN_LAST_MILLIS, -1L)
        if (lastMillis < 0L) return false
        return System.currentTimeMillis() - lastMillis < CHECK_IN_COOLDOWN_MILLIS
    }

    fun millisUntilNextCheckIn(context: Context): Long {
        val lastMillis = PrefHelper(context).getLongDefault(PREF_CHECKIN_LAST_MILLIS, -1L)
        if (lastMillis < 0L) return 0L
        val nextEligibleMillis = lastMillis + CHECK_IN_COOLDOWN_MILLIS
        return (nextEligibleMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    /**
     * Advances the check-in streak by one. Returns the millis earned - only non-zero once the
     * streak reaches CHECK_IN_STREAK_TARGET, at which point the streak resets to 0.
     * Returns 0L if still within the 24h cooldown of the last check-in.
     */
    fun claimDailyCheckIn(context: Context): Long {
        if (isCheckInClaimedToday(context)) return 0L
        resetCheckInStreakIfBroken(context)

        val prefHelper = PrefHelper(context)
        val streak = prefHelper.getIntDefault(PREF_CHECKIN_STREAK_COUNT, 0) + 1

        prefHelper.putLong(PREF_CHECKIN_LAST_MILLIS, System.currentTimeMillis())

        return if (streak >= CHECK_IN_STREAK_TARGET) {
            prefHelper.putInt(PREF_CHECKIN_STREAK_COUNT, 0)
            grantBonusMillis(context, BONUS_TASK_REWARD_MILLIS)
        } else {
            prefHelper.putInt(PREF_CHECKIN_STREAK_COUNT, streak)
            0L
        }
    }

    /** A streak breaks (resets to 0) once more than a full cooldown window is missed entirely. */
    private fun resetCheckInStreakIfBroken(context: Context) {
        val prefHelper = PrefHelper(context)
        val lastMillis = prefHelper.getLongDefault(PREF_CHECKIN_LAST_MILLIS, -1L)
        if (lastMillis >= 0L && System.currentTimeMillis() - lastMillis >= CHECK_IN_STREAK_BREAK_MILLIS) {
            prefHelper.putInt(PREF_CHECKIN_STREAK_COUNT, 0)
        }
    }

    // ---- Bonus task 2: rate the app (one-time reward, granted on triggering the review flow) ----

    fun isRateRewardClaimed(context: Context): Boolean =
        PrefHelper(context).getBooleanDefault(PREF_RATE_CLAIMED, false)

    /** Grants the one-time rate-app reward. Call this once the in-app review flow has been triggered. */
    fun claimRateReward(context: Context): Long {
        if (isRateRewardClaimed(context)) return 0L
        PrefHelper(context).putBoolean(PREF_RATE_CLAIMED, true)
        return grantBonusMillis(context, BONUS_TASK_REWARD_MILLIS)
    }

    // ---- Bonus task 3: share with a friend (one-time reward, granted once a target app is picked) ----

    fun isShareRewardClaimed(context: Context): Boolean =
        PrefHelper(context).getBooleanDefault(PREF_SHARE_CLAIMED, false)

    /** Grants the one-time share reward. Call this once the share sheet reports a component was chosen. */
    fun claimShareReward(context: Context): Long {
        if (isShareRewardClaimed(context)) return 0L
        PrefHelper(context).putBoolean(PREF_SHARE_CLAIMED, true)
        return grantBonusMillis(context, BONUS_TASK_REWARD_MILLIS)
    }

    /**
     * Opens the earn screen. Ad unit IDs and the AdMob/Meta network switch are read from the
     * existing DPAdsConfigurations set up via DPAdsManager.startFlow(...) - firstOpenFlowAdIds
     * keys "ADMOB_REWARDED_ADFREE" / "META_REWARDED_ADFREE", and remoteConfigData keys
     * "REWARDED_ADFREE" (Boolean, enabled) / "REWARDED_ADFREE_MED" ("ADMOB"|"META").
     *
     * backgroundColor / backgroundDrawableRes let the host app theme the screen; leave both
     * null to use the library's default background.
     */
    fun launch(activity: Activity, backgroundColor: Int? = null, backgroundDrawableRes: Int? = null) {
        val intent = Intent(activity, AdsFreeRewardActivity::class.java)
        backgroundColor?.let { intent.putExtra(AdsFreeRewardActivity.EXTRA_BACKGROUND_COLOR, it) }
        backgroundDrawableRes?.let { intent.putExtra(AdsFreeRewardActivity.EXTRA_BACKGROUND_DRAWABLE_RES, it) }
        activity.startActivity(intent)
    }
}
