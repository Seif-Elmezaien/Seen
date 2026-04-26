package com.example.seen.ui.authentication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.seen.R
import com.example.seen.databinding.FragmentQuestionnaire1Binding
import com.example.seen.databinding.FragmentQuestionnaire2Binding
import com.example.seen.ui.activites.AuthActivity
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import kotlin.getValue

class Questionnaire2Fragment : Fragment() {
    private var _binding: FragmentQuestionnaire2Binding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionnaire2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as AuthActivity).viewModel

        binding.btnType1.setOnClickListener {
            selectType("Type1", binding.btnType1)
        }

        binding.btnType2.setOnClickListener {
            selectType("Type2", binding.btnType2)
        }

        binding.btnLada.setOnClickListener {
            selectType("LADA", binding.btnLada)
        }

        binding.btnMody.setOnClickListener {
            selectType("MODY", binding.btnMody)
        }

        binding.btnGestational.setOnClickListener {
            selectType("Gestational", binding.btnGestational)
        }

        binding.btnOther.setOnClickListener {
            selectType("other", binding.btnOther)
        }

        binding.btnBack.setOnClickListener {
            (parentFragment as QuestionnaireContainerFragment)
                .goToPreviousPage()
        }

        binding.btnNext.setOnClickListener {
            if(validInputs()){
                (parentFragment as QuestionnaireContainerFragment)
                    .goToNextPage()
            }
        }
    }

    private fun validInputs(): Boolean {
        if(viewModel.signUpData.diabetes_type == null){
            Toast.makeText(requireContext(), getString(R.string.error_enter_type), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun selectType(diabetesType: String, selectedButton: MaterialButton) {

        // reset all buttons
        val buttons = listOf(
            binding.btnType1,
            binding.btnType2,
            binding.btnLada,
            binding.btnMody,
            binding.btnGestational,
            binding.btnOther
        )

        if (viewModel.signUpData.diabetes_type == diabetesType){
            buttons.forEach {
                it.setBackgroundColor(requireContext().getColor(R.color.secondary))
                viewModel.signUpData.diabetes_type = null
            }
        }
        else {
            viewModel.signUpData.diabetes_type = diabetesType

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