package com.example.seen.ui.home.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.seen.R
import com.example.seen.databinding.FragmentAddReminderBinding
import com.example.seen.databinding.FragmentReminderBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.local.SeenDatabase.Companion.invoke
import com.example.seen.datasource.repository.ReminderRepository
import com.example.seen.ui.home.fragment.AddLogsFragment.LogType
import com.example.seen.ui.home.viewmodel.ReminderViewModel
import com.example.seen.ui.home.viewmodel.ReminderViewModelProviderFactory
import com.google.android.material.button.MaterialButton

class AddReminderFragment : Fragment() {
    var _binding: FragmentAddReminderBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: ReminderViewModel
    private enum class LogType { GLUCOSE, MEDICATION, MEAL }
    private var activeLogType = LogType.GLUCOSE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        setupListeners()
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


    private fun setupListeners() {

        binding.ivBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.btnGlucose.setOnClickListener { switchLogType(LogType.GLUCOSE) }

        binding.btnMedication.setOnClickListener { switchLogType(LogType.MEDICATION) }

        binding.btnMeal.setOnClickListener { switchLogType(LogType.MEAL) }
        
        binding.btnAddReminder.setOnClickListener {  }
    }

    private fun switchLogType(type: LogType) {
        if (activeLogType == type) return
        activeLogType = type

        setButtonActive(binding.btnGlucose, type == LogType.GLUCOSE)
        setButtonActive(binding.btnMedication, type == LogType.MEDICATION)
        setButtonActive(binding.btnMeal, type == LogType.MEAL)

        binding.clMedication.visibility = if (type == LogType.MEDICATION) View.VISIBLE else View.GONE
        binding.tvReminderTitle.visibility = if (type == LogType.MEDICATION) View.GONE else View.VISIBLE
        binding.etReminderTitle.visibility = if (type == LogType.MEDICATION) View.GONE else View.VISIBLE

    }

    private fun setButtonActive(button: MaterialButton, isActive: Boolean) {
        val bgDrawable = if (isActive) R.drawable.bg_add_log_button_active else R.drawable.bg_add_log_button_inactive
        val color = if (isActive) R.color.white else R.color.primary

        button.background = ContextCompat.getDrawable(requireContext(), bgDrawable)
        button.setTextColor(ContextCompat.getColor(requireContext(), color))
        button.iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}