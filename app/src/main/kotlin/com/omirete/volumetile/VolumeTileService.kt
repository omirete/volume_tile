package com.omirete.volumetile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Quick Settings tile that toggles mute for the user's chosen audio stream.
 *
 * Tap  → toggle mute on the selected stream and update the tile icon/state.
 * Long-press → Android automatically routes to VolumeSettingsActivity via the
 *              ACTION_QS_TILE_PREFERENCES intent-filter declared in the manifest.
 *              No code is required here for that.
 *
 * The tile is declared as ACTIVE_TILE (meta-data in manifest), meaning it is
 * responsible for updating its own state rather than subscribing to broadcasts.
 */
class VolumeTileService : TileService() {

    // -------------------------------------------------------------------------
    // TileService lifecycle
    // -------------------------------------------------------------------------

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, VolumeSettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pending = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Keeps the tile in the active (coloured) state with the volume-on icon. */
    private fun refreshTile() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_ACTIVE
        tile.icon = android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_volume_on)
        tile.updateTile()
    }

    companion object {
        const val PREFS_NAME = "volume_tile_prefs"
        const val KEY_STREAM = "selected_stream"
    }
}
