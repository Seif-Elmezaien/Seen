package com.example.seen.ui.community.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seen.databinding.ItemPostCommentLikesBottomSheetBinding
import com.example.seen.ui.community.adapters.LikesAdapter
import com.example.seen.ui.community.fragment.PostDetailsFragmentDirections
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.util.Resource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LikesBottomSheetFragment(
    private val token: String,
    private val postId: Int? = null,
    private val commentId: Int? = null
) : BottomSheetDialogFragment() {

    private var _binding: ItemPostCommentLikesBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by activityViewModels()
    private lateinit var likesAdapter: LikesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ItemPostCommentLikesBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeLikes()

        when {
            postId != null    -> viewModel.getPostLikes(token, postId)
            commentId != null -> viewModel.getCommentLikes(token, commentId)
        }
    }

    private fun setupRecyclerView() {
        likesAdapter = LikesAdapter(requireContext())
        binding.rvLikes.apply {
            adapter = likesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        likesAdapter.setOnSearchResultClickListener {
            dismiss()
            val action = PostDetailsFragmentDirections.actionPostDetailsFragmentToProfileFragment(it.id!!)
            findNavController().navigate(action)
        }
    }

    private fun observeLikes() {
        if (postId != null) {
            viewModel.postLikesResult.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> likesAdapter.differ.submitList(resource.data)
                    is Resource.Error   -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    else -> Unit
                }
            }
        } else {
            viewModel.commentLikesResult.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> likesAdapter.differ.submitList(resource.data)
                    is Resource.Error   -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    else -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearPostLikesResultState()
        viewModel.clearCommentLikesResultState()
        _binding = null
    }
}