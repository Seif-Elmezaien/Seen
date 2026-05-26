package com.example.seen.ui.community.dialog

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.example.seen.R
import com.example.seen.databinding.FragmentImagePreviewBinding
import com.example.seen.ui.community.adapters.PreviewImageAdapter

class ImagePreviewDialogFragment(
    private val images: MutableList<String>,       // 👈 String
    private val startPosition: Int,
    private val isDeletable: Boolean,
    private val onDelete: ((Int) -> Unit)? = null  // 👈 nullable for feed usage
) : DialogFragment() {

    private lateinit var binding: FragmentImagePreviewBinding
    private lateinit var adapter: PreviewImageAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImagePreviewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = PreviewImageAdapter(requireContext())
        binding.viewPager.adapter = adapter
        adapter.differ.submitList(images.toList())

        binding.btnDelete.visibility = if (isDeletable) View.VISIBLE else View.GONE

        binding.viewPager.setCurrentItem(startPosition, false)
        updateCounter(startPosition, images.size)

        val pageCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateCounter(position, adapter.differ.currentList.size)
            }
        }
        binding.viewPager.registerOnPageChangeCallback(pageCallback)

        binding.btnDelete.setOnClickListener {
            val currentPosition = binding.viewPager.currentItem

            // 1. notify fragment to remove from selectedPhotos
            onDelete?.invoke(currentPosition)

            // 2. remove from local images list
            images.removeAt(currentPosition)

            if (images.isEmpty()) {
                dismiss()
                return@setOnClickListener
            }

            val newPosition = if (currentPosition >= images.size) images.size - 1 else currentPosition

            // 3. unregister before submitList
            binding.viewPager.unregisterOnPageChangeCallback(pageCallback)

            // 4. submit updated list, update UI in callback
            adapter.differ.submitList(images.toList()) {
                binding.viewPager.post {
                    binding.viewPager.setCurrentItem(newPosition, false)
                    updateCounter(newPosition, images.size)
                    binding.viewPager.registerOnPageChangeCallback(pageCallback)
                }
            }
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun updateCounter(position: Int, total: Int) {
        binding.tvIndex.text = getString(R.string.image_counter, position + 1, total)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}