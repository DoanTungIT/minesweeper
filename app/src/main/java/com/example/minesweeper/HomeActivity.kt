package com.example.minesweeper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.minesweeper.database.DatabaseHelper

class HomeActivity : AppCompatActivity() {

    private lateinit var playButton: Button
    private lateinit var settingButton: Button
    private lateinit var scoreboardButton: Button
    private lateinit var logoutButton: Button
    private lateinit var dbHelper: DatabaseHelper
    private val userId = 1 // Giả định ID người dùng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dbHelper = DatabaseHelper(this)
        playButton = findViewById(R.id.playButton)
        settingButton = findViewById(R.id.settingButton)
        scoreboardButton = findViewById(R.id.scoreboardButton)
        logoutButton = findViewById(R.id.logoutButton)

        playButton.setOnClickListener {
            // TODO: Start Play Activity (Not implemented yet)
        }

        settingButton.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        scoreboardButton.setOnClickListener {
            startActivity(Intent(this, ScoreboardActivity::class.java))
        }

        logoutButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val isMusicOn = sharedPreferences.getBoolean("isMusicOn", true)

        if (isMusicOn) {
            val intent = Intent(this, BackgroundMusicService::class.java)
            startService(intent)
        }
        applyBrightnessSetting()
    }


    private fun applyBrightnessSetting() {
        val settings = dbHelper.getSettingForUser(userId)
        settings?.let {
            val layoutParams = window.attributes
            layoutParams.screenBrightness = it.brightness // Áp dụng độ sáng từ cơ sở dữ liệu
            window.attributes = layoutParams
        }
    }
}
