package com.example.seen.ui.authentication.fragments

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.seen.R
import com.example.seen.databinding.FragmentSignupBinding
import com.example.seen.domain.model.authentication.CheckEmailRequest
import com.example.seen.domain.model.authentication.SignupRequest
import com.example.seen.ui.activites.AuthActivity
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.example.seen.util.Resource
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var email: String
    private lateinit var phone: String
    private lateinit var password: String
    private lateinit var confirmPassword: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as AuthActivity).viewModel

        clearErrorOnFocus(
            binding.etFirstName,
            binding.etLastName,
            binding.etEmail,
            binding.etPhoneNumber,
            binding.etPassword,
            binding.etConfirmPassword
        )

        binding.btnContinue.setOnClickListener {
            firstName = binding.etFirstName.text.toString().trim().replaceFirstChar { it.uppercase() }
            lastName = binding.etLastName.text.toString().trim().replaceFirstChar { it.uppercase() }
            email = binding.etEmail.text.toString().trim()
            phone = binding.etPhoneNumber.text.toString().trim()
            password = binding.etPassword.text.toString().trim()
            confirmPassword = binding.etConfirmPassword.text.toString().trim()


            if(isValidUserInputs(firstName, lastName, email, phone, password, confirmPassword)){
                setButtonLoading(true)
                viewModel.checkEmailExist(CheckEmailRequest(email))
            }
        }

        binding.tvLoginNow.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        observeEmailExistState()
    }

    private fun clearErrorOnFocus(vararg fields: EditText) {
        fields.forEach { field ->
            field.setOnFocusChangeListener { _, _ -> field.error = null }
        }
    }

    private fun isValidUserInputs(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): Boolean {

        if (firstName.isEmpty()) {
            binding.etFirstName.error = getString(R.string.error_first_name_empty)
            binding.etFirstName.requestFocus()
            return false
        }

        if (lastName.isEmpty()) {
            binding.etLastName.error = getString(R.string.error_last_name_empty)
            binding.etLastName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_email_empty)
            binding.etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalid)
            binding.etEmail.requestFocus()
            return false
        }

        if (phone.isEmpty()) {
            binding.etPhoneNumber.error = getString(R.string.error_phone_empty)
            binding.etPhoneNumber.requestFocus()
            return false
        }

        if (phone.length != 11) {
            binding.etPhoneNumber.error = getString(R.string.error_phone_invalid)
            binding.etPhoneNumber.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_password_empty)
            binding.etPassword.requestFocus()
            return false
        }

        if (password.length < 8) {
            binding.etPassword.error = getString(R.string.error_password_short)
            binding.etPassword.requestFocus()
            return false
        }

        if (password.contains(" ")) {
            binding.etPassword.error = getString(R.string.error_password_spaces)
            binding.etPassword.requestFocus()
            return false
        }

        // Confirm Password
        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = getString(R.string.error_confirm_password_empty)
            binding.etConfirmPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = getString(R.string.error_password_not_match)
            binding.etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun observeEmailExistState(){
        viewModel.emailExistState.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    setButtonLoading(false)

                    if (response.data?.exists == true) {
                        binding.etEmail.error = "Email already exists"
                        binding.etEmail.requestFocus()
                        Log.d("SignupFragment", "Email already exists: ${response.data}")

                    } else {
                        Log.d("SignupFragment", "Email doesn't exists: ${response.data}")
                        viewModel.resetEmailExistState()
                        navigateToQuestionnaire()
                    }
                }
                is Resource.Error -> {
                    setButtonLoading(false)
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetEmailExistState()
                }

                is Resource.Loading -> {setButtonLoading(true)}

                null -> {}
            }
        }
    }

    private fun navigateToQuestionnaire() {
        viewModel.signUpData.first_name = firstName
        viewModel.signUpData.last_name = lastName
        viewModel.signUpData.email = email
        viewModel.signUpData.phone = phone
        viewModel.signUpData.password = password

        findNavController().navigate(R.id.action_signupFragment_to_questionnaireContainerFragment)
    }

    private fun setButtonLoading(isLoading: Boolean) {
        binding.btnContinue.isEnabled = !isLoading
        binding.btnContinue.text = if (isLoading) {
            getString(R.string.loading)
        } else {
            getString(R.string.continue_signup)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
