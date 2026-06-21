package com.example.seen.ui.chat.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seen.databinding.FragmentChatbotBinding
import com.example.seen.datasource.repository.ChatbotRepository
import com.example.seen.ui.chatbot.adapter.ChatbotAdapter
import com.example.seen.ui.chatbot.viewmodel.ChatbotViewModel
import com.example.seen.ui.chatbot.viewmodel.ChatbotViewModelProviderFactory
import kotlinx.coroutines.launch

class ChatListFragment : Fragment() {
    var _binding: FragmentChatbotBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: ChatbotViewModel

    private var token : String? = null
    lateinit var sharedPref : SharedPreferences

    private lateinit var adapter: ChatbotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        setupKeyboardBehavior()
        getToken()
        setupListeners()

    }

    private fun initializeViewModel() {
        val chatbotRepository = ChatbotRepository()
        val factory       =
            ChatbotViewModelProviderFactory(requireActivity().application, chatbotRepository)

        viewModel = ViewModelProvider(this, factory)[ChatbotViewModel::class.java]
    }

    private fun getToken() {
        sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }

    private fun setupListeners() {
    }


    private fun setupKeyboardBehavior() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            binding.commentBar.translationY = 0f
            binding.commentBar.setPadding(
                binding.commentBar.paddingLeft,
                binding.commentBar.paddingTop,
                binding.commentBar.paddingRight,
                if (imeHeight > 0) imeHeight else navHeight
            )
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}