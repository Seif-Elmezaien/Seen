package com.example.seen.ui.chat.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.seen.databinding.FragmentChatBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.ChatRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.chat.ChatMessage
import com.example.seen.domain.model.entites.Log
import com.example.seen.ui.chat.adapter.ChatMessagesAdapter
import com.example.seen.ui.chat.viewmodel.ChatViewModel
import com.example.seen.ui.chat.viewmodel.ChatViewModelProviderFactory
import com.example.seen.util.Resource
import com.example.seen.util.ReverbManager

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatMessagesAdapter
    private val args: ChatFragmentArgs by navArgs()

    private var token: String? = null
    private lateinit var sharedPref: SharedPreferences

    // We read currentUserId from SharedPreferences (same place you store token)
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getToken()
        initializeViewModel()
        setupRecyclerView()
        setupKeyboardBehavior()
        setupListeners()
        observeViewModel()

    }

    private fun getToken() {
        sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }

    private fun initializeViewModel() {
        val db = SeenDatabase(requireContext().applicationContext)
        val factory = ChatViewModelProviderFactory(
            requireActivity().application,
            ChatRepository(),
            UserRepository(db)
        )
        viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = ChatMessagesAdapter(currentUserId)
        binding.rvChat.apply {
            this.adapter = this@ChatFragment.adapter
            layoutManager = LinearLayoutManager(requireContext()).also {
                it.stackFromEnd = true  // messages start from bottom
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSendComment.setOnClickListener {
            val text = binding.etUserMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            binding.etUserMessage.setText("")
            viewModel.sendMessage(token!!, args.receiverId, text)
        }
    }

    private fun observeViewModel() {

        // Get current user ID from Room first, then load messages
        viewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUserId = user.id
                adapter.updateCurrentUserId(user.id)
                viewModel.getMessages(token!!, args.receiverId)
            }
        }

        viewModel.messages.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.rvChat.visibility       = View.GONE
                    binding.llNoReminders.visibility = View.GONE
                }
                is Resource.Success -> {
                    val list = resource.data ?: emptyList()
                    if (list.isEmpty()) {
                        binding.rvChat.visibility        = View.GONE
                        binding.llNoReminders.visibility = View.VISIBLE
                    } else {
                        binding.llNoReminders.visibility = View.GONE
                        binding.rvChat.visibility        = View.VISIBLE
                        adapter.submitList(list)
                        binding.rvChat.scrollToPosition(adapter.itemCount - 1)

                        // Grab conversationId from first message and connect Reverb
                        val conversationId = list.first().conversation_id
                        connectReverb(conversationId)

                        // Set toolbar name
                        val otherMsg = list.firstOrNull { it.sender_id != currentUserId }
                        otherMsg?.sender?.let { user ->
                            binding.tvUserPostName.text = "${user.first_name} ${user.last_name}"
                            Glide.with(this).load(user.profile_picture).circleCrop().into(binding.ivProfile)
                        }
                    }
                }
                is Resource.Error -> {
                    binding.rvChat.visibility       = View.GONE
                    binding.llNoReminders.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    android.util.Log.e("chat", resource.message.toString())
                }
            }
        }

        viewModel.sendResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    // Append the new message to the list and scroll down
                    val current = adapter.currentList.toMutableList()
                    resource.data?.data?.let { newMsg ->
                        current.add(newMsg)
                        adapter.submitList(current)
                        binding.rvChat.scrollToPosition(adapter.itemCount - 1)

                        // Show RV if it was hidden
                        binding.llNoReminders.visibility = View.GONE
                        binding.rvChat.visibility       = View.VISIBLE
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    android.util.Log.e("chat", resource.message.toString())
                }
                else -> Unit
            }
        }
    }

    private var conversationId: Int = -1

    private fun connectReverb(convId: Int) {
        if (conversationId != -1) return  // already connected
        conversationId = convId

        ReverbManager.connect(token!!)
        ReverbManager.subscribeToChat(conversationId) { data ->
            val senderId = data.optInt("sender_id")
            if (senderId == currentUserId) return@subscribeToChat

            val newMsg = ChatMessage(
                id = data.optInt("message_id"),
                conversation_id = data.optInt("conversation_id"),
                sender_id = senderId,
                message = data.optString("message_text").takeIf { it.isNotEmpty() },
                image_url = null,
                voice_url = null,
                video_url = null,
                is_read = false,
                created_at = data.optString("created_at"),
                updated_at = data.optString("created_at"),
                sender = null
            )

            requireActivity().runOnUiThread {
                val current = adapter.currentList.toMutableList()
                current.add(newMsg)
                adapter.submitList(current)
                binding.rvChat.scrollToPosition(adapter.itemCount - 1)
                binding.llNoReminders.visibility = View.GONE
                binding.rvChat.visibility        = View.VISIBLE
            }
        }
    }

    private fun setupKeyboardBehavior() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
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