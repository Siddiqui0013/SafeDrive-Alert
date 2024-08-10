package com.siddiqui.safedrivealert

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.siddiqui.safedrivealert.ui.main.SettingsManager

class SafeDriveAlertApp : Application() {
    override fun onCreate() {
        super.onCreate()
//        SettingsManager.clearPreferences(this)
        val theme = SettingsManager.getSelectedTheme(this)
        AppCompatDelegate.setDefaultNightMode(theme)
    }
}