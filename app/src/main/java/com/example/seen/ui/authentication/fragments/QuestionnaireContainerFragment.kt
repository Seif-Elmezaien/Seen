package com.example.seen.ui.authentication.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.example.seen.R
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.seen.databinding.FragmentQuestionnaireContainerBinding
import com.example.seen.domain.model.User
import com.example.seen.domain.model.authentication.SignupRequest
import com.example.seen.ui.MainActivity
import com.example.seen.ui.authentication.AuthActivity
import com.example.seen.ui.authentication.adapter.QuestionnaireContainerAdapter
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.example.seen.util.Resource

class QuestionnaireContainerFragment : Fragment() {
    private var _binding: FragmentQuestionnaireContainerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionnaireContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as AuthActivity).viewModel

        val adapter = QuestionnaireContainerAdapter(this)
        binding.vpQuestionnaire.adapter = adapter
        sharedPref = requireActivity().getSharedPreferences("token", Context.MODE_PRIVATE)!!
        editor = sharedPref.edit()

        // Link CircleIndicator to ViewPager2
        binding.ciQuestionnaire.setViewPager(binding.vpQuestionnaire)
        binding.vpQuestionnaire.isUserInputEnabled = false

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    val viewPager = binding.vpQuestionnaire

                    if (viewPager.currentItem > 0) {
                        // go to previous page instead of exiting
                        viewPager.currentItem = viewPager.currentItem - 1
                    } else {
                        // allow exit when on first page
                        isEnabled = false
//                        viewModel.signUpData = SignupRequest()
                        requireActivity().onBackPressed()
                    }
                }
            }
        )
    }

    fun goToPreviousPage(){
        binding.vpQuestionnaire.currentItem -= 1
    }

    fun goToNextPage() {
        val itemCount = binding.vpQuestionnaire.adapter!!.itemCount
        if (binding.vpQuestionnaire.currentItem < itemCount - 1){
            binding.vpQuestionnaire.currentItem += 1
        }
        else{
            val data = viewModel.signUpData
            val user = SignupRequest(
                data.first_name,
                data.last_name,
                data.email,
                data.password,
                data.phone,
                data.gender,
                data.birthDate,
                data.diabetes_type,
                data.insulin_therapy,
                data.weight,
                data.height
            )
            viewModel.signup(user)
            observeSignupStateState()
        }
    }

    private fun observeSignupStateState() {
        viewModel.signupState.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    val token = response.data?.token

                    if (token != null) {
                        setToken(token)
                    }
                    addUserToDatabase(response.data!!.user)
                    Log.d("QuestionnaireContainerFragment", "Token: $token")
                    Log.d("QuestionnaireContainerFragment", "User: ${response.data?.user}")

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.signed_up_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetSignupState()
                    goToAccountMade()
                }

                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${response.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Loading -> {}
                null -> {}
            }
        }
    }

    private fun setToken(token: String) {
        editor.putString("token", token)
        editor.apply()
    }

    private fun addUserToDatabase(user: User) {
        // Wait until there is database
    }

    private fun goToAccountMade() {
        findNavController().navigate(R.id.action_questionnaireContainerFragment_to_accountMadeFragment)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}