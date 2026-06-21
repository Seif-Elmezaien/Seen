package com.example.seen.ui.chat.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.seen.databinding.FragmentAccountAndSettingsBinding
import com.example.seen.databinding.FragmentChatBinding
import com.example.seen.databinding.FragmentChatListBinding
import com.example.seen.datasource.repository.ChatRepository
import com.example.seen.datasource.repository.ChatbotRepository
import com.example.seen.ui.chat.viewmodel.ChatViewModel
import com.example.seen.ui.chat.viewmodel.ChatViewModelProviderFactory
import com.example.seen.ui.chatbot.adapter.ChatbotAdapter
import com.example.seen.ui.chatbot.viewmodel.ChatbotViewModel
import com.example.seen.ui.chatbot.viewmodel.ChatbotViewModelProviderFactory


class ChatFragment : Fragment() {
    var _binding: FragmentChatBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: ChatViewModel
    private val args: ChatFragmentArgs by navArgs()

    private var token : String? = null
    lateinit var sharedPref : SharedPreferences

    private lateinit var adapter: ChatbotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        getToken()
        setupListeners()

        Toast.makeText(requireContext(),"${args.conversationId}", Toast.LENGTH_LONG).show()

    }

    private fun initializeViewModel() {
        val chatRepository = ChatRepository()
        val factory       =
            ChatViewModelProviderFactory(requireActivity().application, chatRepository)

        viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]
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