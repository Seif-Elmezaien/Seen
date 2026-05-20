package com.example.seen.ui.home.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.seen.R
import com.example.seen.databinding.FragmentCommunityBinding
import com.example.seen.databinding.FragmentCommunitySearchBinding
import com.example.seen.databinding.FragmentDisplayLogsBinding


class DisplayLogsFragment : Fragment() {

    var _binding: FragmentDisplayLogsBinding? = null
    val binding get() = _binding!!

    private val args: DisplayLogsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDisplayLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}