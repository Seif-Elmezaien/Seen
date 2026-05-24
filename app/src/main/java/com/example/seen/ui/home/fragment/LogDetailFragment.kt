package com.example.seen.ui.home.fragment

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.seen.R
import com.example.seen.databinding.FragmentLogDetailBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.ui.home.viewmodel.HomeViewModel
import com.example.seen.ui.home.viewmodel.HomeViewModelProviderFactory
import com.example.seen.util.Constants.Companion.HIGH_GLUCOSE_VALUE
import com.example.seen.util.Constants.Companion.LOW_GLUCOSE_VALUE
import com.example.seen.util.isOnline
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LogDetailFragment : Fragment() {

    private var _binding: FragmentLogDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private val sdfDate = SimpleDateFormat("MMM d, yyyy, hh:mm a", Locale.getDefault())

    private val args: LogDetailFragmentArgs by navArgs()
    private lateinit var fullLog: FullLog

    private var token : String? = null
    lateinit var sharedPref : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullLog = args.FullLog
        initializeViewModel()
        observeLog()
        setupUi()
        getToken()
        setupListeners()

    }

    private fun initializeViewModel(){
        // Application context to avoid leaks
        val db = SeenDatabase(requireContext().applicationContext)
        val userRepository = UserRepository(db)
        val logRepository = LogRepository(db)

        // create factory
        val factory = HomeViewModelProviderFactory(
            requireActivity().application,
            userRepository,
            logRepository
        )

        // initialize ViewModel
        viewModel = ViewModelProvider(this, factory)
            .get(HomeViewModel::class.java)
    }

    private fun observeLog(){
        viewModel.getLogById(fullLog.log.log_id).observe(viewLifecycleOwner) { updatedLog ->
            if (updatedLog != null) {
                fullLog = updatedLog
                setupUi()
            }
        }
    }

    private fun setupUi(){

        setupHeader(fullLog.log)
        setupGlucose(fullLog.glucose)
        setupMedication(fullLog.medication)
        setupMeal(fullLog.meal)
    }

    private fun setupListeners(){
        binding.btnEditLog.setOnClickListener {
            findNavController().navigate(
                LogDetailFragmentDirections.actionLogDetailFragmentToEditLogsFragment(fullLog)
            )
        }

        binding.btnDeleteLog.setOnClickListener {
            showDeleteLogDialog()
        }
    }

    private fun setupHeader(log : Log){

        binding.tvLogDate.text = sdfDate.format(Date(log.logged_at))
        binding.tvLogTitle.text = log.log_title
        binding.tvLogDescription.text = log.log_description
    }

    private fun setupGlucose(glucose: RecordGlucose?) {

        if (glucose == null) {
            binding.mcvGlucose.visibility = View.GONE
            return
        }

        binding.mcvGlucose.visibility = View.VISIBLE

        val (bgColorRes, textColorRes) = getGlucoseStyle(glucose.glucose_level)

        binding.tvGlucoseLevel.text = "${glucose.glucose_level} mg/dl"

        binding.tvGlucoseLevel.setTextColor(
            ContextCompat.getColor(requireContext(), textColorRes)
        )

        binding.sectionGlucoseLevel.background =
            ContextCompat.getDrawable(requireContext(), bgColorRes)

        binding.tvReadingType.text = glucose.reading_type

        // A1C
        if (glucose.a1c_estimation == null) {
            binding.sectionA1c.visibility = View.GONE
        } else {
            binding.sectionA1c.visibility = View.VISIBLE
            binding.tvA1c.text = glucose.a1c_estimation.toString()
        }

        // Notes
        if (glucose.notes.isNullOrBlank()) {
            binding.sectionGlucoseNotes.visibility = View.GONE
        } else {
            binding.sectionGlucoseNotes.visibility = View.VISIBLE
            binding.tvGlucoseNotes.text = glucose.notes
        }
    }

    private fun getGlucoseStyle(value: Int): Pair<Int, Int> = when {
        value in LOW_GLUCOSE_VALUE..HIGH_GLUCOSE_VALUE -> R.color.good_sugar_reading_10 to R.color.good_sugar_reading
        else -> R.color.bad_sugar_reading_10 to R.color.bad_sugar_reading
    }

    private fun setupMedication(medication: RecordMedication?) {

        if (medication == null) {
            binding.mcvMedication.visibility = View.GONE
            return
        }

        binding.mcvMedication.visibility = View.VISIBLE

        val chipGroup = binding.chipGroupMedications
        chipGroup.removeAllViews()


        medication.medications.forEach { selectedMedication ->
            val chip = createChipStyle(selectedMedication.medication_name, chipGroup)
            chipGroup.addView(chip)
        }

        // Notes
        if (medication.notes.isNullOrBlank()) {
            binding.sectionMedicationNotes.visibility = View.GONE
        } else {
            binding.sectionMedicationNotes.visibility = View.VISIBLE
            binding.tvMedicationNotes.text = medication.notes
        }
    }

    private fun createChipStyle(selectedMedicine: String, chipGroup: ChipGroup) : Chip {
        return Chip(requireContext()).apply {
            text = selectedMedicine
            isCloseIconVisible = false
            isClickable = false
            isCheckable = false
            typeface = ResourcesCompat.getFont(requireContext(), R.font.cairo_medium)
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
            closeIconSize = 24f
            closeIconEndPadding = 16f

            chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_30))
            shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(resources.displayMetrics.density * 38) // 38dp to px
                .build()
        }
    }

    private fun setupMeal(meal: RecordMeal?) {

        if (meal == null) {
            binding.mcvMeal.visibility = View.GONE
            return
        }

        binding.mcvMeal.visibility = View.VISIBLE


        binding.tvMealType.text = meal.meal_type

        binding.tvMealDescription.text = meal.meal_description

        // total carbs
        if (meal.total_carb == null) {
            binding.sectionMealCarbs.visibility = View.GONE
        } else {
            binding.sectionMealCarbs.visibility = View.VISIBLE
            binding.tvMealCarbs.text = meal.total_carb.toString()
        }

        // total calories
        if (meal.total_calories == null) {
            binding.sectionMealCalories.visibility = View.GONE
        } else {
            binding.sectionMealCalories.visibility = View.VISIBLE
            binding.tvMealCalories.text = meal.total_calories.toString()
        }

        // Notes
        if (meal.notes.isNullOrBlank()) {
            binding.sectionMealNotes.visibility = View.GONE
        } else {
            binding.sectionMealNotes.visibility = View.VISIBLE
            binding.tvMealNotes.text = meal.notes
        }
    }

    private fun showDeleteLogDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.alert_dialog_title_log))
            .setMessage(getString(R.string.alert_dialog_message_log))
            .setPositiveButton(getString(R.string.alert_dialog_positive_button_log)) { _, _ ->
                viewModel.deleteLog(fullLog.log)

                if (requireContext().isOnline()){
                    viewModel.syncToServer(token!!)
                }

                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.alert_dialog_negative_button_log), null)
            .show()
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