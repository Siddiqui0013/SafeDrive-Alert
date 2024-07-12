package com.siddiqui.safedrivealert.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.siddiqui.safedrivealert.LaneActivity
import com.siddiqui.safedrivealert.R

class PageTwo : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.page_two, container, false)

        val detectionBtn: Button = view.findViewById(R.id.startLaneDetection)
        detectionBtn.setOnClickListener {
            val intent = Intent(activity, LaneActivity::class.java)
            startActivity(intent)
        }

        val animationView: LottieAnimationView = view.findViewById(R.id.animationViewTwo)
        // You can set additional properties on the animation view here if needed

        return view
    }
}
