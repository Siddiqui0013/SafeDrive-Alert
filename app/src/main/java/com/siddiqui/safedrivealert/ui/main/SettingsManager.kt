package com.siddiqui.safedrivealert.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatDelegate

object SettingsManager {
    private const val PREFS_NAME = "app_settings"
    private const val THEME_KEY = "theme"
    private const val SPEED_LIMIT_KEY = "speed_limit"
    private const val OVERLAY_COLOR_KEY = "overlay_color"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setTheme(context: Context, mode: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(THEME_KEY, mode)
        editor.apply()
    }
    fun getSelectedTheme(context: Context): Int {
        return getPreferences(context).getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun setSpeedLimit(context: Context, speedLimit: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(SPEED_LIMIT_KEY, speedLimit)
        editor.apply()
    }
    fun getSpeedLimit(context: Context): Int {
        return getPreferences(context).getInt(SPEED_LIMIT_KEY, 0)
    }

    fun setOverlayColor(context: Context, color: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(OVERLAY_COLOR_KEY, color)
        editor.apply()
    }
    fun getOverlayColor(context: Context): Int {
        return getPreferences(context).getInt(OVERLAY_COLOR_KEY, Color.TRANSPARENT)
    }
}
