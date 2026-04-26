package com.example.seen.ui.authentication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.seen.R
import com.example.seen.databinding.FragmentQuestionnaire3Binding
import com.example.seen.ui.activites.AuthActivity
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import kotlin.getValue

class Questionnaire3Fragment : Fragment() {
    private var _binding: FragmentQuestionnaire3Binding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionnaire3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as AuthActivity).viewModel

        binding.btnPen.setOnClickListener {
            selectType("Syringes", binding.btnPen)
        }

        binding.btnPump.setOnClickListener {
            selectType("pump", binding.btnPump)
        }

        binding.btnNoInsulin.setOnClickListener {
            selectType("No insulin", binding.btnNoInsulin)
        }

        binding.btnBack.setOnClickListener {
            (parentFragment as QuestionnaireContainerFragment)
                .goToPreviousPage()
        }

        binding.btnNext.setOnClickListener {
            if (validInputs()){
                (parentFragment as QuestionnaireContainerFragment)
                    .goToNextPage()
            }
        }
    }

    private fun validInputs(): Boolean {
        if(viewModel.signUpData.insulin_therapy == null){
            Toast.makeText(requireContext(), getString(R.string.error_enter_insulin_therapy), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun selectType(insulinTherapy: String, selectedButton: MaterialButton) {

        // reset all buttons
        val buttons = listOf(
            binding.btnPen,
            binding.btnPump,
            binding.btnNoInsulin,
        )

        if (viewModel.signUpData.insulin_therapy == insulinTherapy){
            buttons.forEach {
                it.setBackgroundColor(requireContext().getColor(R.color.secondary))
                viewModel.signUpData.insulin_therapy = null
            }
        }
        else {
            viewModel.signUpData.insulin_therapy = insulinTherapy

            buttons.forEach {
                it.setBackgroundColor(requireContext().getColor(R.color.secondary))
            }

            // highlight selected
            selectedButton.setBackgroundColor(requireContext().getColor(R.color.primary))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}