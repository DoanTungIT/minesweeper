package com.example.minesweeper

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.minesweeper.database.DatabaseHelper

class SettingActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var brightnessSeekBar: SeekBar
    private lateinit var musicSpinner: Spinner
    private lateinit var saveButton: Button
    private var isMusicOn = true
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        dbHelper = DatabaseHelper(this)

        // Lấy userId từ SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)

        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar)
        musicSpinner = findViewById(R.id.musicSpinner)
        saveButton = findViewById(R.id.saveButton)

        setupMusicSpinner()
        setupVolumeControl()
        setupBrightnessControl()
        loadSettingsFromDatabase()

        saveButton.setOnClickListener {
            saveSettingsToDatabase()
            applySettingsImmediately()
            Toast.makeText(this, "Settings applied successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMusicSpinner() {
        val options = arrayOf("On", "Off")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        musicSpinner.adapter = adapter

        musicSpinner.setSelection(if (isMusicOn) 0 else 1)

        musicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                isMusicOn = (position == 0)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupVolumeControl() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.max = maxVolume
        volumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupBrightnessControl() {
        brightnessSeekBar.max = 255
        brightnessSeekBar.progress = (window.attributes.screenBrightness * 255).toInt()

        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val layoutParams = window.attributes
                layoutParams.screenBrightness = progress / 255f
                window.attributes = layoutParams
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadSettingsFromDatabase() {
        val settings = dbHelper.getSettingForUser(userId)
        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        settings?.let {
            brightnessSeekBar.progress = (it.brightness * 255).toInt()
            volumeSeekBar.progress = (it.volume * volumeSeekBar.max).toInt()
            isMusicOn = it.musicEnabled
            musicSpinner.setSelection(if (isMusicOn) 0 else 1)

            with(sharedPreferences.edit()) {
                putFloat("volume", it.volume)
                putFloat("brightness", it.brightness)
                putBoolean("musicEnabled", it.musicEnabled)
                apply()
            }
        }
    }

    private fun saveSettingsToDatabase() {
        val brightness = brightnessSeekBar.progress / 255f
        val volume = volumeSeekBar.progress / volumeSeekBar.max.toFloat()

        if (dbHelper.getSettingForUser(userId) == null) {
            dbHelper.insertSetting(userId, volume, brightness, isMusicOn)
        } else {
            dbHelper.updateSetting(userId, volume, brightness, isMusicOn)
        }

        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("volume", volume)
            putFloat("brightness", brightness)
            putBoolean("musicEnabled", isMusicOn)
            apply()
        }
    }

    private fun applySettingsImmediately() {
        // Áp dụng độ sáng ngay lập tức
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightnessSeekBar.progress / 255f
        window.attributes = layoutParams

        // Cập nhật trạng thái nhạc
        if (isMusicOn) {
            startService(Intent(this, BackgroundMusicService::class.java).apply {
                action = "RESUME_MUSIC"
            })
        } else {
            startService(Intent(this, BackgroundMusicService::class.java).apply {
                action = "PAUSE_MUSIC"
            })
        }
    }
}
