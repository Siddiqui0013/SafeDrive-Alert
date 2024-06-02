package com.siddiqui.safedrivealert

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val infoButton:ImageButton = findViewById(R.id.button_info)
        val cameraButton:ImageButton = findViewById(R.id.button_camera)
        val settingsButton:ImageButton = findViewById(R.id.button_settings)

        val textAnim:TextView = findViewById(R.id.animated_text)

        val fadein = AnimationUtils.loadAnimation(this,R.anim.fade_in)
        textAnim.startAnimation(fadein)

        infoButton.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        cameraButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)

    }
}
}