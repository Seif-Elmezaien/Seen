package com.example.seen.ui.community.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.FragmentAddPostBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.community.dialog.ImagePreviewDialogFragment
import com.example.seen.ui.community.viewmodel.CommunityViewModel
import com.example.seen.ui.community.viewmodel.CommunityViewModelProviderFactory
import com.example.seen.util.Constants.Companion.ADVICES
import com.example.seen.util.Constants.Companion.GENERAL
import com.example.seen.util.Constants.Companion.GESTATIONAL
import com.example.seen.util.Constants.Companion.MODY
import com.example.seen.util.Constants.Companion.TYPE1_LADA
import com.example.seen.util.Constants.Companion.TYPE_2
import com.example.seen.util.Resource
import com.google.android.material.chip.Chip
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.sequences.forEach


class AddPostFragment : Fragment() {

    var _binding: FragmentAddPostBinding? = null
    val binding get() = _binding!!

    var token: String? = null

    private lateinit var viewModel: CommunityViewModel
    var selectedCategory = GENERAL

    private val selectedPhotos = mutableListOf<Uri>()

    private val photoPicker = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val remainingSlots = 10 - selectedPhotos.size

            if (remainingSlots <= 0) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.max_images),
                    Toast.LENGTH_SHORT
                ).show()
                return@registerForActivityResult
            }

            val newUris = uris
                .take(remainingSlots)

            selectedPhotos.addAll(newUris)

            updateImageButtonText()
            updatePhotosUI()
        }
    }
    var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getToken()
        initializeViewModel()
        setupListeners()
        handleChips()
        observeAddPost()

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

    private fun setupListeners(){

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnAddImage.setOnClickListener {
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnAddPost.setOnClickListener { submitPost() }
    }

    private fun updateImageButtonText(){
        binding.tvAddImage.text =
            if (selectedPhotos.isEmpty())
                getString(R.string.add_image)
            else
                getString(R.string.add_more_images)
    }

    private fun updatePhotosUI() {
        binding.apply {
            when (selectedPhotos.size) {
                0 -> {
                    ivPhoto1.visibility = View.GONE
                    layoutRow2.visibility = View.GONE
                }
                1 -> {
                    ivPhoto1.visibility = View.VISIBLE
                    layoutRow2.visibility = View.GONE
                    Glide.with(requireContext()).load(selectedPhotos[0]).into(ivPhoto1)
                }
                2 -> {
                    ivPhoto1.visibility = View.VISIBLE
                    layoutRow2.visibility = View.VISIBLE
                    flPhoto3.visibility = View.GONE
                    Glide.with(requireContext()).load(selectedPhotos[0]).into(ivPhoto1)
                    Glide.with(requireContext()).load(selectedPhotos[1]).into(ivPhoto2)
                }
                else -> {
                    ivPhoto1.visibility = View.VISIBLE
                    layoutRow2.visibility = View.VISIBLE
                    flPhoto3.visibility = View.VISIBLE
                    Glide.with(requireContext()).load(selectedPhotos[0]).into(ivPhoto1)
                    Glide.with(requireContext()).load(selectedPhotos[1]).into(ivPhoto2)
                    Glide.with(requireContext()).load(selectedPhotos[2]).into(ivPhoto3)

                    if (selectedPhotos.size > 3) {
                        tvMoreCount.visibility = View.VISIBLE
                        tvMoreCount.text = "+${selectedPhotos.size - 3}"
                    } else {
                        tvMoreCount.visibility = View.GONE
                    }
                }
            }

//            // click to preview
            ivPhoto1.setOnClickListener { openViewer(0) }
            ivPhoto2.setOnClickListener { openViewer(1) }
            ivPhoto3.setOnClickListener { openViewer(2) }
        }
    }

    private fun openViewer(position: Int) {

        val dialog = ImagePreviewDialogFragment(
            images = selectedPhotos,
            startPosition = position,
            isDeletable = true
        ) { deletedPosition ->

            selectedPhotos.removeAt(deletedPosition)

            updateImageButtonText()
            updatePhotosUI()
        }

        dialog.show(parentFragmentManager, "image_viewer")
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
        }
    }

    private fun submitPost() {
        val title    = binding.etPostTitle.text.toString().trim()
        val content  = binding.etPostDescription.text.toString().trim()
        val category = selectedCategory

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val titlePart    = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentPart  = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())

        // convert URIs to MultipartBody.Part for API
        val imageParts = selectedPhotos.mapIndexed { index, uri ->
            uriToMultipart(uri, index)
        }

        viewModel.createPost(token!!, titlePart, contentPart, categoryPart, imageParts)
    }

    private fun uriToMultipart(uri: Uri, index: Int): MultipartBody.Part {
        val contentResolver = requireContext().contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png"  -> "png"
            "image/webp" -> "webp"
            else         -> "jpg"
        }

        val stream = contentResolver.openInputStream(uri)!!
        val bytes  = stream.readBytes()
        stream.close()

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(
            "images[]",                    // match what Laravel expects
            "photo_$index.$extension",
            requestBody
        )
    }

    private fun observeAddPost(){
        viewModel.createPostResult.observe(viewLifecycleOwner) { response ->
            when(response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newPost ->

                        viewModel.insertNewPost(newPost)

                        Toast.makeText(activity, "Post created successfully", Toast.LENGTH_SHORT).show()
                        viewModel.clearCreatePostState()
                        findNavController().popBackStack()
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> showProgressBar()

                else -> hideProgressBar()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}