package com.example.seen.ui.reminder.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.seen.R
import com.example.seen.databinding.FragmentAddReminderBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.MedicineRepository
import com.example.seen.datasource.repository.ReminderRepository
import com.example.seen.domain.model.entites.Medicine
import com.example.seen.domain.model.entites.Reminder
import com.example.seen.domain.model.entites.SelectedMedication
import com.example.seen.ui.reminder.viewmodel.ReminderViewModel
import com.example.seen.ui.reminder.viewmodel.ReminderViewModelProviderFactory
import com.example.seen.ui.reminder.broadcast.ReminderScheduler
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddReminderFragment : Fragment() {
    var _binding: FragmentAddReminderBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: ReminderViewModel
    private val selectedCalendar = Calendar.getInstance()
    private val sdfDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())

    private enum class ReminderType { glucose, medication, meal }
    private var activeReminderType = ReminderType.glucose
    private var medicine: SelectedMedication? = null

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
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        updateTimeText()
        updateDateText()
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


    private fun setupListeners() {

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnGlucose.setOnClickListener { switchLogType(ReminderType.glucose) }

        binding.btnMedication.setOnClickListener { switchLogType(ReminderType.medication) }

        binding.btnMeal.setOnClickListener { switchLogType(ReminderType.meal) }

        binding.etReminderTime.setOnClickListener { showTimePicker() }

        binding.etReminderDate.setOnClickListener { showDatePicker() }

        setupMedicationDropdown()

        binding.btnAddMedication.setOnClickListener { setUpBottomSheet() }

        binding.btnAddReminder.setOnClickListener { handleInput() }
    }

    private fun updateTimeText() {
        binding.etReminderTime.setText(sdfTime.format(selectedCalendar.time))
    }

    private fun updateDateText() {
        binding.etReminderDate.setText(sdfDate.format(selectedCalendar.time))
    }

    private fun switchLogType(type: ReminderType) {
        if (activeReminderType == type) return
        activeReminderType = type

        setButtonActive(binding.btnGlucose, type == ReminderType.glucose)
        setButtonActive(binding.btnMedication, type == ReminderType.medication)
        setButtonActive(binding.btnMeal, type == ReminderType.meal)

        binding.clMedication.visibility = if (type == ReminderType.medication) View.VISIBLE else View.GONE
    }

    private fun setButtonActive(button: MaterialButton, isActive: Boolean) {
        val bgDrawable = if (isActive) R.drawable.bg_add_log_button_active else R.drawable.bg_add_log_button_inactive
        val color = if (isActive) R.color.white else R.color.primary

        button.background = ContextCompat.getDrawable(requireContext(), bgDrawable)
        button.setTextColor(ContextCompat.getColor(requireContext(), color))
        button.iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTitleText("Select time")
            .setHour(selectedCalendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(selectedCalendar.get(Calendar.MINUTE))
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .build()

        picker.addOnPositiveButtonClickListener {
            val now = Calendar.getInstance()

            selectedCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
            selectedCalendar.set(Calendar.MINUTE, picker.minute)
            selectedCalendar.set(Calendar.SECOND, 0)
            selectedCalendar.set(Calendar.MILLISECOND, 0)

            // Prevent past time only if selected date is today
            val isToday =
                selectedCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        selectedCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

            if (isToday && selectedCalendar.before(now)) {
                selectedCalendar.timeInMillis = now.timeInMillis
                selectedCalendar.set(Calendar.SECOND, 0)
                selectedCalendar.set(Calendar.MILLISECOND, 0)
            }

            updateTimeText()
        }

        picker.show(parentFragmentManager, "TIME_PICKER")
    }

    private fun showDatePicker() {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())// minimum is today
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(selectedCalendar.timeInMillis)
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { timestamp ->

            val picked = Calendar.getInstance().apply {
                timeInMillis = timestamp
            }

            selectedCalendar.set(Calendar.YEAR, picked.get(Calendar.YEAR))
            selectedCalendar.set(Calendar.MONTH, picked.get(Calendar.MONTH))
            selectedCalendar.set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))

            selectedCalendar.set(Calendar.SECOND, 0)
            selectedCalendar.set(Calendar.MILLISECOND, 0)

            updateDateText()
            updateTimeText()
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setUpBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_add_medication_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        bottomSheetDialog.show()

        val etMedication = view.findViewById<TextInputEditText>(R.id.etNewMedicationName)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnSaveNewMedication)

        btnConfirm.setOnClickListener {
            val name = etMedication.text?.trim().toString()

            if (name.isEmpty()) {
                etMedication.error = getString(R.string.please_fill_medication_name)
                etMedication.requestFocus()
            } else {
                viewModel.insertMedicine(Medicine( medicine_name = name))
                bottomSheetDialog.dismiss()
            }
        }
    }

    private fun setupMedicationDropdown() {

        val medicineDropdown = binding.etMedicineDropdown
        val chipGroup = binding.chipGroupMedications

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )

        medicineDropdown.setAdapter(adapter)

        viewModel.getAllMedicines().observe(viewLifecycleOwner) { medicines ->

            val names = medicines.map { it.medicine_name }

            adapter.clear()
            adapter.addAll(names)
            adapter.notifyDataSetChanged()
        }

        medicineDropdown.setOnClickListener {

            binding.tvMedicationTitle.error = null

            if (medicineDropdown.tag == null) {
                medicineDropdown.showDropDown()
                medicineDropdown.tag = "active"
            } else {
                medicineDropdown.dismissDropDown()
                medicineDropdown.tag = null
            }
        }

        medicineDropdown.setOnItemClickListener { parent, _, position, _ ->

            val selectedMedicine =
                parent.getItemAtPosition(position).toString()

            // allow only one chip
            chipGroup.removeAllViews()

            medicine = SelectedMedication(selectedMedicine)

            val chip = createChipStyle(selectedMedicine, chipGroup)

            chipGroup.addView(chip)

            medicineDropdown.setText("", false)
            medicineDropdown.clearFocus()
            medicineDropdown.tag = null
        }
    }

    private fun createChipStyle(selectedMedicine: String, chipGroup: ChipGroup) : Chip {
        return Chip(requireContext()).apply {
            text = selectedMedicine
            isCloseIconVisible = true
            isClickable = false
            isCheckable = false

            typeface = ResourcesCompat.getFont(requireContext(), R.font.cairo_medium)
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
            closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_x)
            closeIconSize = 24f
            closeIconEndPadding = 16f

            chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_30))
            chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
            chipStrokeWidth = resources.displayMetrics.density * 1 // 1dp to px
            shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(resources.displayMetrics.density * 38) // 38dp to px
                .build()

            setOnCloseIconClickListener {
                chipGroup.removeView(this)
                medicine = null
            }
        }
    }

    private fun handleInput(){
        val title = binding.etReminderTitle.text.toString().trim()

        if (title.isEmpty()) {
            binding.etReminderTitle.error = getString(R.string.please_fill_log_title)
            binding.etReminderTitle.requestFocus()
            return
        }

        lifecycleScope.launch {
            val reminder = Reminder(
                0,
                activeReminderType.toString(),
                title,
                selectedCalendar.timeInMillis,
                medicine?.medication_name,
                "Still"
            )
            val reminderId = viewModel.insertReminder(reminder).toInt()

            ReminderScheduler.scheduleReminder(
                requireContext(),
                reminderId,
                activeReminderType.toString(),
                reminder.message,
                reminder.time,
                reminder.medication_name
            )
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}