package com.example.seen.ui.authentication.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.seen.R
import com.example.seen.databinding.FragmentLoginBinding
import com.example.seen.domain.model.authentication.LoginRequest
import com.example.seen.ui.MainActivity
import com.example.seen.ui.authentication.AuthActivity
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.example.seen.util.Resource

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDependencies()

        // Clear email error when user interacts with field
        binding.etEmail.setOnFocusChangeListener { _, _ ->
            binding.etEmail.error = null
        }

        // Clear password error when user interacts with field
        binding.etPassword.setOnFocusChangeListener { _, _ ->
            binding.etPassword.error = null
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            binding.tvRegisterNow.isEnabled = false

            if (isValidEmailAndPassword(email, password)) {
                val loginRequest = LoginRequest(email, password)
                login(loginRequest)
            }
        }

        binding.tvRegisterNow.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        observeLoginState()
    }

    /**
     * Intialize the Variables
     */
    private fun initDependencies(){
        viewModel = (activity as AuthActivity).viewModel
        sharedPref = requireActivity().getSharedPreferences("token", Context.MODE_PRIVATE)!!
        editor = sharedPref.edit()
    }

    /**
     * Validate email and password input
     * @return true if valid, false otherwise
     */
    private fun isValidEmailAndPassword(email: String, password: String): Boolean {
        // Check if email is not empty
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_email_empty)
            binding.etEmail.requestFocus()
            return false
        }

        // Check if email is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalid)
            binding.etEmail.requestFocus()
            return false
        }

        // Check if password is not empty
        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_password_empty)
            binding.etPassword.requestFocus()
            return false
        }

        // Check if password is at least 6 characters long
        if (password.length < 6) {
            binding.etPassword.error = getString(R.string.error_password_short)
            binding.etPassword.requestFocus()
            return false
        }

        // Check if password contains spaces
        if (password.contains(" ")) {
            binding.etPassword.error = getString(R.string.error_password_spaces)
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    /**
     * Trigger login request through ViewModel
     */
    private fun login(loginRequest: LoginRequest) {
        setButtonLoading(true)

        viewModel.login(loginRequest)
    }

    /**
     * Observe login state from ViewModel
     * Handles Loading, Success, and Error states
     */
    private fun observeLoginState(){
        viewModel.loginState.observe(viewLifecycleOwner){ response ->
            when(response){
                is Resource.Success -> {
                    binding.tvRegisterNow.isEnabled = true
                    setButtonLoading(false)

                    val token = response.data?.token
                    if (token != null) {
                        setToken(token)
                    }
                    Log.d("LoginFragment", "Token: $token")
                    Log.d("LoginFragment", "User: ${response.data?.user}")

                    Toast.makeText(requireContext(),
                        getString(R.string.login_succesful),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetLoginState()
                    goToMain()
                }
                is Resource.Error -> {
                    binding.tvRegisterNow.isEnabled = true
                    setButtonLoading(false)

                    Toast.makeText(
                        requireContext(),
                        "Error: ${response.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetLoginState()
                }
                is Resource.Loading -> { setButtonLoading(true) }

                null -> {}
            }
        }
    }

    /**
     * save token as SharedPrefrence
     */
    private fun setToken(token: String) {
        editor.putString("token", token)
        editor.apply()
    }

    /**
     * Navigate to MainActivity and clear back stack
     */
    private fun goToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setButtonLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) {
            getString(R.string.loading)
        } else {
            getString(R.string.login)
        }
    }

    /**
     * Clear binding to prevent memory leaks
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}