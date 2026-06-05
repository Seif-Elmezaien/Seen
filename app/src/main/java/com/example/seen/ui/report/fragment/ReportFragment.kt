package com.example.seen.ui.report.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.seen.R
import com.example.seen.databinding.FragmentReportBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.GraphPoint
import com.example.seen.domain.model.entites.ReportStatistics
import com.example.seen.domain.model.entites.ReportFilter
import com.example.seen.ui.report.fragment.GlucoseMarkerView
import com.example.seen.ui.report.viewmodel.ReportViewModel
import com.example.seen.ui.report.viewmodel.ReportViewModelProviderFactory
import com.example.seen.util.Constants.Companion.AFTER_MEAL
import com.example.seen.util.Constants.Companion.BEFORE_MEAL
import com.example.seen.util.Constants.Companion.CUSTOM
import com.example.seen.util.Constants.Companion.FASTING
import com.example.seen.util.Constants.Companion.MONTHLY
import com.example.seen.util.Constants.Companion.RANDOM
import com.example.seen.util.Constants.Companion.WEEKLY
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReportViewModel

    private var fromDate: Long? = null
    private var toDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        setupListeners()

        if (viewModel.fromDate == 0L) {
            setWeeklyPeriod()
        } else {
            restoreUiState()
        }

        observePeriod()
    }

    private fun initializeViewModel() {
        val db            = SeenDatabase(requireContext().applicationContext)
        val logRepository = LogRepository(db)
        val factory       = ReportViewModelProviderFactory(logRepository)

        viewModel = ViewModelProvider(this, factory)[ReportViewModel::class.java]
    }

    private fun setupListeners(){

        binding.tvWeekly.setOnClickListener {
            selectPeriod(binding.tvWeekly)
            setWeeklyPeriod()
        }

        binding.tvMonthly.setOnClickListener {
            selectPeriod(binding.tvMonthly)
            setMonthlyPeriod()
        }

        binding.tvCustom.setOnClickListener { selectPeriod(binding.tvCustom) }

        binding.etDateFrom.setOnClickListener { showDateRangePicker() }

        binding.etDateTo.setOnClickListener { showDateRangePicker() }

        binding.cdLowestRead.setOnClickListener {
            viewModel.statistics.value?.lowestLog.let { fullLog ->
                findNavController().navigate(
                    ReportFragmentDirections.actionReportFragmentToLogDetailFragment(fullLog!!)
                )
            }
        }

        binding.cdHighestRead.setOnClickListener {
            viewModel.statistics.value?.highestLog.let { fullLog ->
                findNavController().navigate(
                    ReportFragmentDirections.actionReportFragmentToLogDetailFragment(fullLog!!)
                )
            }
        }

        binding.tvAll.setOnClickListener {
            selectReadingType(binding.tvAll)
            updateReportFilter(null)
        }

        binding.tvBeforeMeal.setOnClickListener {
            selectReadingType(binding.tvBeforeMeal)
            updateReportFilter(BEFORE_MEAL)
        }

        binding.tvAfterMeal.setOnClickListener {
            selectReadingType(binding.tvAfterMeal)
            updateReportFilter(AFTER_MEAL)
        }

        binding.tvFasting.setOnClickListener {
            selectReadingType(binding.tvFasting)
            updateReportFilter(FASTING)
        }

        binding.tvRandom.setOnClickListener {
            selectReadingType(binding.tvRandom)
            updateReportFilter(RANDOM)
        }

    }

    private fun restoreUiState() {

        when (viewModel.selectedPeriod) {
            WEEKLY ->
                selectPeriod(binding.tvWeekly)

            MONTHLY ->
                selectPeriod(binding.tvMonthly)

            CUSTOM ->
                selectPeriod(binding.tvCustom)
        }

        viewModel.setFilter(
            ReportFilter(
                fromDate = viewModel.fromDate,
                toDate = viewModel.toDate,
                period = viewModel.selectedPeriod,
                readingType = null
            )
        )

        updateDateText()
    }

    private fun updateReportFilter(readingType: String? = null) {

        viewModel.setFilter(
            ReportFilter(
                fromDate = viewModel.fromDate,
                toDate = viewModel.toDate,
                period = viewModel.selectedPeriod,
                readingType = readingType
            )
        )
    }

    private fun setWeeklyPeriod() {

        val today = System.currentTimeMillis()

        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = today
            add(java.util.Calendar.DAY_OF_YEAR, -6)
        }

        fromDate = calendar.timeInMillis
        toDate = today

        updateDateText()

        viewModel.setFilter(
            ReportFilter(
                fromDate = fromDate!!,
                toDate = toDate!!,
                period = WEEKLY,
                readingType = null
            )
        )
    }

    private fun setMonthlyPeriod() {

        val today = System.currentTimeMillis()

        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = today
            add(java.util.Calendar.MONTH, -1)
        }

        fromDate = calendar.timeInMillis
        toDate = today

        updateDateText()

        viewModel.setFilter(
            ReportFilter(
                fromDate = fromDate!!,
                toDate = toDate!!,
                period = MONTHLY,
                readingType = null
            )
        )
    }

    private fun updateDateText() {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        binding.etDateFrom.setText(
            formatter.format(Date(fromDate!!))
        )

        binding.etDateTo.setText(
            formatter.format(Date(toDate!!))
        )
    }

    private fun selectPeriod(selectedView: TextView){
        resetPeriodSelector()

        selectedView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_report_date_tab_active)
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        selectedView.isEnabled = false

        if (selectedView == binding.tvCustom){
            binding.clCustomInputs.visibility = View.VISIBLE
        }
    }

    private fun resetPeriodSelector(){
        // reset all texts
        val textViews = listOf(
            binding.tvWeekly,
            binding.tvMonthly,
            binding.tvCustom
        )

        textViews.forEach {
            it.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_report_date_tab_unactive)
            it.setTextColor(requireContext().getColor(R.color.primary))
            it.isEnabled = true
        }

        binding.clCustomInputs.visibility = View.GONE
    }

    private fun showDateRangePicker() {

        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()

        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select report period")
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->

            fromDate = selection.first
            toDate = selection.second

            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            binding.etDateFrom.setText(
                formatter.format(Date(selection.first))
            )

            binding.etDateTo.setText(
                formatter.format(Date(selection.second))
            )

            viewModel.setFilter(
                ReportFilter(
                    fromDate = fromDate!!,
                    toDate = toDate!!,
                    period = CUSTOM,
                    readingType = null
                )
            )
        }

        picker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun selectReadingType(selectedView: TextView){
        resetReadingType()

        selectedView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_add_log_button_active)
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        selectedView.isEnabled = false
    }

    private fun resetReadingType(){
        val textViews = listOf(
            binding.tvAll,
            binding.tvBeforeMeal,
            binding.tvAfterMeal,
            binding.tvFasting,
            binding.tvRandom
        )

        textViews.forEach {
            it.background = null
            it.setTextColor(requireContext().getColor(R.color.primary))
            it.isEnabled = true
        }
    }

    private fun observePeriod(){

        viewModel.statistics.observe(viewLifecycleOwner) { stats ->
            setupStatsUi(stats)
        }

        viewModel.graphData.observe(viewLifecycleOwner) { graphData ->
            setUpLineChart(graphData)
        }
    }

    private fun setupStatsUi(stats : ReportStatistics){

        binding.tvA1vValue.text =
            stats.estimatedA1C?.let { "%.1f%%".format(it) } ?: "-"

        binding.tvTotalLogsValue.text =
            stats.logsCount.toString()

        binding.tvLowestReadValue.text =
            stats.lowestLog?.glucose?.glucose_level?.toString() ?: "-"

        binding.tvHighestReadValue.text =
            stats.highestLog?.glucose?.glucose_level?.toString() ?: "-"

        binding.tvAverageReadValue.text =
            stats.averageGlucose?.let { "%.0f".format(it) } ?: "-"

        binding.cdHighestRead.alpha = if (stats.highestLog != null) 1f else 0.5f
        binding.cdLowestRead.alpha = if (stats.lowestLog != null) 1f else 0.5f
    }

    private fun setUpLineChart(points: List<GraphPoint>) {

        val chart = binding.chart
        val englishFormat = NumberFormat.getInstance(Locale.ENGLISH)
        val colorWhite = ContextCompat.getColor(requireContext(), R.color.white)
        val transparentWhite = Color.parseColor("#1AFFFFFF")

        if (points.isEmpty()) {
            chart.highlightValues(null)  // ← add this
            chart.clear()
            chart.setNoDataText(getString(R.string.no_data_available))
            chart.setNoDataTextColor(colorWhite)
            chart.invalidate()
            return
        }

        val typeIcons = mapOf(
            FASTING     to ContextCompat.getDrawable(requireContext(), R.drawable.ic_fasting_medium_dot),
            BEFORE_MEAL to ContextCompat.getDrawable(requireContext(), R.drawable.ic_before_meal_medium_dot),
            AFTER_MEAL  to ContextCompat.getDrawable(requireContext(), R.drawable.ic_after_meal_medium_dot),
            RANDOM      to ContextCompat.getDrawable(requireContext(), R.drawable.ic_random_medium_dot)
        )

        val typeColors = mapOf(
            FASTING     to ContextCompat.getColor(requireContext(), R.color.fasting_color),
            BEFORE_MEAL to ContextCompat.getColor(requireContext(), R.color.before_meal_color),
            AFTER_MEAL  to ContextCompat.getColor(requireContext(), R.color.after_meal_color),
            RANDOM      to ContextCompat.getColor(requireContext(), R.color.random_color)
        )

        val sorted = points.sortedBy { it.loggedAt }
        val grouped = sorted.groupBy { it.readingType }

        val globalIndexMap = sorted.mapIndexed { index, p -> p to index }.toMap()

        val dataSets = grouped.entries.map { (type, typePoints) ->
            val entries = typePoints.sortedBy { it.loggedAt }.map { p ->
                Entry(globalIndexMap[p]!!.toFloat(), p.glucoseValue.toFloat(), typeIcons[type])
            }

            LineDataSet(entries, type).apply {
                color = typeColors[type] ?: colorWhite
                lineWidth = 2.5f
                setDrawIcons(true)
                setDrawCircles(false)
                setDrawValues(false)
            }
        }


        val totalPoints = sorted.size
        val labelFormatter = SimpleDateFormat("MMM d", Locale.ENGLISH)

        val labelMap = sorted.mapIndexed { index, p ->
            index to labelFormatter.format(Date(p.loggedAt))
        }.toMap()


        chart.xAxis.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return labelMap[value.roundToInt()] ?: ""
                }
            }
            position = XAxis.XAxisPosition.BOTTOM                       // places the X-axis at the bottom of the chart
            granularity = 1f
            axisMinimum = -0.5f                                         // sets the minimum X value (start of the axis)
            axisMaximum = (totalPoints - 1) + 0.5f
            setDrawGridLines(false)                                      // enables vertical grid lines across the chart
            axisLineColor = colorWhite                                  // sets the color of the X-axis line
            axisLineWidth = 1f                                          // sets thickness of the X-axis line
            gridColor = transparentWhite                                // sets color of grid lines (light transparent white)
            textColor = colorWhite                                      // sets color of X-axis labels (time text)
            textSize = 9f                                               // sets size of X-axis label text (small font)
            isGranularityEnabled = true
        }

        val minY = points.minOf { it.glucoseValue }.toFloat()
        val maxY = points.maxOf { it.glucoseValue }.toFloat()

        // --- Y Axis (Left) ---
        chart.axisLeft.apply {
            axisMinimum = maxOf(0f, minY - 20)
            axisMaximum = maxY + 50
            granularity = ((maxY - minY) / 5).coerceAtLeast(1f)
            spaceMin = 1f
            spaceMax = 1f
            setDrawGridLines(true)
            axisLineColor = colorWhite
            axisLineWidth = 1f
            gridColor = transparentWhite
            textColor = colorWhite
            textSize = 8f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return englishFormat.format(value)
                }
            }
        }

        // --- Disable Right Y Axis ---
        chart.axisRight.isEnabled = false

        val indexToPoint = sorted.mapIndexed { index, p ->
            index to p
        }.toMap()
        // --- General Chart Settings ---
        chart.apply {
            highlightValues(null)  // ← clear any active highlight before swapping data
            data = LineData(dataSets)
            description.isEnabled = false   // Hide "Description" label
            legend.isEnabled = false        // Use custom legend in XML
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(Color.TRANSPARENT)
            animateX(400) // Animate on load

            marker = GlucoseMarkerView(
                requireContext(),
                R.layout.item_marker_glucose,
                indexToPoint
            )
            invalidate ()
        }

        updateLegendVisibility(grouped.keys)
    }

    private fun updateLegendVisibility(activeTypes: Set<String>) {
        val showAll = activeTypes.size > 1
        binding.tvLegendFasting.visibility    = if (showAll || activeTypes.contains(FASTING))     View.VISIBLE else View.GONE
        binding.tvLegendBeforeMeal.visibility = if (showAll || activeTypes.contains(BEFORE_MEAL)) View.VISIBLE else View.GONE
        binding.tvLegendAfterMeal.visibility  = if (showAll || activeTypes.contains(AFTER_MEAL))  View.VISIBLE else View.GONE
        binding.tvLegendRandom.visibility     = if (showAll || activeTypes.contains(RANDOM))      View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}