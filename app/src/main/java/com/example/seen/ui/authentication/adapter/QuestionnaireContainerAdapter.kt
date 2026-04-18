package com.example.seen.ui.authentication.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.seen.ui.authentication.fragments.Questionnaire1Fragment
import com.example.seen.ui.authentication.fragments.Questionnaire2Fragment
import com.example.seen.ui.authentication.fragments.Questionnaire3Fragment

class QuestionnaireContainerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        private val slides = listOf(
            Questionnaire1Fragment(),
            Questionnaire2Fragment(),
            Questionnaire3Fragment()
        )

    override fun createFragment(position: Int): Fragment {
        return slides[position]
    }

    override fun getItemCount(): Int {
        return slides.size
    }
}