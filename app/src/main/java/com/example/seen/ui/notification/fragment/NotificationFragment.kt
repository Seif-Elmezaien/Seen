package com.example.seen.ui.notification.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.FragmentNotificationBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.NotificationRepository
import com.example.seen.datasource.repository.ProfileRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.notification.NotificationItem
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.ui.community.viewmodel.CommunityViewModelProviderFactory
import com.example.seen.ui.notification.adapter.NotificationAdapter
import com.example.seen.ui.notification.viewmodel.NotificationViewModel
import com.example.seen.ui.notification.viewmodel.NotificationViewModelProviderFactory
import com.example.seen.util.Resource

class NotificationFragment : Fragment() {

    var _binding: FragmentNotificationBinding? = null
    val binding get() = _binding!!

    private var token: String? = null
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var viewModel: NotificationViewModel
    private lateinit var communityViewModel: CommunityViewModel


    private var isLoading = false
    private var isScrolling = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getToken()
        initializeViewModel()
        initializeCommunityViewModel()
        setupRecyclerView()
        observeNotifications()
        observePost()           // ✅ add
        observeFriendRequest()  // ✅ add

        viewModel.getNotifications(token!!)
        binding.rvHome.addOnScrollListener(scrollListener)

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun getToken() {
        val sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }

    private fun initializeViewModel() {

        val factory = NotificationViewModelProviderFactory(
            requireActivity().application,
            NotificationRepository(),
            ProfileRepository(),
            CommunityRepository()
        )
        viewModel = ViewModelProvider(this, factory).get(NotificationViewModel::class.java)
    }

    private fun initializeCommunityViewModel(){
        // Application context to avoid leaks
        val db = SeenDatabase(requireContext().applicationContext)
        val userRepository = UserRepository(db)
        val communityRepository = CommunityRepository()
        val profileRepository = ProfileRepository()

        // create factory
        val factory = CommunityViewModelProviderFactory(
            requireActivity().application,
            userRepository,
            communityRepository,
            profileRepository
        )

        // initialize ViewModel by activity
        communityViewModel = ViewModelProvider(requireActivity(), factory)
            .get(CommunityViewModel::class.java)
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(requireContext())
        binding.rvHome.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(scrollListener)
        }

        notificationAdapter.setOnAcceptClickListener { item ->
            item.reference_id?.toIntOrNull()?.let { senderId ->
                viewModel.acceptFriendRequest(token!!, senderId)  // ✅ call API
                viewModel.deleteNotification(token!!, item.notification_id!!)
                removeNotification(item)
            }
        }

        notificationAdapter.setOnRejectClickListener { item ->
            removeNotification(item)
            viewModel.deleteNotification(token!!, item.notification_id!!)
        }

        notificationAdapter.setOnDeleteClickListener { item ->
            viewModel.deleteNotification(token!!, item.notification_id!!)
            removeNotification(item)
        }

        notificationAdapter.setOnItemClickListener { item ->
            // mark as read
            if (!item.is_read!!) {
                viewModel.markAsRead(token!!, item.notification_id!!)
                val updated = notificationAdapter.differ.currentList.toMutableList()
                val index = updated.indexOfFirst { it.notification_id == item.notification_id }
                if (index != -1) {
                    updated[index] = updated[index].copy(is_read = true)
                    notificationAdapter.differ.submitList(updated)
                }
            }

            // navigate based on type
            when (item.type) {
                "friend_request", "friend_accepted" -> {
                    item.reference_id?.toIntOrNull()?.let { userId ->
                        val action = NotificationFragmentDirections
                            .actionNotificationFragmentToProfileFragment(userId)
                        findNavController().navigate(action)
                    }
                }
                "like", "comment" -> {
                    item.extra_data?.post_id?.let { postId ->
                        item.extra_data.post_id?.let { postId ->
                            viewModel.getPost(token!!, postId)  // ✅ fetch then navigate
                        }
                    }
                }
            }
        }
    }

    private fun observePost() {
        viewModel.postResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* optional: show progress */ }
                is Resource.Success -> {
                    resource.data?.let { post ->
                        viewModel.clearPostResult()
                        communityViewModel.communityCommentPage = 1   // ✅ reset comments
                        communityViewModel.communityCommentResponse = null
                        val action = NotificationFragmentDirections
                            .actionNotificationFragmentToPostDetailsFragment(post.data)
                        findNavController().navigate(action)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearPostResult()
                }
                else -> Unit
            }
        }
    }

    private fun observeFriendRequest() {
        viewModel.friendRequestResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.notification_accept_request_title), Toast.LENGTH_SHORT).show()
                    viewModel.friendRequestResult.value = null
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.friendRequestResult.value = null
                }
                else -> Unit
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling = true
        }
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val lm = recyclerView.layoutManager as LinearLayoutManager
            val firstVisible = lm.findFirstVisibleItemPosition()
            val visibleCount = lm.childCount
            val totalCount = lm.itemCount

            val shouldPaginate = !isLoading && !viewModel.notificationsIsLastPage
                    && isScrolling && (firstVisible + visibleCount >= totalCount)

            if (shouldPaginate) {
                isScrolling = false
                viewModel.getNotifications(token!!)
            }
        }
    }

    private fun removeNotification(item: NotificationItem) {
        val updated = notificationAdapter.differ.currentList.toMutableList()
        updated.removeAll { it.notification_id == item.notification_id }
        notificationAdapter.differ.submitList(updated)
    }

    private fun observeNotifications() {
        viewModel.notifications.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    isLoading = true
                    binding.rvHome.visibility = View.GONE
                    binding.llNoReminders.visibility = View.GONE
                }
                is Resource.Success -> {
                    isLoading = false
                    val list = resource.data?.notifications ?: emptyList()
                    if (list.isEmpty()) {
                        binding.rvHome.visibility = View.GONE
                        binding.llNoReminders.visibility = View.VISIBLE
                    } else {
                        binding.rvHome.visibility = View.VISIBLE
                        binding.llNoReminders.visibility = View.GONE
                        notificationAdapter.differ.submitList(list)
                    }
                }
                is Resource.Error -> {
                    isLoading = false
                    binding.rvHome.visibility = View.GONE
                    binding.llNoReminders.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}