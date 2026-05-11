package com.example.seen.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seen.R
import com.example.seen.databinding.FragmentReminderBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.local.SeenDatabase.Companion.invoke
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.ReminderRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Reminder
import com.example.seen.ui.home.adapter.HomeAdapter
import com.example.seen.ui.home.adapter.ReminderAdapter
import com.example.seen.ui.home.viewmodel.HomeViewModel
import com.example.seen.ui.home.viewmodel.HomeViewModelProviderFactory
import com.example.seen.ui.home.viewmodel.ReminderViewModel
import com.example.seen.ui.home.viewmodel.ReminderViewModelProviderFactory
import kotlinx.coroutines.launch

class ReminderFragment : Fragment() {
    var _binding: FragmentReminderBinding? = null
    val binding get() = _binding!!

    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var viewModel: ReminderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        setupRecyclerView()
        observeData()

        binding.btnAddReminder.setOnClickListener {
            findNavController().navigate(R.id.action_reminderFragment_to_addReminderFragment)
        }

    }

    private fun initializeViewModel(){
        // Application context to avoid leaks
        val db = SeenDatabase(requireContext().applicationContext)
        val reminderRepository = ReminderRepository(db)

        // create factory
        val factory = ReminderViewModelProviderFactory(
            requireActivity().application,
            reminderRepository,
        )

        // initialize ViewModel
        viewModel = ViewModelProvider(this, factory)
            .get(ReminderViewModel::class.java)
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter(requireContext())

        binding.rvReminder.apply {
            isNestedScrollingEnabled = true
            adapter = reminderAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeData() {
        viewModel.getAllReminders().observe(viewLifecycleOwner) { reminders ->
            handleRemindersState(reminders)
        }
    }

    private fun handleRemindersState(reminders: List<Reminder>) {
        val isEmptyLogs = reminders.isEmpty()

        binding.rvReminder.visibility = if (isEmptyLogs) View.GONE else View.VISIBLE
        binding.llNoReminders.visibility = if (isEmptyLogs) View.VISIBLE else View.GONE

        if (!isEmptyLogs){
            reminderAdapter.differ.submitList(reminders)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}