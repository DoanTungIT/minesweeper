package com.example.minesweeper

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.minesweeper.database.DatabaseHelper

class ScoreboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val userId = 1 // Giả định ID người dùng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoreboard)

        dbHelper = DatabaseHelper(this)
        val scoreboardTextView: TextView = findViewById(R.id.scoreboardTextView)

        // Fake data for demo
        val fakeScores = """
            Username     | Difficulty | Time | Date
            -------------------------------------------------
            Player1      | Easy       | 45s  | 2024-11-06
            Player2      | Medium     | 60s  | 2024-11-06
            Player3      | Hard       | 75s  | 2024-11-06
        """.trimIndent()

        scoreboardTextView.text = fakeScores
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
