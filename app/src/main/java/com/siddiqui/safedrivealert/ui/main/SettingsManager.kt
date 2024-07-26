package com.siddiqui.safedrivealert.ui.main

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "app_settings"
    private const val THEME_KEY = "theme"
    private const val SPEED_LIMIT_KEY = "speed_limit"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setTheme(context: Context, isDarkMode: Boolean) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(THEME_KEY, isDarkMode)
        editor.apply()
    }

    fun isDarkMode(context: Context): Boolean {
        return getPreferences(context).getBoolean(THEME_KEY, false)
    }

    fun setSpeedLimit(context: Context, speedLimit: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(SPEED_LIMIT_KEY, speedLimit)
        editor.apply()
    }

    fun getSpeedLimit(context: Context): Int {
        return getPreferences(context).getInt(SPEED_LIMIT_KEY, 0) // Default to 0 if not set
    }
}
