package com.siddiqui.safedrivealert

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.siddiqui.safedrivealert.ui.main.SettingsManager

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val infoButton:Button = findViewById(R.id.button_info)
        val cameraButton:Button = findViewById(R.id.button_camera)
        val settingsButton:Button = findViewById(R.id.button_settings)

//        val textAnim:TextView = findViewById(R.id.animated_text)

//        val animation: LottieAnimationView = findViewById(R.id.lottie_animation)

//        val fadeIn = AnimationUtils.loadAnimation(this,R.anim.fade_in)
//        val fadeOut = AnimationUtils.loadAnimation(this,R.anim.fade_out)

//"        textAnim.startAnimation(fadeIn)
//
//        Handler().postDelayed({
//            textAnim.startAnimation(fadeOut)
//            textAnim.visibility = TextView.GONE
//"        }, 2000)

//        Handler().postDelayed({
//            animation.visibility = LottieAnimationView.VISIBLE
//        }, 5000)

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

//        val speedLimit = SettingsManager.getSpeedLimit(this)
//        Toast.makeText(this, "Current speed limit: $speedLimit", Toast.LENGTH_LONG).show()


    }
}