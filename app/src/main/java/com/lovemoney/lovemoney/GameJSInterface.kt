package com.lovemoney.lovemoney

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONObject
import com.lovemoney.lovemoney.utils.Logger

class GameJSInterface(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("LoveMoneyGameData", Context.MODE_PRIVATE)

    @JavascriptInterface
    fun vibrate(duration: Long = 50) {
        Logger.jsInterface("Vibrate called with duration: ${duration}ms")
        try {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
            Logger.jsInterface("Vibration triggered successfully")
        } catch (e: Exception) {
            Logger.logException("GameJSInterface", "Error triggering vibration", e)
        }
    }

    @JavascriptInterface
    fun vibratePattern(pattern: String) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val patternArray = pattern.split(",").map { it.toLong() }.toLongArray()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(patternArray, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(patternArray, -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun saveData(key: String, value: String) {
        Logger.jsInterface("Save data called: key=$key, value length=${value.length}")
        try {
            sharedPrefs.edit().putString(key, value).apply()
            Logger.jsInterface("Data saved successfully for key: $key")
        } catch (e: Exception) {
            Logger.logException("GameJSInterface", "Error saving data for key: $key", e)
        }
    }

    @JavascriptInterface
    fun loadData(key: String): String? {
        Logger.jsInterface("Load data called for key: $key")
        try {
            val value = sharedPrefs.getString(key, null)
            Logger.jsInterface("Data loaded for key: $key, value length=${value?.length ?: 0}")
            return value
        } catch (e: Exception) {
            Logger.logException("GameJSInterface", "Error loading data for key: $key", e)
            return null
        }
    }

    @JavascriptInterface
    fun removeData(key: String) {
        sharedPrefs.edit().remove(key).apply()
    }

    @JavascriptInterface
    fun clearAllData() {
        sharedPrefs.edit().clear().apply()
    }

    @JavascriptInterface
    fun shareGame(text: String, url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$text\n$url")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Game"))
    }

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun getDeviceInfo(): String {
        val info = JSONObject()
        info.put("brand", Build.BRAND)
        info.put("model", Build.MODEL)
        info.put("androidVersion", Build.VERSION.RELEASE)
        info.put("sdkVersion", Build.VERSION.SDK_INT)
        info.put("appVersion", "1.0.0")
        return info.toString()
    }

    @JavascriptInterface
    fun handleGameEvent(eventJson: String) {
        Logger.jsInterface("Game event received: $eventJson")
        try {
            val event = JSONObject(eventJson)
            val type = event.getString("type")
            Logger.jsInterface("Processing game event type: $type")

            when (type) {
                "achievement" -> {
                    val achievement = event.getString("achievement")
                    Logger.jsInterface("Achievement unlocked: $achievement")
                    // Handle achievement unlock
                    saveAchievement(achievement)
                    vibrate(100)
                }
                "levelComplete" -> {
                    val level = event.getInt("level")
                    val score = event.getInt("score")
                    Logger.jsInterface("Level complete: level=$level, score=$score")
                    // Save level progress
                    saveLevelProgress(level, score)
                }
                "gameOver" -> {
                    val finalScore = event.getInt("score")
                    Logger.jsInterface("Game over with score: $finalScore")
                    // Handle game over
                    saveHighScore(finalScore)
                }
                "soundEffect" -> {
                    Logger.jsInterface("Sound effect triggered, providing haptic feedback")
                    // Trigger haptic feedback
                    vibrate(30)
                }
                else -> {
                    Logger.jsInterface("Unknown game event type: $type")
                }
            }
        } catch (e: Exception) {
            Logger.logException("GameJSInterface", "Error handling game event", e)
        }
    }

    @JavascriptInterface
    fun requestFullscreen() {
        if (context is GameActivity) {
            context.runOnUiThread {
                context.window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }
        }
    }

    @JavascriptInterface
    fun exitFullscreen() {
        if (context is GameActivity) {
            context.runOnUiThread {
                context.window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    @JavascriptInterface
    fun getHighScore(): Int {
        return sharedPrefs.getInt("high_score", 0)
    }

    @JavascriptInterface
    fun getSavedLevel(): Int {
        return sharedPrefs.getInt("saved_level", 1)
    }

    @JavascriptInterface
    fun isFirstTime(): Boolean {
        val isFirst = sharedPrefs.getBoolean("first_time", true)
        if (isFirst) {
            sharedPrefs.edit().putBoolean("first_time", false).apply()
        }
        return isFirst
    }

    private fun saveAchievement(achievement: String) {
        val achievements = sharedPrefs.getStringSet("achievements", mutableSetOf()) ?: mutableSetOf()
        achievements.add(achievement)
        sharedPrefs.edit().putStringSet("achievements", achievements).apply()
    }

    private fun saveLevelProgress(level: Int, score: Int) {
        sharedPrefs.edit()
            .putInt("saved_level", level)
            .putInt("level_${level}_score", score)
            .apply()
    }

    private fun saveHighScore(score: Int) {
        val currentHighScore = sharedPrefs.getInt("high_score", 0)
        if (score > currentHighScore) {
            sharedPrefs.edit().putInt("high_score", score).apply()
        }
    }
}