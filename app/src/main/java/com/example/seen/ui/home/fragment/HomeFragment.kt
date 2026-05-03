package com.example.seen.ui.home.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seen.R
import com.example.seen.databinding.FragmentHomeBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.home.adapter.HomeAdapter
import com.example.seen.ui.home.viewmodel.HomeViewModel
import com.example.seen.ui.home.viewmodel.HomeViewModelProviderFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {
    var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!
    lateinit var homeAdapter: HomeAdapter

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        setUserInfo()
        setUpLineChart()
        setRecyclerView()



        viewModel.logs.observe(viewLifecycleOwner) { logs ->
                homeAdapter.differ.submitList(logs)
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { timestamp ->
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val formatted = sdf.format(Date(timestamp))

            binding.tvChosenDate.text = formatted
        }

        binding.cdAlert.setOnClickListener {

        }

        binding.cdChatbot.setOnClickListener {

        }

        binding.cdReminder.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_reminderFragment)
        }

        binding.tvToday.setOnClickListener {
            resetDateSelector()
            binding.tvToday.apply {
                background = ContextCompat.getDrawable(requireContext(),R.drawable.bg_tab_active)
                setTextColor(requireContext().getColor(R.color.primary))
                isEnabled = false
            }

            val todayDate = System.currentTimeMillis()
            viewModel.selectDate(todayDate)
        }

        binding.tvYesterday.setOnClickListener {
            resetDateSelector()
            binding.tvYesterday.apply {
                background = ContextCompat.getDrawable(requireContext(),R.drawable.bg_tab_active)
                setTextColor(requireContext().getColor(R.color.primary))
                isEnabled = false
            }

            val yesterdayDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            viewModel.selectDate(yesterdayDate)
        }

        binding.ivCalendar.setOnClickListener {
            showDatePicker()
        }
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

    private fun setUserInfo(){
        viewModel.getUser().observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = getString(R.string.hello_username) + " " + user.first_name
                binding.tvUserDiabetesType.text = getString(setUserDiabetesType(user.diabetes_type))
                binding.tvAlertTitle.text = getString(R.string.alert_title) + " " + user.first_name
            }
        }
    }

    private fun setUserDiabetesType(userDiabetesType: String) =
         when(userDiabetesType){
            "Type1" -> R.string.type_1
            "Type2" -> R.string.type_2
            "LADA"  -> R.string.type_lada
            "MODY"  -> R.string.type_mody
            "Gestational" -> R.string.type_gestational
            else -> R.string.type_1
        }

    private fun resetDateSelector(){
        // reset all buttons and texts
        val textViews = listOf(
            binding.tvToday,
            binding.tvYesterday,
        )

        textViews.forEach {
            it.background = null
            it.setTextColor(requireContext().getColor(R.color.text_grey))
            it.isEnabled = true
        }
        binding.ivCalendar.apply {
            background = null
            setColorFilter(requireContext().getColor(R.color.text_grey))
        }
    }

    private fun setRecyclerView(){
        homeAdapter = HomeAdapter(requireContext())

        binding.rvHome.apply {
            isNestedScrollingEnabled = false
            adapter = homeAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selectedDateMillis ->

            binding.ivCalendar.apply {
                resetDateSelector()
                background = ContextCompat.getDrawable(requireContext(),R.drawable.bg_tab_active)
                setColorFilter(requireContext().getColor(R.color.primary))
            }
            viewModel.selectDate(selectedDateMillis)
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setUpLineChart() {

        val chart = binding.chart
        val englishFormat = NumberFormat.getInstance(Locale.ENGLISH)

        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.primary)
        val colorText = ContextCompat.getColor(requireContext(), R.color.white)

        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_legend_dot)

        val areaBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_area_gradient)

        val entries = listOf(
            Entry(1f, 88f, drawable),   // 8:00
            Entry(2f, 93f, drawable),   // 14:00
            Entry(3f, 85f, drawable),   // 18:00
            Entry(4f, 80f, drawable)    // 23:00
        )

        val dataSet = LineDataSet(entries, "Blood Glucose Level").apply {
            color = Color.parseColor("#FFFFFF") //Line color
            lineWidth = 2.5f
            setDrawFilled(true)
            fillDrawable = areaBackground
            setDrawIcons(true)
            setDrawCircles(false)
            setDrawValues(false) // Hide value labels on points
//            mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curve
        }

        chart.data = LineData(dataSet)

        // --- X Axis ---
        val xLabels = listOf("","8:00", "14:00", "18:00", "23:00")
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(xLabels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            axisMinimum = 0f   // or 3f, 4f, etc.
            spaceMin = 1f
            spaceMax = 1f
            setDrawGridLines(true)
            axisLineColor = Color.parseColor("#FFFFFF")
            axisLineWidth = 1f
            gridColor = Color.parseColor("#1AFFFFFF")
            textColor = colorText
            textSize = 8f
        }

        // --- Y Axis (Left) ---
        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 120f
            granularity = 30f
            spaceMin = 1f
            spaceMax = 1f
            axisLineColor = Color.parseColor("#FFFFFF")
            axisLineWidth = 1f
            setDrawGridLines(true)
            gridColor = Color.parseColor("#1AFFFFFF")
            textColor = colorText
            textSize = 8f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return englishFormat.format(value)
                }
            }
        }

        // --- Disable Right Y Axis ---
        chart.axisRight.isEnabled = false

        // --- General Chart Settings ---
        chart.apply {
            description.isEnabled = false   // Hide "Description" label
            legend.isEnabled = false        // Use custom legend in XML
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setBackgroundColor(Color.TRANSPARENT)
            animateX(1000) // Animate on load
        }

        chart.invalidate() // Refresh

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
