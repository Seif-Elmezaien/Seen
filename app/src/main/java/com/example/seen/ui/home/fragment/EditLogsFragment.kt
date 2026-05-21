package com.example.seen.ui.home.fragment

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.seen.R
import com.example.seen.databinding.FragmentAddLogsBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.MedicineRepository
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.Medicine
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.SelectedMedication
import com.example.seen.ui.home.viewmodel.AddLogsViewModel
import com.example.seen.ui.home.viewmodel.AddLogsViewModelProviderFactory
import com.example.seen.util.Constants.Companion.HIGH_GLUCOSE_VALUE
import com.example.seen.util.Constants.Companion.LOW_GLUCOSE_VALUE
import com.example.seen.util.isOnline
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.getValue

class EditLogsFragment : Fragment() {
    private var _binding: FragmentAddLogsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel : AddLogsViewModel
    private val selectedCalendar = Calendar.getInstance()
    private val sdfDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())

    private enum class LogType { GLUCOSE, MEDICATION, MEAL }
    private var activeLogType = LogType.GLUCOSE
    private var selectedMeasurementType: String? = null
    private var selectedMealType: String? = null

    private val medicineList = mutableListOf<SelectedMedication>()

    private val args: EditLogsFragmentArgs by navArgs()
    private lateinit var fullLog: FullLog

    private var token : String? = null
    lateinit var sharedPref : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullLog = args.FullLog

        initializeViewModel()
        setupUi()
        getToken()
        setupListeners()
    }

    private fun initializeViewModel(){
        // Application context to avoid leaks
        val db = SeenDatabase(requireContext().applicationContext)
        val logRepository = LogRepository(db)
        val medicineRepository = MedicineRepository(db)


        // create factory
        val factory = AddLogsViewModelProviderFactory(
            requireActivity().application,
            logRepository,
            medicineRepository
        )

        // initialize ViewModel
        viewModel = ViewModelProvider(this, factory)
            .get(AddLogsViewModel::class.java)
    }

    private fun setupUi() {
        updateHeaderUi()
        updateGlucoseUi(fullLog.glucose)
        updateMedicationUi(fullLog.medication)
        updateMealUi(fullLog.meal)
        binding.btnAddNewLog.text = getString(R.string.edit_log)
    }

    private fun updateHeaderUi(){
        binding.etLogTitle.setText(fullLog.log.log_title)
        binding.etLogDescription.setText(fullLog.log.log_description)
        selectedCalendar.time = Date(fullLog.log.logged_at)
        updateDateText()
        updateTimeText()
    }

    private fun updateGlucoseUi(glucose: RecordGlucose?){
        if (glucose == null) {
            return
        }

        selectedMeasurementType = glucose.reading_type

        // ← add this
        val glucoseButtons = mapOf(
            "Random" to binding.tvGlucoseRandom,
            "Before Meal" to binding.tvGlucoseBeforeMeal,
            "After Meal" to binding.tvGlucoseAfterMeal,
            "Fasting" to binding.tvGlucoseFasting
        )
        glucoseButtons[selectedMeasurementType]?.let { setTypeButtonActive(it) }

        binding.etGlucoseLogValue.setText(glucose.glucose_level.toString())
        binding.etGlucoseLogValue.background = ContextCompat.getDrawable(
            requireContext(),
            getSugarStyle(glucose.glucose_level)
        )


        if (glucose.a1c_estimation != null){
            binding.etA1c.setText(glucose.a1c_estimation.toString())
        }
        if (glucose.notes != null){
            binding.etGlucoseNotes.setText(glucose.notes)
        }
    }

    private fun updateMedicationUi(medication: RecordMedication?){
        if (medication == null) {
            return
        }

        medicineList.addAll(medication.medications)

        if (medication.notes != null){
            binding.etMedicationNotes.setText(medication.notes)
        }
    }

    private fun updateMealUi(meal: RecordMeal?){
        if(meal == null){
            return
        }

        selectedMealType = meal.meal_type

        // ← add this
        val mealButtons = mapOf(
            "Breakfast" to binding.tvBreakfast,
            "Lunch" to binding.tvLunch,
            "Dinner" to binding.tvDinner,
            "Snack" to binding.tvSnack
        )
        mealButtons[meal.meal_type]?.let { setTypeButtonActive(it) }

        binding.etMealDescription.setText(meal.meal_description)

        if (meal.total_carb != null){
            binding.etCarbs.setText(meal.total_carb.toString())
        }

        if (meal.total_calories != null)(
            binding.etCalories.setText(meal.total_calories.toString())
        )

        if (meal.notes != null){
            binding.etMealNotes.setText(meal.notes)
        }
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

        binding.etGlucoseLogValue.addTextChangedListener{ editable ->
            val value = editable?.toString()?.toIntOrNull()

            binding.etGlucoseLogValue.background = ContextCompat.getDrawable(
                requireContext(),
                getSugarStyle(value)
            )
        }

        setupMedicationDropdown()
        binding.btnAddMedication.setOnClickListener { setUpBottomSheet() }

        binding.btnAddNewLog.setOnClickListener { handleInput() }
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
            val now = Calendar.getInstance()
            val isToday =
                selectedCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                selectedCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

            if (isToday && (picker.hour > now.get(Calendar.HOUR_OF_DAY) ||
                        (picker.hour == now.get(Calendar.HOUR_OF_DAY) && picker.minute > now.get(Calendar.MINUTE)))) {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                selectedCalendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
            } else {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
                selectedCalendar.set(Calendar.MINUTE, picker.minute)
            }

            updateTimeText()
        }

        picker.show(parentFragmentManager, "TIME_PICKER")
    }

    private fun showDatePicker() {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
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
        val buttons = mapOf(
            binding.tvGlucoseRandom to "Random",
            binding.tvGlucoseBeforeMeal to "Before Meal",
            binding.tvGlucoseAfterMeal to "After Meal",
            binding.tvGlucoseFasting to "Fasting"
        )

        buttons.forEach { (button, englishValue) ->
            button.setOnClickListener {
                binding.tvMeasurementType.error = null

                if (button.tag == "active") {
                    setTypeButtonInactive(button)
                    selectedMeasurementType = null
                } else {
                    buttons.keys.forEach { setTypeButtonInactive(it) }
                    setTypeButtonActive(button)
                    selectedMeasurementType = englishValue
                }
            }
        }
    }

    private fun setupMealTypeSelection() {
        val buttons = mapOf(
            binding.tvBreakfast to "Breakfast",
            binding.tvLunch to "Lunch",
            binding.tvDinner to "Dinner",
            binding.tvSnack to "Snack"
        )

        buttons.forEach { (button, englishValue) ->
            button.setOnClickListener {
                binding.tvMealType.error = null

                if (button.tag == "active") {
                    setTypeButtonInactive(button)
                    selectedMealType = null
                } else {
                    buttons.keys.forEach { setTypeButtonInactive(it) }
                    setTypeButtonActive(button)
                    selectedMealType = englishValue
                }
            }
        }
    }

    private fun setTypeButtonActive(button: TextView) {
        button.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_add_log_button_active)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        button.tag = "active"
    }

    private fun setTypeButtonInactive(button: TextView) {
        button.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_add_log_button_inactive)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        button.tag = null
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

        val etMedication = view.findViewById<TextInputEditText>(R.id.etNewMedicationName)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnSaveNewMedication)

        btnConfirm.setOnClickListener {
            val name = etMedication.text?.trim().toString()

            if (name.isEmpty()) {
                etMedication.error = getString(R.string.please_fill_medication_name)
                etMedication.requestFocus()
            } else {
                viewModel.insertMedicine(Medicine(medicine_name = name))
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
            mutableListOf<String>())

        medicineDropdown.setAdapter(adapter)

        viewModel.getAllMedicines().observe(viewLifecycleOwner) { medicines ->
            val names = medicines.map { it.medicine_name }
            adapter.clear()
            adapter.addAll(names)
            adapter.notifyDataSetChanged()
            medicineDropdown.setText("", false) // reset without triggering filter
            medicineDropdown.setAdapter(adapter) // re-attach to reset internal filter state
        }

        medicineDropdown.setOnClickListener {
            binding.tvMedicationTitle.error = null

            if (medicineDropdown.tag == null){
                medicineDropdown.showDropDown()
                medicineDropdown.tag = "active"
            } else {
                medicineDropdown.dismissDropDown()
                medicineDropdown.tag = null
            }
        }

        medicineDropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedMedicine = parent.getItemAtPosition(position).toString()

            // prevent duplicates
            val isDuplicate = (0 until chipGroup.childCount).any { i ->
                (chipGroup.getChildAt(i) as Chip).text == selectedMedicine
            }

            if (!isDuplicate) {
                medicineList.add(SelectedMedication(selectedMedicine))
                val chip = createChipStyle(selectedMedicine, chipGroup)
                chipGroup.addView(chip)
            }

            medicineDropdown.setText("", false) // reset without triggering filter
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
                medicineList.removeIf { it.medication_name == selectedMedicine }
            }
        }
    }

    private fun handleInput() {
        if (!validateCommonFields()) return
        if (!validateGlucoseSection()) return
        if (!validateMedicationSection()) return
        if (!validateMealSection()) return

        val hasAnySectionFilled = hasGlucoseFilled() || hasMedicationFilled() || hasMealFilled()

        if (!hasAnySectionFilled) {
            Toast.makeText(requireContext(), R.string.please_fill_at_least_one_section, Toast.LENGTH_SHORT).show()
            return
        }

        saveLog()
    }

