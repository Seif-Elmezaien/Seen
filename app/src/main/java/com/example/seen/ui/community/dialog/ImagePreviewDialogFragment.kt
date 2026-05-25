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
    private val images: MutableList<Uri>,
    private val startPosition: Int,
    private val isDeletable: Boolean,
    private val onDelete: (Int) -> Unit
) : DialogFragment() {

    private lateinit var binding: FragmentImagePreviewBinding

    private lateinit var adapter: PreviewImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentImagePreviewBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = PreviewImageAdapter(requireContext())
        binding.viewPager.adapter = adapter
        adapter.differ.submitList(images.toList())

        binding.btnDelete.visibility = if (isDeletable) View.VISIBLE else View.GONE   // 👈 renamed



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

            onDelete(currentPosition)

            // 2. build new list locally so we control the size
            val newList = images.toList()  // images already updated by onDelete

            if (newList.isEmpty()) {
                dismiss()
                return@setOnClickListener
            }

            val newPosition = if (currentPosition >= newList.size) newList.size - 1 else currentPosition

            binding.viewPager.unregisterOnPageChangeCallback(pageCallback)

            // 3. submit new list, THEN update UI after differ finishes
            adapter.differ.submitList(newList) {
                // this callback fires AFTER differ finishes — size is now correct ✅
                binding.viewPager.post {
                    binding.viewPager.setCurrentItem(newPosition, false)
                    updateCounter(newPosition, newList.size)  // 👈 pass size directly, don't read from differ
                    // 👇 re-register after everything is done
                    binding.viewPager.registerOnPageChangeCallback(pageCallback)
                }
            }
        }


        binding.btnClose.setOnClickListener { dismiss() }
    }

    // 👇 takes size as parameter instead of reading from differ (avoids async race)
    private fun updateCounter(position: Int, total: Int) {
        binding.tvIndex.text = getString(R.string.image_counter, position + 1, total)
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}