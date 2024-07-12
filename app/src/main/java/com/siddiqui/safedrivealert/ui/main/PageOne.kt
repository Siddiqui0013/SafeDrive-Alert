package com.siddiqui.safedrivealert.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.siddiqui.safedrivealert.DetectionActivity
import com.siddiqui.safedrivealert.R

class PageOne : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                          savedInstanceState: Bundle?):View
    {
        val view = inflater.inflate(R.layout.page_one, container, false)
        val detectionBtn:Button = view.findViewById(R.id.startDetectionButton)
        detectionBtn.setOnClickListener {
            val intent = Intent(activity, DetectionActivity::class.java)
            startActivity(intent)
        }


        return view
    }
}