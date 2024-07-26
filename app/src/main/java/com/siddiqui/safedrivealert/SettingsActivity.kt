package com.siddiqui.safedrivealert

import SettingsAdapter
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siddiqui.safedrivealert.ui.main.SettingsManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsList = listOf("Select Theme", "Set Speed Limit", "Change Alert Sound")
        val recyclerView: RecyclerView = findViewById(R.id.settings_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SettingsAdapter(settingsList) { setting ->
            when (setting) {
                "Select Theme" -> showSelectThemeDialog()
                "Set Speed Limit" -> showSetSpeedLimitDialog()
                "Change Alert Sound" -> showChangeAlertSoundDialog()
            }
        }
    }

    private fun showSelectThemeDialog() {
        val themes = arrayOf("Dark", "Light", "System Default")
        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, -1) { dialog, which ->
                // Handle theme selection
            }
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showSetSpeedLimitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_speed_limit, null)
        val speedLimitInput = dialogView.findViewById<EditText>(R.id.speed_limit_input)

        AlertDialog.Builder(this)
            .setTitle("Set Speed Limit")
            .setView(dialogView)
            .setPositiveButton("Confirm") { dialog, _ ->
                val speedLimitText = speedLimitInput.text.toString()
                if (speedLimitText.isNotEmpty()) {
                    val speedLimit = speedLimitText.toInt()
                    SettingsManager.setSpeedLimit(this, speedLimit)
                    Toast.makeText(this, "Speed limit set to $speedLimit", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid speed limit", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    private fun showChangeAlertSoundDialog() {
        val sounds = arrayOf("Sound 1", "Sound 2", "Sound 3")
        AlertDialog.Builder(this)
            .setTitle("Change Alert Sound")
            .setSingleChoiceItems(sounds, -1) { dialog, which ->
                // Handle sound selection
            }
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
