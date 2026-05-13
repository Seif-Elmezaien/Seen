package com.example.seen.ui.community.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.FragmentCommunityBinding
import com.example.seen.databinding.FragmentPostDetailsBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.local.SeenDatabase.Companion.invoke
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.community.adapters.CommentAdapter
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.ui.community.viewmodel.CommunityViewModelProviderFactory
import com.example.seen.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.seen.util.Resource


class PostDetailsFragment : Fragment() {

    var _binding: FragmentPostDetailsBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: CommunityViewModel
    var token: String? = null

    private lateinit var commentAdapter: CommentAdapter
    private val args: PostDetailsFragmentArgs by navArgs()

    var isLoading = false
    var isLastPage = false
    var isScrolling = false
    var currentPage = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getToken()
        initializeViewModel()
        setPostItem()
        setCommentRecyclerView()

        viewModel.getPostComments(args.post.id, token!!, page = 1)

        viewModel.communityComment.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { commentResponse ->

                        val comments = commentResponse.comments ?: emptyList()

                        commentAdapter.differ.submitList(comments)
                        isLastPage = comments.size < QUERY_PAGE_SIZE
                        if (isLastPage) {
                            binding.rvComments.setPadding(0, 0, 0, 0)
                        }
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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                currentPage++  // increment before fetching
                viewModel.getPostComments(args.post.id, token!!, currentPage)
                isScrolling = false
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        // initialize ViewModel
        viewModel = ViewModelProvider(this, factory)
            .get(CommunityViewModel::class.java)
    }
    private fun getToken() {
        val sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }
    private fun setPostItem(){
        binding.apply {
            tvUserPostName.text = args.post.user.full_name
            tvPostTime.text = args.post.created_at
            tvCategory.text = args.post.category
            tvPostTitle.text = args.post.title
            tvPostContent.text = args.post.content
            if(args.post.images.isNotEmpty()){
                Glide.with(root)
                    .load(args.post.images[0].media)
                    .into(ivPostImage)
            }
            tvLikesCountDt.text = args.post.likes_count.toString()
            tvCommentsCountDt.text = args.post.comments_count.toString()
            Glide.with(root)
                .load(args.post.user.profile_picture.isNotEmpty() ?: "")
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)
            binding.arrowBg.setOnClickListener {
                requireActivity().onBackPressed()
            }
    }
    }
    private fun setCommentRecyclerView(){
        commentAdapter = CommentAdapter()
        binding.rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@PostDetailsFragment.scrollListener)
        }
    }

}