package com.example.volumetile

import android.media.AudioManager
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

    private val audioManager: AudioManager by lazy {
        getSystemService(AUDIO_SERVICE) as AudioManager
    }

    // -------------------------------------------------------------------------
    // TileService lifecycle
    // -------------------------------------------------------------------------

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        val stream = selectedStream()
        audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_TOGGLE_MUTE, 0)
        refreshTile()
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Updates the tile icon and state to reflect the current mute status. */
    private fun refreshTile() {
        val tile = qsTile ?: return
        val stream = selectedStream()
        val isMuted = audioManager.isStreamMute(stream)

        tile.state = if (isMuted) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.icon = android.graphics.drawable.Icon.createWithResource(
            this,
            if (isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_on
        )
        tile.updateTile()
    }

    /** Returns the AudioManager stream constant saved by VolumeSettingsActivity. */
    private fun selectedStream(): Int {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getInt(KEY_STREAM, AudioManager.STREAM_MUSIC)
    }

    companion object {
        const val PREFS_NAME = "volume_tile_prefs"
        const val KEY_STREAM = "selected_stream"
    }
}
