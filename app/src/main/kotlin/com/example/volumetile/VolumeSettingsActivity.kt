package com.example.volumetile

import android.media.AudioManager
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.DynamicColors
import com.google.android.material.slider.Slider

class VolumeSettingsActivity : AppCompatActivity() {

    private lateinit var audioManager: AudioManager

    private data class StreamRow(
        val stream: Int,
        val sliderId: Int,
        val levelId: Int,
        val iconId: Int,
        val iconRes: Int,
    )

    private val rows = listOf(
        StreamRow(AudioManager.STREAM_MUSIC,        R.id.sliderMedia,        R.id.tvMediaLevel,        R.id.iconMedia,        R.drawable.ic_stream_media),
        StreamRow(AudioManager.STREAM_RING,         R.id.sliderRing,         R.id.tvRingLevel,         R.id.iconRing,         R.drawable.ic_stream_ring),
        StreamRow(AudioManager.STREAM_ALARM,        R.id.sliderAlarm,        R.id.tvAlarmLevel,        R.id.iconAlarm,        R.drawable.ic_stream_alarm),
        StreamRow(AudioManager.STREAM_NOTIFICATION, R.id.sliderNotification, R.id.tvNotificationLevel, R.id.iconNotification, R.drawable.ic_stream_notification),
    )

    // Maps stream -> volume saved before muting; presence in map means stream is muted.
    private val savedVolumes = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.activity_volume_settings)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        for (row in rows) {
            val slider = findViewById<Slider>(row.sliderId)
            val tvLevel = findViewById<TextView>(row.levelId)
            val icon = findViewById<ImageView>(row.iconId)

            val max = audioManager.getStreamMaxVolume(row.stream).coerceAtLeast(1)
            val current = audioManager.getStreamVolume(row.stream).toFloat().coerceIn(0f, max.toFloat())

            slider.valueTo = max.toFloat()
            slider.value = current
            tvLevel.text = current.toInt().toString()

            slider.addOnChangeListener { _, value, fromUser ->
                tvLevel.text = value.toInt().toString()
                if (fromUser) {
                    audioManager.setStreamVolume(row.stream, value.toInt(), 0)
                    // User manually raised volume — clear muted state
                    if (value > 0 && savedVolumes.containsKey(row.stream)) {
                        savedVolumes.remove(row.stream)
                        icon.setImageResource(row.iconRes)
                        icon.alpha = 1f
                    }
                }
            }

            icon.setOnClickListener {
                toggleMute(row, slider, tvLevel, icon)
            }
        }

        findViewById<MaterialButton>(R.id.btnSoundSettings).setOnClickListener {
            audioManager.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
        }
    }

    private fun toggleMute(row: StreamRow, slider: Slider, tvLevel: TextView, icon: ImageView) {
        if (savedVolumes.containsKey(row.stream)) {
            // Unmute: restore saved volume
            val restored = savedVolumes.remove(row.stream)!!
            audioManager.setStreamVolume(row.stream, restored, 0)
            slider.value = restored.toFloat()
            tvLevel.text = restored.toString()
            icon.setImageResource(row.iconRes)
            icon.alpha = 1f
        } else {
            // Mute: save current volume and silence the stream
            val current = audioManager.getStreamVolume(row.stream)
            savedVolumes[row.stream] = current
            audioManager.setStreamVolume(row.stream, 0, 0)
            slider.value = 0f
            tvLevel.text = "0"
            icon.setImageResource(R.drawable.ic_volume_off)
            icon.alpha = 0.5f
        }
    }
}
