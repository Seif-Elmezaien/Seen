package com.example.seen.ui.home.fragment

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.seen.R
import com.example.seen.databinding.FragmentHomeEntryBinding


class HomeEntryFragment : Fragment() {
    var _binding: FragmentHomeEntryBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(checkOnBoarding()){
            findNavController().navigate(R.id.action_homeEntryFragment_to_homeFragment)
        }

        binding.btnStartNow.setOnClickListener {
            findNavController().navigate(R.id.action_homeEntryFragment_to_onboardingContainerFragment)
        }
    }

    private fun  checkOnBoarding(): Boolean {
        val sharedPref = requireContext().getSharedPreferences("onBoarding", MODE_PRIVATE)
        return sharedPref.getBoolean("onBoardingFinished", false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}