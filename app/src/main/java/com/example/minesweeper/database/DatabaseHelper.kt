package com.example.minesweeper.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "minesweeper.db"
        const val DATABASE_VERSION = 1

        // User table
        const val TABLE_USER = "user"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD_HASH = "password_hash"

        // Game Setting table
        const val TABLE_GAME_SETTING = "game_setting"
        const val COLUMN_SETTING_ID = "setting_id"
        const val COLUMN_VOLUME = "volume"
        const val COLUMN_BRIGHTNESS = "brightness"
        const val COLUMN_MUSIC_ENABLED = "music_enabled"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD_HASH TEXT
            )
        """

        val createGameSettingTable = """
            CREATE TABLE $TABLE_GAME_SETTING (
                $COLUMN_SETTING_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER,
                $COLUMN_VOLUME REAL,
                $COLUMN_BRIGHTNESS REAL,
                $COLUMN_MUSIC_ENABLED INTEGER,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USER($COLUMN_USER_ID)
            )
        """

        db.execSQL(createUserTable)
        db.execSQL(createGameSettingTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAME_SETTING")
        onCreate(db)
    }

    // Insert a new user and return the ID of the created user
    fun insertUser(username: String, passwordHash: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD_HASH, passwordHash)
        }
        return db.insert(TABLE_USER, null, values)
    }

    // Authenticate user and return user ID if successful, or -1 otherwise
    fun authenticateUser(username: String, passwordHash: String): Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT $COLUMN_USER_ID FROM $TABLE_USER WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD_HASH = ?",
            arrayOf(username, passwordHash)
        )
        val userId = if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)) else -1
        cursor.close()
        return userId
    }

    // Insert game setting for a specific user
    fun insertSetting(userId: Int, volume: Float, brightness: Float, musicEnabled: Boolean): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_VOLUME, volume)
            put(COLUMN_BRIGHTNESS, brightness)
            put(COLUMN_MUSIC_ENABLED, if (musicEnabled) 1 else 0)
        }
        return db.insert(TABLE_GAME_SETTING, null, values)
    }

    // Get game setting for a specific user
    fun getSettingForUser(userId: Int): GameSetting? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_GAME_SETTING WHERE $COLUMN_USER_ID = ?",
            arrayOf(userId.toString())
        )
        var setting: GameSetting? = null
        if (cursor.moveToFirst()) {
            setting = GameSetting(
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                volume = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_VOLUME)),
                brightness = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_BRIGHTNESS)),
                musicEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MUSIC_ENABLED)) == 1
            )
        }
        cursor.close()
        return setting
    }

    // Update game setting for a specific user
    fun updateSetting(userId: Int, volume: Float, brightness: Float, musicEnabled: Boolean): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_VOLUME, volume)
            put(COLUMN_BRIGHTNESS, brightness)
            put(COLUMN_MUSIC_ENABLED, if (musicEnabled) 1 else 0)
        }
        return db.update(TABLE_GAME_SETTING, values, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
    }
}

// Data class for GameSetting to represent a user's settings
data class GameSetting(
    val userId: Int,
    val volume: Float,
    val brightness: Float,
    val musicEnabled: Boolean
)

// Data class for User (optional if you need to retrieve full user data)
data class User(
    val userId: Int,
    val username: String,
    val passwordHash: String
)
