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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.FragmentProfileBinding
import com.example.seen.domain.model.community.Data
import com.example.seen.domain.model.community.request.EditPostRequest
import com.example.seen.domain.model.profile.ProfileData
import com.example.seen.ui.community.adapters.PostAdapter
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.util.Constants.Companion.ADVICES
import com.example.seen.util.Constants.Companion.GENERAL
import com.example.seen.util.Constants.Companion.GESTATIONAL
import com.example.seen.util.Constants.Companion.LADA
import com.example.seen.util.Constants.Companion.MODY
import com.example.seen.util.Constants.Companion.POST_PAGE_SIZE
import com.example.seen.util.Constants.Companion.TYPE1_LADA
import com.example.seen.util.Constants.Companion.TYPE_1
import com.example.seen.util.Constants.Companion.TYPE_2
import com.example.seen.util.Resource
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.local.SeenDatabase.Companion.invoke
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.ProfileRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.community.viewmodel.CommunityViewModelProviderFactory

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CommunityViewModel
    private val args: ProfileFragmentArgs by navArgs()

    private lateinit var postAdapter: PostAdapter
    private var token: String? = null

    private var isLoading = false
    private var isLastPage = false
    private var isScrolling = false

    // current relation state — updated after each action
    private var currentRelation = ""
    private var profileData: ProfileData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getToken()
        initializeViewModel()
        setupRecyclerView()
        setupPostAdapterCallbacks()

        viewModel.getUserProfile(token!!, args.userId)
        viewModel.getUserPosts(token!!, args.userId, isNewUser = true)

        observeProfile()
        observeUserPosts()
        observeFriendAction()
        observeEditDeletePost()

        viewModel.getUserId().observe(viewLifecycleOwner) { user ->
            postAdapter.userId = user.id
        }
    }

    // ─── Token ───────────────────────────────────────────────────────────────

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

    // ─── RecyclerView ────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(requireContext(), childFragmentManager)
        binding.rvPosts.apply {
            isNestedScrollingEnabled = false
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(scrollListener)
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

            val shouldPaginate = !isLoading && !isLastPage && isScrolling
                    && (firstVisible + visibleCount >= totalCount)
                    && totalCount >= POST_PAGE_SIZE

            if (shouldPaginate) {
                isScrolling = false
                viewModel.getUserPosts(token!!, args.userId)
            }
        }
    }

    // ─── Adapter callbacks ───────────────────────────────────────────────────

    private fun setupPostAdapterCallbacks() {
        postAdapter.setOnLikeClickListener { updatedPost ->
            viewModel.updatePostInCache(updatedPost)
            viewModel.likePost(token!!, updatedPost.id)
        }

        postAdapter.setOnEditClickListener { post ->
            showEditPostDialog(post)
        }

        postAdapter.setOnDeleteClickListener { post ->
            showDeletePostDialog(post)
        }

        postAdapter.setOnCommentClickListener {
            viewModel.communityCommentPage = 1
            viewModel.communityCommentResponse = null
            val action =
                ProfileFragmentDirections
                    .actionProfileFragmentToPostDetailsFragment(it)
            findNavController().navigate(action)
        }
    }

    // ─── Observe ─────────────────────────────────────────────────────────────

    private fun observeProfile() {
        viewModel.profileResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.data?.let { data ->
                        profileData = data
                        currentRelation = data.relation_status
                        bindProfile(data)
                    }
                    viewModel.clearProfileState()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearProfileState()
                }
                else -> Unit
            }
        }
    }

    private fun observeUserPosts() {
        viewModel.userPostsResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> isLoading = true
                is Resource.Success -> {
                    isLoading = false
                    val posts = viewModel.userPostsResponse?.data ?: emptyList()
                    val currentPage = resource.data?.meta?.current_page ?: 1
                    val lastPage = resource.data?.meta?.last_page ?: 1
                    isLastPage = currentPage >= lastPage
                    postAdapter.differ.submitList(posts.toList())
                }
                is Resource.Error -> {
                    isLoading = false
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeFriendAction() {
        viewModel.friendRequestResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    setButtonsEnabled(false)  // ✅ disable all on loading
                }
                is Resource.Success -> {
                    setButtonsEnabled(true)
                    viewModel.getUserProfile(token!!, args.userId)
                    viewModel.clearFriendRequestState()
                }
                is Resource.Error -> {
                    setButtonsEnabled(true)  // ✅ re-enable on error
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearFriendRequestState()
                }
                else -> Unit
            }
        }
    }

    private fun observeEditDeletePost() {
        viewModel.editPostResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { updatedPost ->
                        viewModel.updatePostInCache(updatedPost)
                        val newList = viewModel.userPostsResponse?.data?.toList() ?: emptyList()
                        postAdapter.differ.submitList(null)
                        postAdapter.differ.submitList(newList)
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
                is Resource.Success -> viewModel.clearDeletePostState()
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearDeletePostState()
                }
                else -> Unit
            }
        }
    }

    // ─── Bind profile UI ─────────────────────────────────────────────────────

    private fun bindProfile(data: ProfileData) {
        binding.apply {
            tvUserName.text = data.full_name
            tvFriendsCount.text = data.friends_count.toString()
            tvPostsCount.text = data.posts_count.toString()

            val (bgRes, colorRes, textRes) = getCategoryStyle(data.diabetes_type)
            tvCategory.background = ContextCompat.getDrawable(requireContext(), bgRes)
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
            tvCategory.text = getString(textRes)

            flAvatarStroke.background = ContextCompat.getDrawable(
                requireContext(), getAvatarBorder(data.diabetes_type)
            )

            Glide.with(requireContext())
                .load(data.profile_picture.takeIf { it.isNotEmpty() })
                .placeholder(R.drawable.ic_placeholder_profile)
                .into(ivProfile)

            updateRelationUI(data.relation_status)
        }
    }

    private var activeButton: com.google.android.material.button.MaterialButton? = null

    private fun updateRelationUI(status: String) {
        currentRelation = status
        binding.apply {
            clMyProfile.visibility = View.GONE
            clNotUserProfile.visibility = View.GONE
            btnAddFriend.visibility = View.GONE
            clPendingSent.visibility = View.GONE
            clPendingReceived.visibility = View.GONE
            clFriends.visibility = View.GONE

            when (status) {
                "me" -> clMyProfile.visibility = View.VISIBLE

                "add_friend" -> {
                    clNotUserProfile.visibility = View.VISIBLE
                    btnAddFriend.visibility = View.VISIBLE
                    activeButton = btnAddFriend
                    btnAddFriend.setOnClickListener {
                        btnAddFriend.text = getString(R.string.sending)  // ✅ immediate feedback
                        btnCancel.text = getString(R.string.cancel)
                        viewModel.sendFriendRequest(token!!, args.userId)
                    }
                }

                "pending_sent" -> {
                    clNotUserProfile.visibility = View.VISIBLE
                    clPendingSent.visibility = View.VISIBLE
                    activeButton = btnCancel
                    btnCancel.setOnClickListener {
                        btnCancel.text = getString(R.string.canceling)
                        btnAddFriend.text = getString(R.string.add_friend)
                        viewModel.cancelFriendRequest(token!!, args.userId)
                    }
                }

                "pending_received" -> {
                    clNotUserProfile.visibility = View.VISIBLE
                    clPendingReceived.visibility = View.VISIBLE
                    btnAccept.setOnClickListener {
                        btnAccept.text = getString(R.string.accepting)
                        activeButton = btnAccept
                        viewModel.acceptFriendRequest(token!!, args.userId)
                    }
                    btnReject.setOnClickListener {
                        btnReject.text = getString(R.string.rejecting)
                        activeButton = btnReject
                        viewModel.cancelFriendRequest(token!!, args.userId)
                    }
                }

                "friends" -> {
                    clNotUserProfile.visibility = View.VISIBLE
                    clFriends.visibility = View.VISIBLE
                    activeButton = btnRemove
                    btnRemove.setOnClickListener {
                        btnRemove.text = getString(R.string.removing)
                        viewModel.removeFriend(token!!, args.userId)
                    }
                }

                "blocked" -> {
                    clNotUserProfile.visibility = View.VISIBLE
                    btnBlock.text = getString(R.string.unblock)
                    activeButton = btnBlock
                    btnBlock.setOnClickListener {
                        btnBlock.text = getString(R.string.unblocking)
                        viewModel.unblockFriend(token!!, args.userId)
                    }
                }
            }

            if (status != "me") {
                btnBlock.visibility = View.VISIBLE
                if (status != "blocked") {
                    btnBlock.text = getString(R.string.block)
                    btnBlock.setOnClickListener {
                        btnBlock.text = getString(R.string.blocking)
                        activeButton = btnBlock
                        viewModel.blockFriend(token!!, args.userId)
                    }
                }
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.apply {
            btnAddFriend.isEnabled = enabled
            btnCancel.isEnabled = enabled
            btnAccept.isEnabled = enabled
            btnReject.isEnabled = enabled
            btnFriends.isEnabled = enabled
            btnRemove.isEnabled = enabled
            btnBlock.isEnabled = enabled
        }
    }

    // ─── Dialogs ─────────────────────────────────────────────────────────────

    private fun showEditPostDialog(post: Data) {
        val view = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        val etTitle = EditText(requireContext()).apply { setText(post.title); hint = "Title" }
        val etContent = EditText(requireContext()).apply {
            setText(post.content); hint = "Content"; minLines = 3
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        val categories = listOf(GENERAL, TYPE1_LADA, TYPE_2, MODY, GESTATIONAL, ADVICES)
        val spinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
            setSelection(categories.indexOf(post.category).takeIf { it >= 0 } ?: 0)
        }
        view.addView(etTitle); view.addView(etContent); view.addView(spinner)

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
                val newList = postAdapter.differ.currentList.toMutableList()
                newList.removeAll { it.id == post.id }
                postAdapter.differ.submitList(newList)
                viewModel.deletePost(token!!, post.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Style helpers ───────────────────────────────────────────────────────

    private fun getCategoryStyle(diabetesType: String): Triple<Int, Int, Int> = when (diabetesType) {
        TYPE1_LADA  -> Triple(R.drawable.bg_diabetes_type1,       R.color.profile_type1_stroke,       R.string.category_type1_lada)
        TYPE_2      -> Triple(R.drawable.bg_diabetes_type2,       R.color.profile_type2_stroke,       R.string.category_type2)
        MODY        -> Triple(R.drawable.bg_diabetes_mody,        R.color.profile_mody_stroke,        R.string.category_mody)
        GESTATIONAL -> Triple(R.drawable.bg_diabetes_gestational, R.color.profile_gestational_stroke, R.string.category_gestational)
        ADVICES     -> Triple(R.drawable.bg_diabetes_advise,      R.color.advise_gray,                R.string.category_advise)
        else        -> Triple(R.drawable.bg_diabetes_general,     R.color.general_yellow,             R.string.category_general)
    }

    private fun getAvatarBorder(diabetesType: String) = when (diabetesType) {
        TYPE_1 -> R.drawable.avatar_profile_border_type1
        TYPE_2 -> R.drawable.avatar_profile_border_type2
        LADA   -> R.drawable.avatar_profile_border_lada
        MODY   -> R.drawable.avatar_profile_border_mody
        else   -> R.drawable.avatar_profile_border_gestational
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}