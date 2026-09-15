package com.dp.ads.lib.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.dp.ads.lib.callingClasses.AdsFreeManager

/**
 * Receives the EXTRA_CHOSEN_COMPONENT broadcast fired by Intent.createChooser's PendingIntent
 * once the user actually picks a target app - the closest signal Android offers that the share
 * sheet wasn't just dismissed. Grants the one-time share reward only when a component was chosen.
 */
class ShareCompletionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val chosenComponent = intent.getParcelableExtra<ComponentName>(Intent.EXTRA_CHOSEN_COMPONENT)
        if (chosenComponent != null) {
            AdsFreeManager.claimShareReward(context)
        }
    }
}
