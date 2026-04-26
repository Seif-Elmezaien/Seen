package com.example.seen.ui.authentication.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.example.seen.R
import com.example.seen.databinding.FragmentQuestionnaire1Binding
import com.example.seen.ui.activites.AuthActivity
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import java.util.Calendar
import java.util.Date
import kotlin.getValue

class Questionnaire1Fragment : Fragment() {
    private var _binding: FragmentQuestionnaire1Binding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionnaire1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as AuthActivity).viewModel

        retriveDataIfExists()

        clearErrorOnFocus(
            binding.etWeight,
            binding.etHeight
        )

        binding.btnMale.setOnClickListener {
            genderButtonClicked("Male")
        }

        binding.btnFemale.setOnClickListener {
            genderButtonClicked("Female")
        }

        binding.etBirthDate.setOnClickListener {
            birthDatePicker()
        }

        binding.etWeight.doOnTextChanged { text, _, _, _ ->
            viewModel.signUpData.weight = text.toString().toDoubleOrNull()
        }

        binding.etHeight.doOnTextChanged { text, _, _, _ ->
            viewModel.signUpData.height = text.toString().toDoubleOrNull()
        }

        binding.btnNext.setOnClickListener {

            if (validateInputs()){
                (parentFragment as QuestionnaireContainerFragment)
                    .goToNextPage()
            }
        }
    }



    private fun clearErrorOnFocus(vararg fields: EditText) {
        fields.forEach { field ->
            field.setOnFocusChangeListener { _, _ -> field.error = null }
        }
    }

    private fun birthDatePicker() {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val formattedDate = String.format(
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                binding.etBirthDate.setText(formattedDate)
                viewModel.signUpData.birthDate = formattedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun retriveDataIfExists(){

        if(viewModel.signUpData.weight != null){
            val data = viewModel.signUpData

            genderButtonClicked(data.gender.toString())
            binding.etBirthDate.setText(data.birthDate)
            binding.etWeight.setText(data.weight.toString())
            binding.etHeight.setText(data.height.toString())
        }
    }

    private fun genderButtonClicked(
        gender: String
    ) {
        viewModel.signUpData.gender = gender

        if (gender == "Male") {
            binding.btnMale.setBackgroundColor(resources.getColor(R.color.primary))
            binding.btnFemale.setBackgroundColor(resources.getColor(R.color.secondary))
        }
        else {
            binding.btnFemale.setBackgroundColor(resources.getColor(R.color.primary))
            binding.btnMale.setBackgroundColor(resources.getColor(R.color.secondary))
        }
    }

    private fun validateInputs() : Boolean {
        val data = viewModel.signUpData

        if (data.gender == null ||
            data.birthDate == null ||
            data.weight == null ||
            data.height == null
        ) {
            Toast.makeText(requireContext(), getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show()
            return false
        }

        if (data.height!! <= 0f){
            binding.etHeight.setError(getString(R.string.error_height))
            binding.etHeight.requestFocus()
            return false
        }

        if(data.weight!! <= 0f){
            binding.etWeight.setError(getString(R.string.error_weight))
            binding.etWeight.requestFocus()
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