// ─── Validation ───────────────────────────────────────────────────

    private fun validateCommonFields(): Boolean {
        val title = binding.etLogTitle.text.toString().trim()
        val description = binding.etLogDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etLogTitle.error = getString(R.string.please_fill_log_title)
            binding.etLogTitle.requestFocus()
            return false
        }

        if (description.isEmpty()) {
            binding.etLogDescription.error = getString(R.string.please_fill_log_description)
            binding.etLogDescription.requestFocus()
            return false
        }

        return true
    }

    private fun validateGlucoseSection(): Boolean {
        val hasValue = binding.etGlucoseLogValue.text.toString().toIntOrNull() != null
        val hasType = selectedMeasurementType != null
        val hasNullable = binding.etA1c.text.toString().toFloatOrNull() != null
                || binding.etGlucoseNotes.text.toString().trim().isNotEmpty()

        if ((hasNullable || hasValue) && !hasType) {
            binding.tvMeasurementType.error = getString(R.string.please_select_measurement_type)
            switchLogType(LogType.GLUCOSE)
            return false
        }

        if ((hasNullable || hasType) && !hasValue) {
            binding.etGlucoseLogValue.error = getString(R.string.please_fill_glucose_value)
            binding.etGlucoseLogValue.requestFocus()
            switchLogType(LogType.GLUCOSE)
            return false
        }

        return true
    }

    private fun validateMedicationSection(): Boolean {
        val hasNullable = binding.etMedicationNotes.text.toString().trim().isNotEmpty()

        if (hasNullable && medicineList.isEmpty()) {
            binding.tvMedicationTitle.error = getString(R.string.please_select_medication)
            switchLogType(LogType.MEDICATION)
            return false
        }

        return true
    }

    private fun validateMealSection(): Boolean {
        val hasDescription = binding.etMealDescription.text.toString().trim().isNotEmpty()
        val hasType = selectedMealType != null
        val hasNullable = binding.etCarbs.text.toString().toIntOrNull() != null
                || binding.etCalories.text.toString().toIntOrNull() != null
                || binding.etMealNotes.text.toString().trim().isNotEmpty()

        if ((hasNullable || hasDescription) && !hasType) {
            binding.tvMealType.error = getString(R.string.please_select_meal_type)
            switchLogType(LogType.MEAL)
            return false
        }

        if ((hasNullable || hasType) && !hasDescription) {
            binding.etMealDescription.error = getString(R.string.please_fill_meal_description)
            binding.etMealDescription.requestFocus()
            switchLogType(LogType.MEAL)
            return false
        }

        return true
    }

