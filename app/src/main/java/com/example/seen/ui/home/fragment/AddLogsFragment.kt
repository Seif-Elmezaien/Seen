package com.example.seen.ui.home.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.seen.R
import com.example.seen.databinding.FragmentAddLogsBinding
import com.example.seen.util.Constants.Companion.HIGH_GLUCOSE_VALUE
import com.example.seen.util.Constants.Companion.LOW_GLUCOSE_VALUE
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddLogsFragment : Fragment() {
    private var _binding: FragmentAddLogsBinding? = null
    private val binding get() = _binding!!

    private val selectedCalendar = Calendar.getInstance()
    private val sdfDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())

    private enum class LogType { GLUCOSE, MEDICATION, MEAL }
    private var activeLogType = LogType.GLUCOSE
    private var selectedMeasurementType: String? = null
    private var selectedMealType: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        setupListeners()
    }

    private fun setupUi() {
        updateDateText()
        updateTimeText()
    }

    private fun updateDateText() {
        binding.etLogDate.setText(sdfDate.format(selectedCalendar.time))
    }

    private fun updateTimeText() {
        binding.etLogTime.setText(sdfTime.format(selectedCalendar.time))
    }

    private fun setupListeners() {

        binding.etLogTime.setOnClickListener { showTimePicker() }

        binding.etLogDate.setOnClickListener { showDatePicker() }

        binding.btnGlucose.setOnClickListener { switchLogType(LogType.GLUCOSE) }

        binding.btnMedication.setOnClickListener { switchLogType(LogType.MEDICATION) }

        binding.btnMeal.setOnClickListener { switchLogType(LogType.MEAL) }

        setupGlucoseTypeSelection()
        setupMealTypeSelection()

        binding.btnAddMedication.setOnClickListener {

        }

        binding.etGlucoseLogValue.addTextChangedListener{ editable ->
            val value = editable?.toString()?.toIntOrNull()

            binding.etGlucoseLogValue.background = ContextCompat.getDrawable(requireContext(), getSugarStyle(value))
        }

        binding.btnAddMedication.setOnClickListener { setUpBottomSheet() }

        binding.btnAddNewLog.setOnClickListener {
        }
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTitleText("Select time")
            .setHour(selectedCalendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(selectedCalendar.get(Calendar.MINUTE))
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()

        picker.addOnPositiveButtonClickListener {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
            selectedCalendar.set(Calendar.MINUTE, picker.minute)
            updateTimeText()
        }

        picker.show(parentFragmentManager, "TIME_PICKER")
    }

    private fun showDatePicker() {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(selectedCalendar.timeInMillis)
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { timestamp ->
            val picked = Calendar.getInstance().apply { timeInMillis = timestamp }
            selectedCalendar.set(Calendar.YEAR, picked.get(Calendar.YEAR))
            selectedCalendar.set(Calendar.MONTH, picked.get(Calendar.MONTH))
            selectedCalendar.set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))
            updateDateText()
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun switchLogType(type: LogType) {
        if (activeLogType == type) return
        activeLogType = type

        setButtonActive(binding.btnGlucose, type == LogType.GLUCOSE)
        setButtonActive(binding.btnMedication, type == LogType.MEDICATION)
        setButtonActive(binding.btnMeal, type == LogType.MEAL)

        binding.clGlucose.visibility = if (type == LogType.GLUCOSE) View.VISIBLE else View.GONE
        binding.clMedication.visibility = if (type == LogType.MEDICATION) View.VISIBLE else View.GONE
        binding.clMeal.visibility = if (type == LogType.MEAL) View.VISIBLE else View.GONE
    }

    private fun setButtonActive(button: MaterialButton, isActive: Boolean) {
        val bgDrawable = if (isActive) R.drawable.bg_add_log_button_active else R.drawable.bg_add_log_button_inactive
        val color = if (isActive) R.color.white else R.color.primary

        button.background = ContextCompat.getDrawable(requireContext(), bgDrawable)
        button.setTextColor(ContextCompat.getColor(requireContext(), color))
        button.iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    private fun setupGlucoseTypeSelection() {
        val buttons = listOf(
            binding.tvGlucoseRandom,
            binding.tvGlucoseBeforeMeal,
            binding.tvGlucoseAfterMeal,
            binding.tvGlucoseFasting
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                buttons.forEach { setTypeButtonInactive(it) }
                setTypeButtonActive(button)
                selectedMeasurementType = button.text.toString()
            }
        }
    }

    private fun setupMealTypeSelection() {
        val buttons = listOf(
            binding.tvBreakfast,
            binding.tvLunch,
            binding.tvDinner,
            binding.tvSnack
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                buttons.forEach { setTypeButtonInactive(it) }
                setTypeButtonActive(button)
                selectedMealType = button.text.toString()
            }
        }
    }

    private fun setTypeButtonActive(button: TextView) {
        button.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_add_log_button_active)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun setTypeButtonInactive(button: TextView) {
        button.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_add_log_button_inactive)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
    }

    private fun getSugarStyle(value: Int?): Int = when {
        value == null -> R.drawable.bg_et_add_logs
        value in LOW_GLUCOSE_VALUE..HIGH_GLUCOSE_VALUE -> R.drawable.bg_et_add_logs_good_reading
        else -> R.drawable.bg_et_add_logs_bad_reading
    }

    private fun setUpBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_add_medication_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        bottomSheetDialog.show()

        // access views directly
        val etMedication = view.findViewById<TextInputEditText>(R.id.etNewMedicationName)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnSaveNewMedication)

        btnConfirm.setOnClickListener {
            val name = etMedication.text?.toString()
            // do something with it
            bottomSheetDialog.dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}