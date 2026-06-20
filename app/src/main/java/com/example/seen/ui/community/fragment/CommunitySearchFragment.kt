package com.example.seen.ui.community.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.FragmentCommunitySearchBinding
import com.example.seen.domain.model.community.Data
import com.example.seen.domain.model.community.request.EditPostRequest
import com.example.seen.ui.community.adapters.PostAdapter
import com.example.seen.ui.community.adapters.UserAdapter
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.util.Constants.Companion.ADVICES
import com.example.seen.util.Constants.Companion.GENERAL
import com.example.seen.util.Constants.Companion.GESTATIONAL
import com.example.seen.util.Constants.Companion.MODY
import com.example.seen.util.Constants.Companion.SEARCH_POST_USER_TIME_DELAY
import com.example.seen.util.Constants.Companion.TYPE1_LADA
import com.example.seen.util.Constants.Companion.TYPE_2
import com.example.seen.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.emptyList


class CommunitySearchFragment : Fragment() {

    private var _binding: FragmentCommunitySearchBinding? = null
    private val binding get() = _binding!!

    private enum class SearchTab { POSTS, PROFILES }

    private var selectedTab = SearchTab.POSTS
    private val viewModel: CommunityViewModel by activityViewModels()

    private lateinit var postsAdapter: PostAdapter
    private lateinit var accountsAdapter: UserAdapter
    private lateinit var token: String

    private var isLoading = false
    private var isLastPage = false
    private var isScrolling = false

    private var debounceJob: Job? = null

