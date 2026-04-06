package com.example.volumetile

import android.media.AudioManager
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import com.google.android.material.slider.Slider

class VolumeSettingsActivity : AppCompatActivity() {

    private lateinit var audioManager: AudioManager

    private data class StreamRow(val stream: Int, val sliderId: Int, val levelId: Int)

    private val rows = listOf(
        StreamRow(AudioManager.STREAM_MUSIC,        R.id.sliderMedia,        R.id.tvMediaLevel),
        StreamRow(AudioManager.STREAM_RING,         R.id.sliderRing,         R.id.tvRingLevel),
        StreamRow(AudioManager.STREAM_ALARM,        R.id.sliderAlarm,        R.id.tvAlarmLevel),
        StreamRow(AudioManager.STREAM_NOTIFICATION, R.id.sliderNotification, R.id.tvNotificationLevel),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.activity_volume_settings)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        for (row in rows) {
            val slider = findViewById<Slider>(row.sliderId)
            val tvLevel = findViewById<TextView>(row.levelId)

            val max = audioManager.getStreamMaxVolume(row.stream).coerceAtLeast(1)
            val current = audioManager.getStreamVolume(row.stream).toFloat().coerceIn(0f, max.toFloat())

            slider.valueTo = max.toFloat()
            slider.value = current
            tvLevel.text = current.toInt().toString()

            slider.addOnChangeListener { _, value, fromUser ->
                tvLevel.text = value.toInt().toString()
                if (fromUser) {
                    audioManager.setStreamVolume(row.stream, value.toInt(), 0)
                }
            }
        }
    }
}
