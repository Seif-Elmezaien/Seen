package com.example.seen.ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.seen.ui.home.fragment.Onboarding1Fragment
import com.example.seen.ui.home.fragment.Onboarding2Fragment
import com.example.seen.ui.home.fragment.Onboarding3Fragment

class OnBoardingContainerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        private val slides = listOf(
            Onboarding1Fragment(),
            Onboarding2Fragment(),
            Onboarding3Fragment()
        )

    override fun createFragment(position: Int): Fragment {
        return slides[position]
    }

    override fun getItemCount(): Int {
        return slides.size
    }
}