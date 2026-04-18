package com.example.seen.ui.home.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.seen.R
import com.example.seen.databinding.FragmentOnboardingContainerBinding
import com.example.seen.ui.home.adapter.OnBoardingContainerAdapter

class OnboardingContainerFragment : Fragment() {
    private var _binding: FragmentOnboardingContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = OnBoardingContainerAdapter(this)
        binding.vpOnBoarding.adapter = adapter

        // Link CircleIndicator to ViewPager2
        binding.ciOnBoarding.setViewPager(binding.vpOnBoarding)

        binding.tvskip.setOnClickListener {
            onBoardingFinished()
            findNavController().navigate(R.id.action_onboardingContainerFragment_to_homeFragment)
        }

        binding.btnGoNext.setOnClickListener {
            if (binding.vpOnBoarding.currentItem < adapter.itemCount - 1) {
                binding.vpOnBoarding.currentItem += 1
            } else {
                onBoardingFinished()
                findNavController().navigate(R.id.action_onboardingContainerFragment_to_homeFragment)
            }
        }

        binding.btnGoBack.setOnClickListener {
            if (binding.vpOnBoarding.currentItem > 0) {
                binding.vpOnBoarding.currentItem -= 1
            }

        }
    }

    private fun onBoardingFinished() {
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("onBoardingFinished", true)
        editor.apply()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}