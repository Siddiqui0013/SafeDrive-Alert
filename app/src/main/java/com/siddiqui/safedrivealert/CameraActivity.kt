package com.siddiqui.safedrivealert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.siddiqui.safedrivealert.databinding.ActivityCameraBinding
import com.siddiqui.safedrivealert.ui.main.PageOne
import com.siddiqui.safedrivealert.ui.main.PageTwo
import androidx.viewpager2.adapter.FragmentStateAdapter

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val fragments = arrayListOf<Fragment>(
            PageOne(),
            PageTwo()
        )
        val adapter = ViewPagerAdapter(this, fragments)
        viewPager.adapter = adapter

        // Connect the TabLayout with the ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // You can set custom text or icons here if needed
        }.attach()
    }

    private inner class ViewPagerAdapter(
        activity: AppCompatActivity, val fragments: ArrayList<Fragment>
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}
