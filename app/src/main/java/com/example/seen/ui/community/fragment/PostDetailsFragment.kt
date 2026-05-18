package com.example.seen.ui.community.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.FragmentPostDetailsBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.community.Comment
import com.example.seen.ui.community.adapters.CommentAdapter
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.ui.community.viewmodel.CommunityViewModelProviderFactory
import com.example.seen.util.Constants.Companion.POST_PAGE_SIZE
import com.example.seen.util.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


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

    // ─── New: track which comment user wants to edit/delete ───
    private var selectedCommentId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupKeyboardBehavior()
        getToken()
        initializeViewModel()
        setPostItem()
        setCommentRecyclerView()

        viewModel.getPostComments(args.post.id, token!!)

        observeGetComments()

        observeAddComment()
        observeEditComment()
        observeDeleteComment()
        observeLikeComment()

        // ─── New: Send button click ───
        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addComment(token!!, args.post.id, text)
                binding.etComment.setText("")
            }
        }
    }



    private fun observeGetComments() {
        viewModel.communityComment.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { commentResponse ->
                        val comments = commentResponse.comments ?: emptyList()
                        commentAdapter.differ.submitList(comments)
                        isLastPage = comments.size < args.post.comments_count
                        if (isLastPage) binding.rvComments.setPadding(0, 0, 0, 0)
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(activity, "Error: $it", Toast.LENGTH_LONG).show()
                    }
                }

                is Resource.Loading -> showProgressBar()
            }
        })
    }
    private fun observeAddComment() {
        viewModel.addCommentResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newComment ->
                        // add the new comment at top of list
                        commentAdapter.addComment(newComment)
                        // scroll to top so user sees it
                        binding.rvComments.scrollToPosition(0)
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showProgressBar()
            }
        }
    }

    private fun observeEditComment() {
        viewModel.editCommentResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { updatedComment ->
                        // update just that one comment in the list
                        commentAdapter.updateComment(updatedComment)
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showProgressBar()
            }
        }
    }

    private fun observeDeleteComment() {
        viewModel.deleteCommentResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    // remove the deleted comment from the list
                    commentAdapter.removeComment(selectedCommentId)
                    selectedCommentId = -1
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showProgressBar()
            }
        }
    }

    private fun observeLikeComment() {
        viewModel.likeCommentResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    // UI already updated optimistically in adapter
                    // nothing extra needed here
                }
                is Resource.Error -> {
                    Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> { /* optional: show small loader */ }
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
                viewModel.getPostComments(args.post.id, token!!)
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
            val (bgRes, colorRes, categoryRes) = setCategoryBackground(args.post.category ?: "")

            tvUserPostName.text = args.post.user.full_name
            tvPostTime.text = getRelativeTime(args.post.created_at)
            tvCategory.text = getString(categoryRes)
            tvPostTitle.text = args.post.title
            tvPostContent.text = args.post.content
            flAvatarStroke.background = ContextCompat.getDrawable(requireContext(),setProfileBackground(args.post.user.diabetes_type ?: ""))
            if(args.post.images.isNotEmpty()){
                ivPostImage.visibility = View.VISIBLE
                Glide.with(root)
                    .load(args.post.images[0].url)
                    .into(ivPostImage)
            }
            else {
                ivPostImage.visibility = View.GONE
            }
            tvCategory.background = ContextCompat.getDrawable(requireContext(), bgRes)
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
            tvLikesCountDt.text = args.post.likes_count.toString()
            tvCommentsCountDt.text = args.post.comments_count.toString()
            Glide.with(root)
                .load(args.post.user.profile_picture.takeIf { it.isNotEmpty() })
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)
            binding.arrowBg.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
    private fun setProfileBackground(messageType : String) = when (messageType) {
        "Type1" -> R.drawable.avatar_border_type1
        "Type2" -> R.drawable.avatar_border_type2
        "LADA" -> R.drawable.avatar_border_lada
        "MODY" -> R.drawable.avatar_border_mody
        else ->  R.drawable.avatar_border_gestational
    }
    private fun setCategoryBackground(messageType: String): Triple<Int, Int, Int> = when (messageType) {
        "Type1 / LADA"  -> Triple(R.drawable.bg_diabetes_type1,       R.color.profile_type1_stroke,       R.string.category_type1_lada)
        "Type2"         -> Triple(R.drawable.bg_diabetes_type2,        R.color.profile_type2_stroke,       R.string.category_type2)
        "MODY"          -> Triple(R.drawable.bg_diabetes_mody,         R.color.profile_mody_stroke,        R.string.category_mody)
        "Gestational"   -> Triple(R.drawable.bg_diabetes_gestational,  R.color.profile_gestational_stroke, R.string.category_gestational)
        "Advices"       -> Triple(R.drawable.bg_diabetes_advise,       R.color.advise_gray,                R.string.category_advise)
        else            -> Triple(R.drawable.bg_diabetes_general,  R.color.general_yellow, R.string.category_general)
    }
    private fun setCommentRecyclerView() {
        commentAdapter = CommentAdapter(
            context = requireContext(),

            // ─── Like: toggle like on server ───
            onLikeClick = { comment ->
                viewModel.likeComment(token!!, comment.id ?: return@CommentAdapter)
            },

            // ─── Edit: show a dialog to edit text ───
            onEditClick = { comment ->
                selectedCommentId = comment.id ?: return@CommentAdapter
                showEditCommentDialog(comment)
            },

            // ─── Delete: show confirmation dialog ───
            onDeleteClick = { comment ->
                selectedCommentId = comment.id ?: return@CommentAdapter
                showDeleteCommentDialog(comment)
            }
        )

        binding.rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@PostDetailsFragment.scrollListener)
        }
    }

    // ════════════════════════════════════════════════════════
    //  Dialogs
    // ════════════════════════════════════════════════════════

    private fun showEditCommentDialog(comment: Comment) {
        // simple input dialog
        val editText = android.widget.EditText(requireContext())
        editText.setText(comment.comment_text)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Comment")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    //!! (Non-null Assertion)
                    viewModel.editComment(token!!, comment.id!!, newText)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteCommentDialog(comment: Comment) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                //!! (Non-null Assertion)
                viewModel.deleteComment(token!!, comment.id!!)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun getRelativeTime(isoTimestamp: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val date = sdf.parse(isoTimestamp) ?: return ""
        val now = Date()
        val diffMillis = now.time - date.time

        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours   = minutes / 60

        val calNow = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val calDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = date }

        calNow.set(Calendar.HOUR_OF_DAY, 0); calNow.set(Calendar.MINUTE, 0)
        calNow.set(Calendar.SECOND, 0);      calNow.set(Calendar.MILLISECOND, 0)

        calDate.set(Calendar.HOUR_OF_DAY, 0); calDate.set(Calendar.MINUTE, 0)
        calDate.set(Calendar.SECOND, 0);       calDate.set(Calendar.MILLISECOND, 0)

        val calendarDays = ((calNow.timeInMillis - calDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            seconds < 60      -> if (seconds <= 1) getString(R.string.time_just_now)
            else getString(R.string.time_seconds_ago, seconds)
            minutes < 60      -> if (minutes == 1L) getString(R.string.time_one_minute_ago)
            else getString(R.string.time_minutes_ago, minutes)
            hours < 24        -> if (hours == 1L) getString(R.string.time_one_hour_ago)
            else getString(R.string.time_hours_ago, hours)
            calendarDays == 1 -> getString(R.string.time_one_day_ago)
            calendarDays == 2 -> getString(R.string.time_two_days_ago)
            calendarDays < 7  -> getString(R.string.time_days_ago, calendarDays)
            else              -> {
                val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                displayFormat.timeZone = TimeZone.getTimeZone("UTC")
                displayFormat.format(date)
            }
        }
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
}