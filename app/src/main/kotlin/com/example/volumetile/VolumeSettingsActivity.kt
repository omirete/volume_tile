package com.example.volumetile

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings

class VolumeSettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == "android.service.quicksettings.action.QS_TILE_PREFERENCES") {
            startActivity(Intent(Settings.Panel.ACTION_VOLUME))
        } else {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
        }
        finish()
    }
}
