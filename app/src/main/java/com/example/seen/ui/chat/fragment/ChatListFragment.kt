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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.FragmentChatListBinding
import com.example.seen.databinding.FragmentChatbotBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.local.SeenDatabase.Companion.invoke
import com.example.seen.datasource.repository.ChatRepository
import com.example.seen.datasource.repository.ChatbotRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.chat.adapter.ChatListAdapter
import com.example.seen.ui.chat.viewmodel.ChatViewModel
import com.example.seen.ui.chat.viewmodel.ChatViewModelProviderFactory
import com.example.seen.ui.chatbot.adapter.ChatbotAdapter
import com.example.seen.ui.chatbot.viewmodel.ChatbotViewModel
import com.example.seen.ui.chatbot.viewmodel.ChatbotViewModelProviderFactory
import com.example.seen.ui.community.adapters.UserAdapter
import com.example.seen.util.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatListFragment : Fragment() {
    var _binding: FragmentChatListBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: ChatViewModel

    private var token : String? = null
    lateinit var sharedPref : SharedPreferences

    private lateinit var adapter: ChatListAdapter

    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        getToken()
        setupListeners()
        setupRecyclerView()
        observeConversations()

        viewModel.getConversations(token!!)

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

    private fun getToken() {
        sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }

    private fun setupListeners() {

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etSearchChats.setOnClickListener {
            showSearchBottomSheet()
        }
    }

    private fun setupRecyclerView() {

        adapter = ChatListAdapter(requireContext(), currentUserId)

        binding.rvChats.apply {
            this.adapter = this@ChatListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        adapter.setOnItemClickListener { conversation ->
            val otherUserId = if (conversation.user1_id == currentUserId)
                conversation.user2?.id
            else
                conversation.user1?.id

            otherUserId?.let {
                val action = ChatListFragmentDirections
                    .actionChatListFragmentToChatFragment(it)
                findNavController().navigate(action)
            }
        }
    }

    private fun observeSearchUsers(
        adapter: UserAdapter,
    ) {

        viewModel.searchResults.observe(viewLifecycleOwner) { resource ->

            when (resource) {

                is Resource.Success -> {

                    resource.data?.let {

                        adapter.differ.submitList(
                            it.results
                        )
                    }
                }

                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        resource.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> Unit
            }
        }
    }

    private fun observeConversations() {

        // Get current user ID from Room first, then load messages
        viewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUserId = user.id
                adapter.updateCurrentUserId(user.id)
            }
        }

        viewModel.conversations.observe(viewLifecycleOwner) { resource ->

            when (resource) {

                is Resource.Loading -> {
                    binding.layoutEmptyState.visibility = View.GONE
                }

                is Resource.Success -> {

                    val conversations =
                        resource.data?.data?.data ?: emptyList()

                    if (conversations.isEmpty()) {

                        binding.rvChats.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE

                    } else {

                        binding.rvChats.visibility = View.VISIBLE
                        binding.layoutEmptyState.visibility = View.GONE

                        adapter.differ.submitList(conversations)
                    }
                }

                is Resource.Error -> {

                    binding.rvChats.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                }

                else -> Unit
            }
        }
    }

    private fun showSearchBottomSheet() {

        val bottomSheetDialog = BottomSheetDialog(requireContext())

        val view = layoutInflater.inflate(
            R.layout.item_search_user_chat_bottom_sheet,
            null
        )

        bottomSheetDialog.setContentView(view)

        val etSearch =
            view.findViewById<TextInputEditText>(R.id.etSearchChats)

        val rvUsers =
            view.findViewById<RecyclerView>(R.id.rvLikes)

        val searchAdapter = UserAdapter()

        rvUsers.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        observeSearchUsers(searchAdapter)

        var searchJob: Job? = null

        etSearch.isFocusableInTouchMode = true
        etSearch.requestFocus()

        etSearch.addTextChangedListener { editable ->

            searchJob?.cancel()

            searchJob = lifecycleScope.launch {

                delay(500)

                val query = editable.toString().trim()

                if (query.isNotEmpty()) {
                    viewModel.searchChatFriends(
                        token!!,
                        query
                    )
                } else {
                    searchAdapter.differ.submitList(emptyList())
                }
            }
        }

        searchAdapter.setOnSearchResultClickListener { user ->

            bottomSheetDialog.dismiss()

            val action =
                ChatListFragmentDirections
                    .actionChatListFragmentToChatFragment(
                        user.id!!
                    )

            findNavController().navigate(action)
        }

        bottomSheetDialog.show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}