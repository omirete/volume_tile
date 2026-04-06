package com.example.volumetile

import android.media.AudioManager
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Dialog-style Activity launched when the user long-presses the Volume tile.
 *
 * Provides:
 *  - A Spinner to select which audio stream the tile controls.
 *  - A SeekBar to set that stream's volume in real time.
 *
 * Both selections are persisted in SharedPreferences so VolumeTileService
 * picks them up the next time it refreshes.
 */
class VolumeSettingsActivity : AppCompatActivity() {

    // AudioManager stream constants paired with the string-array in strings.xml.
    // The order here MUST match the <string-array name="stream_names"> order.
    private val streams = intArrayOf(
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_RING,
        AudioManager.STREAM_ALARM,
        AudioManager.STREAM_NOTIFICATION
    )

    private lateinit var audioManager: AudioManager
    private lateinit var seekBar: SeekBar
    private lateinit var tvVolumeLevel: TextView

    /** Tracks whether the spinner callback should react (suppresses init-time calls). */
    private var spinnerReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume_settings)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        seekBar = findViewById(R.id.seekBarVolume)
        tvVolumeLevel = findViewById(R.id.tvVolumeLevel)
        val spinner: Spinner = findViewById(R.id.spinnerStream)

        // Populate the Spinner with human-readable stream names.
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.stream_names,
            android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinner.adapter = adapter

        // Restore the previously selected stream index.
        val savedStream = savedStream()
        val savedIndex = streams.indexOfFirst { it == savedStream }.coerceAtLeast(0)
        spinner.setSelection(savedIndex, false)

        // Load initial SeekBar state for the restored stream.
        updateSeekBar(savedStream)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                if (!spinnerReady) {
                    // First callback fires during layout — ignore it.
                    spinnerReady = true
                    return
                }
                val stream = streams[pos]
                saveStream(stream)
                updateSeekBar(stream)
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                tvVolumeLevel.text = progress.toString()
                if (fromUser) {
                    val stream = streams[spinner.selectedItemPosition]
                    // FLAG_SHOW_UI = 0 so we don't summon the system volume pop-up each drag.
                    audioManager.setStreamVolume(stream, progress, 0)
                }
            }

            override fun onStartTrackingTouch(bar: SeekBar) = Unit
            override fun onStopTrackingTouch(bar: SeekBar) = Unit
        })
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Configures the SeekBar range and current value for [stream]. */
    private fun updateSeekBar(stream: Int) {
        val max = audioManager.getStreamMaxVolume(stream)
        val current = audioManager.getStreamVolume(stream)
        seekBar.max = max
        seekBar.progress = current
        tvVolumeLevel.text = current.toString()
    }

    private fun savedStream(): Int {
        val prefs = getSharedPreferences(VolumeTileService.PREFS_NAME, MODE_PRIVATE)
        return prefs.getInt(VolumeTileService.KEY_STREAM, AudioManager.STREAM_MUSIC)
    }

    private fun saveStream(stream: Int) {
        getSharedPreferences(VolumeTileService.PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(VolumeTileService.KEY_STREAM, stream)
            .apply()
    }
}
