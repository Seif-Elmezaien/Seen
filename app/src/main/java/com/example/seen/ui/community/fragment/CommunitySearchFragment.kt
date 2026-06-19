package com.example.seen.ui.community.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.FragmentCommunityBinding
import com.example.seen.databinding.FragmentCommunitySearchBinding
import com.example.seen.ui.community.adapters.PostAdapter
import com.example.seen.ui.community.adapters.UserAdapter
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.emptyList


class CommunitySearchFragment : Fragment() {

    private var _binding: FragmentCommunitySearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by activityViewModels()

    private lateinit var postsAdapter: PostAdapter
    private lateinit var accountsAdapter: UserAdapter

    private lateinit var token: String

    // Debounce: wait 500ms after user stops typing before calling API
    private var debounceJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunitySearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getToken()
        setupAdapters()
        setupSearchInput()
        setupScrollListeners()
        observeSearchResults()

        // Start in empty state
        showEmptyState()
    }

    // ─── Adapters ────────────────────────────────────────────────────────────

    private fun setupAdapters() {
        postsAdapter = PostAdapter(requireContext(), childFragmentManager)
        binding.rvPosts.apply {
            adapter = postsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        accountsAdapter = UserAdapter()
        binding.rvAccounts.apply {
            adapter = accountsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // ─── Chips ───────────────────────────────────────────────────────────────

    // ─── Search Input ────────────────────────────────────────────────────────

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""

                debounceJob?.cancel()

                if (query.isEmpty()) {
                    viewModel.clearSearchState()
                    showEmptyState()
                    return
                }

                debounceJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(500)
                    viewModel.searchPostAndUser(token, query, isNewQuery = true)
                }
            }
        })
    }

    // ─── Pagination on Scroll ────────────────────────────────────────────────

    private fun setupScrollListeners() {
        // Posts pagination
        binding.rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount

                if (dy > 0 && lastVisible >= total - 2) {
                    val query = binding.etSearch.text.toString().trim()
                    if (query.isNotEmpty()) {
                        // isNewQuery = false → append next page
                        viewModel.searchPostAndUser(token, query, isNewQuery = false)
                    }
                }
            }
        })

        // Accounts pagination
        binding.rvAccounts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount

                if (dy > 0 && lastVisible >= total - 2) {
                    val query = binding.etSearch.text.toString().trim()
                    if (query.isNotEmpty()) {
                        viewModel.searchPostAndUser(token, query, isNewQuery = false)
                    }
                }
            }
        })
    }

    // ─── Observe ─────────────────────────────────────────────────────────────

    private fun observeSearchResults() {
        viewModel.searchResults.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)

                is Resource.Success -> {
                    showLoading(false)
                    val data = resource.data

                    val posts = data?.posts?.data ?: emptyList()
                    val users = data?.users ?: emptyList()

                    postsAdapter.differ.submitList(posts)
                    accountsAdapter.differ.submitList(users)

                    // Decide what to show based on selected chip + result emptiness
                    val isPostsSelected = true
                    // If both are empty, show empty state
                    val activeListEmpty = if (isPostsSelected) posts.isEmpty() else users.isEmpty()

                    if (activeListEmpty) {
                        showEmptyState()
                    } else {
                        if (isPostsSelected) showPosts() else showAccounts()
                    }
                }

                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ─── UI State Helpers ────────────────────────────────────────────────────

    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.rvPosts.visibility = View.GONE
        binding.rvAccounts.visibility = View.GONE
    }

    private fun showPosts() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.rvPosts.visibility = View.VISIBLE
        binding.rvAccounts.visibility = View.GONE
    }

    private fun showAccounts() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.rvPosts.visibility = View.GONE
        binding.rvAccounts.visibility = View.VISIBLE
    }

    private fun showLoading(isLoading: Boolean) {
        // Optional: show/hide a ProgressBar if you have one
        // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun getToken() {
        val sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}