    // Keep a single scroll listener instance so we can remove/re-add it
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) isScrolling = true
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val lm = recyclerView.layoutManager as LinearLayoutManager
            val firstVisible = lm.findFirstVisibleItemPosition()
            val visibleCount = lm.childCount
            val totalCount = lm.itemCount
            val hasValidQuery = binding.etSearch.text.toString().isNotBlank()

            val shouldLoadMore = !isLoading && !isLastPage && isScrolling
                    && (firstVisible + visibleCount >= totalCount)
                    && hasValidQuery

            if (shouldLoadMore) {
                isScrolling = false
                isLoading = true
                viewModel.searchPostAndUser(token, binding.etSearch.text.toString(), isNewQuery = false)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCommunitySearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getToken()
        setupAdapters()
        setupSearchInput()
        observeSearchResults()
        setupListeners()
        showEmptyState()
        attachScrollListener() // attach to default tab's RV

        viewModel.getUserId().observe(viewLifecycleOwner) { user ->
            postsAdapter.userId = user.id
        }

        observeLikeError()
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

        postsAdapter.setOnLikeClickListener { updatedPost ->
            likePost(updatedPost)
        }

        postsAdapter.setOnCommentClickListener {
            viewModel.communityCommentPage = 1
            viewModel.communityCommentResponse = null
            val action =
                CommunitySearchFragmentDirections
                    .actionCommunitySearchFragmentToPostDetailsFragment(it)
            findNavController().navigate(action)
        }

        postsAdapter.setOnEditClickListener {
            showEditPostDialog(it)
        }

        postsAdapter.setOnDeleteClickListener {
            showDeletePostDialog(it)
        }
    }

    private fun observeLikeError(){
        viewModel.likeError.observe(viewLifecycleOwner) { postId ->

            val currentList = postsAdapter.differ.currentList.toMutableList()

            val position = currentList.indexOfFirst { it.id == postId }

            if (position != -1) {

                val post = currentList[position]
                val revertedPost = post.copy(
                    is_liked = !(post.is_liked ?: false),
                    likes_count = post.likes_count + if (post.is_liked == true) -1 else 1
                )

                currentList[position] = revertedPost
                postsAdapter.differ.submitList(currentList)

                viewModel.communityPostsResponse =
                    viewModel.communityPostsResponse?.copy(
                        data = currentList
                    )
            }
        }
    }

    // ─── Scroll Listener ─────────────────────────────────────────────────────

    private fun attachScrollListener() {
        // Remove from both first to avoid duplicates
        binding.rvPosts.removeOnScrollListener(scrollListener)
        binding.rvAccounts.removeOnScrollListener(scrollListener)

        val activeRv = if (selectedTab == SearchTab.POSTS) binding.rvPosts else binding.rvAccounts
        activeRv.addOnScrollListener(scrollListener)
    }

    // ─── Search Input ────────────────────────────────────────────────────────

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable?.toString()?.trim().orEmpty()
            debounceJob?.cancel()

            if (query.isBlank()) {
                viewModel.clearSearchState()
                showEmptyState()
                return@addTextChangedListener
            }

            debounceJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(SEARCH_POST_USER_TIME_DELAY)
                if (query == binding.etSearch.text.toString().trim()) {
                    resetPaginationState() // reset before new query
                    viewModel.searchPostAndUser(token = token, query = query, isNewQuery = true)
                }
            }
        }
    }

    // ─── Observe ─────────────────────────────────────────────────────────────

    private fun observeSearchResults() {
        viewModel.searchResults.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)

                is Resource.Success -> {
                    showLoading(false)
                    isLoading = false // ✅ unlock pagination

                    val data = resource.data
                    val posts = data?.data?.posts?.data ?: emptyList()
                    val users = data?.data?.users ?: emptyList()

                    // ✅ Update isLastPage based on whether the backend has more pages
                    val currentPage = data?.data?.posts?.meta?.current_page ?: 1
                    val lastPage = data?.data?.posts?.meta?.last_page ?: 1
                    isLastPage = currentPage >= lastPage

                    postsAdapter.differ.submitList(posts)
                    accountsAdapter.differ.submitList(users)
                    updateVisibleContent()
                }

                is Resource.Error -> {
                    showLoading(false)
                    isLoading = false // ✅ unlock pagination on error too
                    showEmptyState()
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.tvPosts.setOnClickListener {
            selectedTab = SearchTab.POSTS
            selectReadingType(binding.tvPosts)
            attachScrollListener() // ✅ re-attach to the correct RV
            updateVisibleContent()
        }

        binding.tvProfiles.setOnClickListener {
            selectedTab = SearchTab.PROFILES
            selectReadingType(binding.tvProfiles)
            attachScrollListener() // ✅ re-attach to the correct RV
            updateVisibleContent()
        }

        selectReadingType(binding.tvPosts)

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateVisibleContent() {
        val resource = viewModel.searchResults.value
        if (resource !is Resource.Success) return

        val posts = resource.data?.data?.posts?.data ?: emptyList()
        val users = resource.data?.data?.users ?: emptyList()

        when (selectedTab) {
            SearchTab.POSTS -> if (posts.isEmpty()) showEmptyState() else showPosts()
            SearchTab.PROFILES -> if (users.isEmpty()) showEmptyState() else showAccounts()
        }
    }

    private fun likePost(updatedPost: Data) {
        viewModel.communityPostsResponse?.let { response ->

            val updatedList = response.data.toMutableList()

            val index = updatedList.indexOfFirst { it.id == updatedPost.id }

            if (index != -1) {
                updatedList[index] = updatedPost

                viewModel.communityPostsResponse =
                    response.copy(data = updatedList)
            }
        }

        viewModel.likePost(token!!, updatedPost.id)
    }

    private fun showEditPostDialog(post: Data) {
        val view = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val etTitle = EditText(requireContext()).apply {
            setText(post.title)
            hint = "Title"
        }

        val etContent = EditText(requireContext()).apply {
            setText(post.content)
            hint = "Content"
            minLines = 3
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        val categories = listOf(GENERAL, TYPE1_LADA, TYPE_2, MODY, GESTATIONAL, ADVICES)
        val spinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )
            setSelection(categories.indexOf(post.category).takeIf { it >= 0 } ?: 0)
        }

        view.addView(etTitle)
        view.addView(etContent)
        view.addView(spinner)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Post")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()
                val category = categories[spinner.selectedItemPosition]
                if (title.isNotEmpty() && content.isNotEmpty()) {
                    viewModel.editPost(token!!, post.id, EditPostRequest(title, content, category))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeletePostDialog(post: Data) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePostFromCache(post.id)
                val newList = postsAdapter.differ.currentList.toMutableList()
                newList.removeAll { it.id == post.id }
                postsAdapter.differ.submitList(newList)
                viewModel.deletePost(token!!, post.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeEditDeletePost() {
        viewModel.editPostResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { updatedPost ->
                        viewModel.updatePostInCache(updatedPost)
                        // resubmit the relevant adapter
                        postsAdapter.differ.submitList(null)
                        postsAdapter.differ.submitList(viewModel.communityPostsResponse?.data?.toList())
                    }
                    viewModel.clearEditPostState()
                }
                is Resource.Error -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }

        viewModel.deletePostResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    viewModel.clearDeletePostState()
                }
                is Resource.Error -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    // ─── Pagination Helpers ──────────────────────────────────────────────────

    private fun resetPaginationState() {
        isLoading = false
        isLastPage = false
        isScrolling = false
    }

    // ─── UI State ────────────────────────────────────────────────────────────

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

    private fun selectReadingType(selectedView: TextView) {
        resetReadingType()
        selectedView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_search_selected)
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        TextViewCompat.setCompoundDrawableTintList(
            selectedView,
            ContextCompat.getColorStateList(requireContext(), R.color.white)
        )
        selectedView.isEnabled = false
    }

    private fun resetReadingType() {
        listOf(binding.tvPosts, binding.tvProfiles).forEach {
            it.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_search_unselected)
            it.setTextColor(requireContext().getColor(R.color.primary))
            TextViewCompat.setCompoundDrawableTintList(
                it,
                ContextCompat.getColorStateList(requireContext(), R.color.primary)
            )
            it.isEnabled = true
        }
    }

    private fun showLoading(show: Boolean) {
        // binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun getToken() {
        val sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }


    override fun onResume() {
        super.onResume()
        val cached = viewModel.searchResponse ?: return
        val posts = cached.data.posts.data
        val users = cached.data.users
        postsAdapter.differ.submitList(null)
        postsAdapter.differ.submitList(posts)
        accountsAdapter.differ.submitList(null)
        accountsAdapter.differ.submitList(users)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}