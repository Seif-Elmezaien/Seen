package com.example.seen.ui.community.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
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
import com.example.seen.databinding.ItemCommunityPostBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.ProfileRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.community.Comment
import com.example.seen.domain.model.community.Data
import com.example.seen.domain.model.community.request.EditPostRequest
import com.example.seen.ui.community.adapters.CommentAdapter
import com.example.seen.ui.community.dialog.ImagePreviewDialogFragment
import com.example.seen.ui.community.dialog.LikesBottomSheetFragment
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.ui.community.viewmodel.CommunityViewModelProviderFactory
import com.example.seen.util.Constants.Companion.ADVICES
import com.example.seen.util.Constants.Companion.COMMENT_PAGE_SIZE
import com.example.seen.util.Constants.Companion.GENERAL
import com.example.seen.util.Constants.Companion.GESTATIONAL
import com.example.seen.util.Constants.Companion.LADA
import com.example.seen.util.Constants.Companion.MODY
import com.example.seen.util.Constants.Companion.TYPE1_LADA
import com.example.seen.util.Constants.Companion.TYPE_1
import com.example.seen.util.Constants.Companion.TYPE_2
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
    var isLiked = false
    var likesCount = 0

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

        getToken()
        setupKeyboardBehavior()
        initializeViewModel()
        setPostItem()
        setCommentRecyclerView()
        observeEditDeletePost()

        viewModel.getPostComments(args.post.id, token!!)

        viewModel.getUserId().observe(viewLifecycleOwner) { user ->
            commentAdapter.userId = user.id

            if (user.id == args.post.user.id){
                binding.editPost.visibility = View.VISIBLE
                binding.deletePost.visibility = View.VISIBLE
            } else {
                binding.editPost.visibility = View.GONE
                binding.deletePost.visibility = View.GONE
            }
        }

        observeLikeError()
        observeGetComments()

        observeLikeComment()
        observeAddComment()
        observeEditComment()
        observeDeleteComment()

        // ─── New: Send button click ───
        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addComment(token!!, args.post.id, text)
                binding.etComment.setText("")
            }
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
        val profileRepository = ProfileRepository()


        // create factory
        val factory = CommunityViewModelProviderFactory(
            requireActivity().application,
            userRepository,
            communityRepository,
            profileRepository
        )

        // initialize ViewModel by activity
        viewModel = ViewModelProvider(requireActivity(), factory)
            .get(CommunityViewModel::class.java)
    }

    private fun setPostItem(){
        binding.apply {
            val (bgRes, colorRes, categoryRes) = setCategoryBackground(args.post.category ?: "")
            isLiked = args.post.is_liked ?: false
            likesCount = args.post.likes_count

            tvUserPostName.text = args.post.user.full_name
            tvPostTime.text = getRelativeTime(args.post.created_at)
            tvCategory.text = getString(categoryRes)
            tvPostTitle.text = args.post.title
            tvPostContent.text = args.post.content
            flAvatarStroke.background = ContextCompat.getDrawable(requireContext(),setProfileBackground(args.post.user.diabetes_type ?: ""))

            bindPhotos(args.post.images.map { it.url })

            tvCategory.background = ContextCompat.getDrawable(requireContext(), bgRes)
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
            ivLike.isSelected = isLiked
            tvLikesCount.text = likesCount.toString()
            tvCommentsCount.text = args.post.comments_count.toString()
            Glide.with(root)
                .load(args.post.user.profile_picture.takeIf { it.isNotEmpty() })
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)
            arrowBg.setOnClickListener {
                findNavController().navigateUp()
            }

            ivLike.setOnClickListener {
                // 1. flip UI immediately
                isLiked = !isLiked
                likesCount += if (isLiked) 1 else -1
                ivLike.isSelected = isLiked
                tvLikesCount.text = likesCount.toString()

                // 2. update the shared cache so CommunityFragment sees it
                viewModel.communityPostsResponse?.let { response ->
                    val updatedList = response.data.toMutableList()
                    val index = updatedList.indexOfFirst { it.id == args.post.id }
                    if (index != -1) {
                        updatedList[index] = updatedList[index].copy(
                            is_liked = isLiked,
                            likes_count = likesCount
                        )
                        viewModel.communityPostsResponse = response.copy(data = updatedList)
                    }
                }

                // ✅ also update searchResponse
                viewModel.searchResponse?.let { response ->
                    val updatedPosts = response.data.posts.data.toMutableList()
                    val index = updatedPosts.indexOfFirst { it.id == args.post.id }
                    if (index != -1) {
                        updatedPosts[index] = updatedPosts[index].copy(
                            is_liked = isLiked,
                            likes_count = likesCount
                        )
                        viewModel.searchResponse = response.copy(
                            data = response.data.copy(
                                posts = response.data.posts.copy(data = updatedPosts)
                            )
                        )
                    }
                }

                // 3. fire API
                viewModel.likePost(token!!, args.post.id)
            }

            tvLikesCount.setOnClickListener {
                LikesBottomSheetFragment(token = token!!, postId = args.post.id)
                    .show(parentFragmentManager, "post_likes")
            }

            editPost.setOnClickListener { showEditPostDialog(args.post) }
            deletePost.setOnClickListener { showDeletePostDialog(args.post) }

            ivProfile.setOnClickListener {
                val action = PostDetailsFragmentDirections.actionPostDetailsFragmentToProfileFragment(args.post.user.id!!)
                findNavController().navigate(action)
            }
        }
    }

    private fun bindPhotos(photos: List<String>) {
        binding.apply {
            when (photos.size) {
                0 -> {
                    ivPhoto1.visibility = View.GONE
                    layoutRow2.visibility = View.GONE
                }
                1 -> {
                    ivPhoto1.visibility = View.VISIBLE
                    layoutRow2.visibility = View.GONE
                    Glide.with(requireContext()).load(photos[0]).into(ivPhoto1)
                    ivPhoto1.setOnClickListener { openViewer(photos, 0) }  // 👈
                }
                2 -> {
                    ivPhoto1.visibility = View.VISIBLE
                    layoutRow2.visibility = View.VISIBLE
                    flPhoto3.visibility = View.GONE
                    Glide.with(requireContext()).load(photos[0]).into(ivPhoto1)
                    Glide.with(requireContext()).load(photos[1]).into(ivPhoto2)
                    ivPhoto1.setOnClickListener { openViewer(photos, 0) }  // 👈
                    ivPhoto2.setOnClickListener { openViewer(photos, 1) }  // 👈
                }
                else -> {
                    ivPhoto1.visibility = View.VISIBLE
                    layoutRow2.visibility = View.VISIBLE
                    flPhoto3.visibility = View.VISIBLE
                    Glide.with(requireContext()).load(photos[0]).into(ivPhoto1)
                    Glide.with(requireContext()).load(photos[1]).into(ivPhoto2)
                    Glide.with(requireContext()).load(photos[2]).into(ivPhoto3)
                    ivPhoto1.setOnClickListener { openViewer(photos, 0) }  // 👈
                    ivPhoto2.setOnClickListener { openViewer(photos, 1) }  // 👈
                    ivPhoto3.setOnClickListener { openViewer(photos, 2) }  // 👈

                    if (photos.size > 3) {
                        tvMoreCount.visibility = View.VISIBLE
                        tvMoreCount.text = "+${photos.size - 3}"
                    } else {
                        tvMoreCount.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun openViewer(photos: List<String>, startPosition: Int) {
        ImagePreviewDialogFragment(
            images = photos.toMutableList(),
            startPosition = startPosition,
            isDeletable = false    // 👈 no delete in details screen
        ).show(parentFragmentManager, "image_viewer")
    }

    private fun setCommentRecyclerView() {
        commentAdapter = CommentAdapter(
            requireContext(),
        )

        binding.rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@PostDetailsFragment.scrollListener)
        }

        commentAdapter.setOnLikeClickListener { updatedComment ->
            viewModel.communityCommentResponse?.let { response ->

                val updatedList = response.comments.toMutableList()

                val index = updatedList.indexOfFirst { it.id == updatedComment.id }

                if (index != -1) {
                    updatedList[index] = updatedComment

                    viewModel.communityCommentResponse =
                        response.copy(comments = updatedList)
                }
            }

            viewModel.likeComment(token!!, updatedComment.id!!)
        }

        commentAdapter.setOnEditClickListener {
            showEditCommentDialog(it)
        }

        commentAdapter.setOnDeleteClickListener {
            showDeleteCommentDialog(it)
        }

        commentAdapter.setOnLikeCountClickListener { comment ->
            LikesBottomSheetFragment(token = token!!, commentId = comment.id)
                .show(parentFragmentManager, "comment_likes")
        }

        commentAdapter.setOnProfileClickListener {
            val action = PostDetailsFragmentDirections.actionPostDetailsFragmentToProfileFragment(it.id!!)
            findNavController().navigate(action)
        }
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
                        // update UI fields directly since we're already on the details screen
                        binding.tvPostTitle.text = updatedPost.title
                        binding.tvPostContent.text = updatedPost.content
                        val (bgRes, colorRes, categoryRes) = setCategoryBackground(updatedPost.category ?: "")
                        binding.tvCategory.text = getString(categoryRes)
                        binding.tvCategory.background = ContextCompat.getDrawable(requireContext(), bgRes)
                        binding.tvCategory.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
                    }
                    viewModel.clearEditPostState()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearEditPostState()
                }
                else -> Unit
            }
        }

        viewModel.deletePostResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    viewModel.clearDeletePostState()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearDeletePostState()
                }
                else -> Unit
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
            val isTotalMoreThanVisible = totalItemCount >= COMMENT_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getPostComments(args.post.id, token!!)
                isScrolling = false
            }
        }
    }

    private fun observeGetComments() {
        viewModel.communityComment.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    viewModel.communityCommentResponse?.comments.let {
                        commentAdapter.differ.submitList(it?.toList())
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
    private fun observeAddComment() {
        viewModel.addCommentResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newComment ->

                        // ✅ update the cache first
                        viewModel.communityCommentResponse?.let { cached ->
                            val updatedComments = cached.comments.toMutableList()
                            updatedComments.add(0, newComment.comment)
                            viewModel.communityCommentResponse = cached.copy(comments = updatedComments)
                        }

                        // add the new comment at top of list
                        commentAdapter.addComment(newComment.comment) {
                            binding.rvComments.scrollToPosition(0)
                        }

                        // increment the comment count
                        val current = binding.tvCommentsCount.text
                            .toString().toIntOrNull() ?: 0
                        binding.tvCommentsCount.text = (current + 1).toString()
                    }

                    viewModel.communityPostsResponse?.let { postsResponse ->
                        val updatedList = postsResponse.data.toMutableList()
                        val index = updatedList.indexOfFirst { it.id == args.post.id }
                        if (index != -1) {
                            updatedList[index] = updatedList[index].copy(
                                comments_count = updatedList[index].comments_count + 1
                            )
                            viewModel.communityPostsResponse = postsResponse.copy(data = updatedList)
                        }
                    }

                    viewModel.clearAddCommentState()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showProgressBar()

                else -> Unit
            }
        }
    }

    private fun observeEditComment() {
        viewModel.editCommentResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { updatedComment ->

                        // ✅ update the cache first
                        viewModel.communityCommentResponse?.let { cached ->
                            val updatedComments = cached.comments.toMutableList()
                            updatedComments.add(0, updatedComment.comment)
                            viewModel.communityCommentResponse = cached.copy(comments = updatedComments)
                        }

                        // update just that one comment in the list
                        commentAdapter.updateComment(updatedComment.comment)
                    }

                    viewModel.clearEditCommentState()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showProgressBar()

                else -> Unit
            }
        }
    }

    private fun observeDeleteComment() {
        viewModel.deleteCommentResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()

                    viewModel.communityCommentResponse?.let { cached ->
                        val updatedComments = cached.comments.toMutableList()
                        updatedComments.removeAll { it.id == selectedCommentId }
                        viewModel.communityCommentResponse = cached.copy(comments = updatedComments)
                    }

                    // remove the deleted comment from the list
                    commentAdapter.removeComment(selectedCommentId)
                    selectedCommentId = -1

                    // ✅ decrement the count shown on the post header
                    val current = binding.tvCommentsCount.text
                        .toString().toIntOrNull() ?: 0
                    binding.tvCommentsCount.text = (current - 1).toString()

                    viewModel.communityPostsResponse?.let { postsResponse ->
                        val updatedList = postsResponse.data.toMutableList()
                        val index = updatedList.indexOfFirst { it.id == args.post.id }
                        if (index != -1) {
                            updatedList[index] = updatedList[index].copy(
                                comments_count = updatedList[index].comments_count - 1
                            )
                            viewModel.communityPostsResponse = postsResponse.copy(data = updatedList)
                        }
                    }

                    viewModel.clearDeleteCommentState()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> showProgressBar()

                else -> Unit
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeLikeError(){
        viewModel.likeError.observe(viewLifecycleOwner) { postId ->
            if (postId == args.post.id) {
                // revert the local UI vars
                isLiked = !isLiked
                likesCount += if (isLiked) 1 else -1
                binding.ivLike.isSelected = isLiked
                binding.tvLikesCount.text = likesCount.toString()
            }
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
                selectedCommentId = comment.id!!
                viewModel.deleteComment(token!!, selectedCommentId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setProfileBackground(messageType : String) = when (messageType) {
        TYPE_1 -> R.drawable.avatar_border_type1
        TYPE_2 -> R.drawable.avatar_border_type2
        LADA -> R.drawable.avatar_border_lada
        MODY -> R.drawable.avatar_border_mody
        else ->  R.drawable.avatar_border_gestational
    }

    private fun setCategoryBackground(messageType: String): Triple<Int, Int, Int> = when (messageType) {
        TYPE1_LADA      -> Triple(R.drawable.bg_diabetes_type1,        R.color.profile_type1_stroke,       R.string.category_type1_lada)
        TYPE_2          -> Triple(R.drawable.bg_diabetes_type2,        R.color.profile_type2_stroke,       R.string.category_type2)
        MODY            -> Triple(R.drawable.bg_diabetes_mody,         R.color.profile_mody_stroke,        R.string.category_mody)
        GESTATIONAL     -> Triple(R.drawable.bg_diabetes_gestational,  R.color.profile_gestational_stroke, R.string.category_gestational)
        ADVICES         -> Triple(R.drawable.bg_diabetes_advise,       R.color.advise_gray,                R.string.category_advise)
        else            -> Triple(R.drawable.bg_diabetes_general,      R.color.general_yellow,             R.string.category_general)
    }

    fun getRelativeTime(timestamp: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("Africa/Cairo")

        val date = try { sdf.parse(timestamp) } catch (e: Exception) { return "" } ?: return ""

        val now = Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo"))
        val diffMillis = now.timeInMillis - date.time

        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours   = minutes / 60

        val calNow = Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo"))
        val calDate = Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo")).apply { time = date }

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
                displayFormat.timeZone = TimeZone.getTimeZone("Africa/Cairo")
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