package com.example.seen.ui.reminder.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seen.R
import com.example.seen.databinding.FragmentReminderBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.MedicineRepository
import com.example.seen.datasource.repository.ReminderRepository
import com.example.seen.domain.model.entites.Reminder
import com.example.seen.ui.reminder.viewmodel.ReminderViewModel
import com.example.seen.ui.reminder.viewmodel.ReminderViewModelProviderFactory
import com.example.seen.ui.reminder.adapter.ReminderAdapter
import com.example.seen.ui.reminder.broadcast.ReminderScheduler

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
        setupListeners()
    }

    private fun initializeViewModel(){
        // Application context to avoid leaks
        val db = SeenDatabase.Companion(requireContext().applicationContext)
        val reminderRepository = ReminderRepository(db)
        val medicineRepository = MedicineRepository(db)

        // create factory
        val factory = ReminderViewModelProviderFactory(
            requireActivity().application,
            reminderRepository,
            medicineRepository
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

    private fun setupListeners() {
        binding.btnAddReminder.setOnClickListener {
            findNavController().navigate(R.id.action_reminderFragment_to_addReminderFragment)
        }

        reminderAdapter.setOnDeleteClickListener { reminder ->

            ReminderScheduler.cancelReminder(
                requireContext(),
                reminder.reminder_id
            )

            viewModel.deleteReminder(reminder)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}