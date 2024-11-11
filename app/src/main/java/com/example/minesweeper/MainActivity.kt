package com.example.minesweeper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.minesweeper.database.DatabaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val userId = dbHelper.authenticateUser(username, password)
            if (userId != -1) {
                // Lưu userId vào SharedPreferences
                val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putInt("userId", userId)
                    apply()
                }

                // Tải và áp dụng cài đặt của người dùng
                applyUserSettings(userId)

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun applyUserSettings(userId: Int) {
        val settings = dbHelper.getSettingForUser(userId)
        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        settings?.let {
            with(sharedPreferences.edit()) {
                putFloat("volume", it.volume)
                putFloat("brightness", it.brightness)
                putBoolean("musicEnabled", it.musicEnabled)
                apply()
            }

            // Áp dụng trạng thái nhạc ngay lập tức
            if (it.musicEnabled) {
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

    override fun onResume() {
        super.onResume()
        // Tắt nhạc nền ở màn hình đăng nhập
        stopService(Intent(this, BackgroundMusicService::class.java))
    }
}
