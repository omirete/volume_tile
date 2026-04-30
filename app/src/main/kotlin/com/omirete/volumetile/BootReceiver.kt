package com.omirete.volumetile

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.quicksettings.TileService

/**
 * Receives ACTION_BOOT_COMPLETED and asks the system to call
 * [VolumeTileService.onStartListening], which restores the tile to its active
 * (coloured) state after a device reboot.
 *
 * Without this, ACTIVE_TILEs stay in STATE_UNAVAILABLE (greyed out) until the
 * user opens the Quick Settings panel for the first time after boot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            TileService.requestListeningState(
                context,
                ComponentName(context, VolumeTileService::class.java)
            )
        }
    }
}
