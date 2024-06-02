package com.siddiqui.safedrivealert

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.siddiqui.safedrivealert.databinding.ActivityCameraBinding


class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startDetectionButton.setOnClickListener {
            val intent = Intent(this, DetectionActivity::class.java)
            startActivity(intent)
        }
    }
}
