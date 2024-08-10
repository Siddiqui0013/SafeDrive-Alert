package com.siddiqui.safedrivealert

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_info)

        val m1Btn: Button = findViewById(R.id.module_drowsiness)
        val m3Btn: Button = findViewById(R.id.module_lane)
        val drowsyStartBtn: Button = findViewById(R.id.DrowsyBtn)
        val laneStartBtn: Button = findViewById(R.id.LaneBtn)

        val m1Info: TextView = findViewById(R.id.info_drowsiness)
        val m3Info: TextView = findViewById(R.id.info_lane)

        m1Btn.setOnClickListener { toggleVisibility(m1Info) }
        m3Btn.setOnClickListener { toggleVisibility(m3Info) }

        // Set click listeners to start the activities
        drowsyStartBtn.setOnClickListener {
            val intent = Intent(this, DetectionActivity::class.java)
            startActivity(intent)
        }

        laneStartBtn.setOnClickListener {
            val intent = Intent(this, LaneActivity::class.java)
            startActivity(intent)
        }
    }

    private fun toggleVisibility(textView: TextView) {
        if (textView.visibility == View.GONE) {
            textView.visibility = View.VISIBLE
            expand(textView)
        } else {
            textView.visibility = View.GONE
            collapse(textView)
        }
    }

    private fun expand(textView: TextView) {
        textView.measure(
            View.MeasureSpec.makeMeasureSpec(textView.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.UNSPECIFIED
        )
        val targetHeight = textView.measuredHeight

        textView.visibility = View.VISIBLE
        textView.alpha = 0.0f
        textView.translationY = -targetHeight.toFloat()

        textView.animate()
            .alpha(1.0f)
            .translationY(0f)
            .setDuration(300L)
            .setListener(null)
            .start()
    }

    private fun collapse(textView: TextView) {
        textView.alpha = 1.0f
        textView.translationY = 0f

        textView.animate()
            .alpha(0.0f)
            .translationY(-textView.height.toFloat())
            .setDuration(300L)
            .withEndAction { textView.visibility = View.GONE }
            .start()
    }
}