// ─── State checks ─────────────────────────────────────────────────

    private fun hasGlucoseFilled() =
        binding.etGlucoseLogValue.text.toString().toIntOrNull() != null || selectedMeasurementType != null

    private fun hasMedicationFilled() = medicineList.isNotEmpty()

    private fun hasMealFilled() =
        selectedMealType != null || binding.etMealDescription.text.toString().trim().isNotEmpty()

// ─── Save ─────────────────────────────────────────────────────────

    private fun saveLog() {
        lifecycleScope.launch {
            val log = Log(
                log_id = fullLog.log.log_id,
                log_title = binding.etLogTitle.text.toString().trim(),
                log_description = binding.etLogDescription.text.toString().trim(),
                logged_at = selectedCalendar.timeInMillis
            )
            viewModel.insertLog(log)
            val logId = log.log_id

            if (hasGlucoseFilled()) insertGlucoseRecord(logId)
            if (hasMedicationFilled()) insertMedicationRecord(logId)
            if (hasMealFilled()) insertMealRecord(logId)

            if (requireContext().isOnline()){
                viewModel.syncToServer(token!!)
            }

            findNavController().popBackStack()
        }
    }

    private suspend fun insertGlucoseRecord(logId: String) {
        viewModel.insertRecordGlucose(
            RecordGlucose(
                reading_id = 0,
                log_id = logId,
                reading_type = selectedMeasurementType!!,
                glucose_level = binding.etGlucoseLogValue.text.toString().toInt(),
                a1c_estimation = binding.etA1c.text.toString().toFloatOrNull(),
                notes = binding.etGlucoseNotes.text.toString().trim()
            )
        )
    }

    private suspend fun insertMedicationRecord(logId: String) {
        viewModel.insertRecordMedication(
            RecordMedication(
                medication_id = 0,
                log_id = logId,
                medications = medicineList,
                notes = binding.etMedicationNotes.text.toString().trim()
            )
        )
    }

    private suspend fun insertMealRecord(logId: String) {
        viewModel.insertRecordMeal(
            RecordMeal(
                meal_id = 0,
                log_id = logId,
                meal_type = selectedMealType!!,
                meal_description = binding.etMealDescription.text.toString().trim(),
                total_carb = binding.etCarbs.text.toString().toIntOrNull(),
                total_calories = binding.etCalories.text.toString().toIntOrNull(),
                notes = binding.etMealNotes.text.toString().trim()
            )
        )
    }

    private fun getToken() {
        sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}