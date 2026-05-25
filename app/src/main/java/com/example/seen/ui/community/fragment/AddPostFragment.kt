package com.example.seen.ui.community.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.seen.databinding.FragmentAddPostBinding


class AddPostFragment : Fragment() {

    var _binding: FragmentAddPostBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}