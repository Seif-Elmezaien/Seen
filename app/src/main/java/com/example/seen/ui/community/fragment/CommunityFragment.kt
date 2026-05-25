package com.example.seen.ui.community.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.FragmentCommunityBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.community.Data
import com.example.seen.ui.community.adapters.PostAdapter
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.ui.community.viewmodel.CommunityViewModelProviderFactory
import com.example.seen.util.Constants.Companion.ADVICES
import com.example.seen.util.Constants.Companion.GENERAL
import com.example.seen.util.Constants.Companion.GESTATIONAL
import com.example.seen.util.Constants.Companion.MODY
import com.example.seen.util.Constants.Companion.POST_PAGE_SIZE
import com.example.seen.util.Constants.Companion.TYPE1_LADA
import com.example.seen.util.Constants.Companion.TYPE_2
import com.example.seen.util.Resource
import com.google.android.material.chip.Chip


class CommunityFragment : Fragment() {

    private lateinit var postAdapter: PostAdapter

    var _binding: FragmentCommunityBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: CommunityViewModel
    var token: String? = null

    var selectedCategory = GENERAL

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getToken()
        initializeViewModel()
        setupRecyclerView()
        setPostAdapter()
        observeLikeError()
        handleChips()

        // Only fetch if no data yet
        if (viewModel.communityPostsResponse == null) {
            viewModel.getCommunityPosts(token!!, selectedCategory)
        } else {
            // Restore existing data immediately without new API call
            postAdapter.differ.submitList(viewModel.communityPostsResponse!!.data.toList())
        }

        viewModel.communityPosts.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    viewModel.communityPostsResponse?.data?.let {
                    postAdapter.differ.submitList(it.toList())
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "Error: $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        binding.btnAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_communityFragment_to_addPostFragment)
        }
        binding.btnSearchPost.setOnClickListener {
            findNavController().navigate(R.id.action_communityFragment_to_communitySearchFragment)
        }
    }

    private fun getToken() {
        val sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }

    private fun initializeViewModel(){
        // Application context to avoid leaks
        val db = SeenDatabase(requireContext().applicationContext)
        val userRepository = UserRepository(db)
        val communityRepository = CommunityRepository()

        // create factory
        val factory = CommunityViewModelProviderFactory(
            requireActivity().application,
            userRepository,
            communityRepository
        )

        // initialize ViewModel by activity
        viewModel = ViewModelProvider(requireActivity(), factory)
            .get(CommunityViewModel::class.java)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(requireContext())  // ✅ create a new instance
        binding.rvPosts.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@CommunityFragment.scrollListener)
        }
    }

    private fun setPostAdapter() {
        postAdapter.setOnCommentClickListener {
            viewModel.communityCommentPage = 1
            viewModel.communityCommentResponse = null
            val action =
                CommunityFragmentDirections
                    .actionCommunityFragmentToPostDetailsFragment(it)
            findNavController().navigate(action)
        }

        postAdapter.setOnLikeClickListener { updatedPost ->
            likePost(updatedPost)
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

    private fun observeLikeError(){
        viewModel.likeError.observe(viewLifecycleOwner) { postId ->

            val currentList = postAdapter.differ.currentList.toMutableList()

            val position = currentList.indexOfFirst { it.id == postId }

            if (position != -1) {

                val post = currentList[position]
                val revertedPost = post.copy(
                    is_liked = !(post.is_liked ?: false),
                    likes_count = post.likes_count + if (post.is_liked == true) -1 else 1
                )

                currentList[position] = revertedPost
                postAdapter.differ.submitList(currentList)

                viewModel.communityPostsResponse =
                    viewModel.communityPostsResponse?.copy(
                        data = currentList
                    )
            }
        }
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= POST_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getCommunityPosts(token!!,  selectedCategory)
                isScrolling = false
            }
        }
    }
    private fun handleChips() {
        binding.chipGroupCategories.children
            .filterIsInstance<Chip>()
            .forEach { chip ->
                chip.setOnClickListener { chip.isChecked = true }
            }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            val newCategory = when (checkedIds.firstOrNull()) {
                R.id.chipGeneral     -> GENERAL
                R.id.chipType1Lada   -> TYPE1_LADA
                R.id.chipType2       -> TYPE_2
                R.id.chipMonogenic   -> MODY
                R.id.chipGestational -> GESTATIONAL
                R.id.chipAdvise      -> ADVICES
                else -> selectedCategory
            }

            selectedCategory = newCategory

            viewModel.getCommunityPosts(token!!, selectedCategory, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}