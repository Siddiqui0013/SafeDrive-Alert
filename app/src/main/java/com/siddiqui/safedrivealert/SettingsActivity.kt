package com.siddiqui.safedrivealert

import SettingsAdapter
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siddiqui.safedrivealert.ui.main.SettingsManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsList = listOf("Select Theme", "Set Speed Limit", "Change Face Overlay Color")
        val recyclerView: RecyclerView = findViewById(R.id.settings_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SettingsAdapter(settingsList) { setting ->
            when (setting) {
                "Select Theme" -> showSelectThemeDialog()
                "Set Speed Limit" -> showSetSpeedLimitDialog()
                "Change Face Overlay Color" -> showChangeOverlayColorDialog()
            }
        }
    }

    private fun showSelectThemeDialog() {

        val currentTheme = SettingsManager.getSelectedTheme(this)
        val checkedItem = when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_YES -> 0
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            else -> 2
        }

        val themes = arrayOf("Dark", "Light", "System Default")
        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selectedTheme = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_YES
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                SettingsManager.setTheme(this, selectedTheme)
                AppCompatDelegate.setDefaultNightMode(selectedTheme)
                recreate()
                dialog.dismiss()
            }
            .show()
    }

    private fun showSetSpeedLimitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_speed_limit, null)
        val speedLimitInput = dialogView.findViewById<EditText>(R.id.speed_limit_input)
        speedLimitInput.hint = SettingsManager.getSpeedLimit(this).toString()

        AlertDialog.Builder(this)
            .setTitle("Set Speed Limit (From 40 to 100)")
            .setView(dialogView)
            .setPositiveButton("Confirm") { dialog, _ ->
                val speedLimitText = speedLimitInput.text.toString()
                if (speedLimitText.isNotEmpty()) {
                    val speedLimit = speedLimitText.toInt()
                    if (speedLimit in 40..100) {
                        SettingsManager.setSpeedLimit(this, speedLimit)
                        Toast.makeText(this, "Speed limit set to $speedLimit", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please enter a valid speed limit between 40 and 100", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter a valid speed limit", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    private fun showChangeOverlayColorDialog() {

        val currentColor = SettingsManager.getOverlayColor(this)
        val checkedItem = when (currentColor) {
            Color.WHITE -> 0
            Color.BLACK -> 1
            Color.RED -> 2
            Color.GREEN -> 3
            else -> 4
        }

        val colors = arrayOf("White", "Black", "Red", "Green", "No Overlay")
        AlertDialog.Builder(this)
            .setTitle("Change Overlay Color")
            .setSingleChoiceItems(colors, checkedItem) { dialog, which ->
                val selectedColor = when (which) {
                    0 -> Color.WHITE
                    1 -> Color.BLACK
                    2 -> Color.RED
                    3 -> Color.GREEN
                    else -> Color.TRANSPARENT
                }
                SettingsManager.setOverlayColor(this, selectedColor)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

}
