package com.example.seen.ui.home.fragment

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.FragmentHomeBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.community.PostUser
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Log
import com.example.seen.ui.community.fragment.CommunityFragmentDirections
import com.example.seen.ui.home.adapter.HomeAdapter
import com.example.seen.ui.home.viewmodel.HomeViewModel
import com.example.seen.ui.home.viewmodel.HomeViewModelProviderFactory
import com.example.seen.util.Constants.Companion.GESTATIONAL
import com.example.seen.util.Constants.Companion.LADA
import com.example.seen.util.Constants.Companion.MODY
import com.example.seen.util.Constants.Companion.TYPE_1
import com.example.seen.util.Constants.Companion.TYPE_2
import com.example.seen.util.isOnline
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeAdapter: HomeAdapter
    private lateinit var viewModel: HomeViewModel
    private var selectedDate = System.currentTimeMillis()
    private val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    private var token : String? = null
    lateinit var sharedPref : SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()

        setupRecyclerView()
        setupUI()
        getToken()
        setupListeners()
        observeData()
        deleteOnSwipe()
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

    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter(requireContext())

        binding.rvHome.apply {
            isNestedScrollingEnabled = false
            adapter = homeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupUI() {
        binding.chart.apply {
            setNoDataText(getString(R.string.no_data_available))
            setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        setUserInfo()
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
            TYPE_1 -> R.string.type_1
            TYPE_2 -> R.string.type_2
            LADA  -> R.string.type_lada
            MODY  -> R.string.type_mody
            GESTATIONAL -> R.string.type_gestational
            else -> R.string.type_1
        }

    private fun observeData() {

        viewModel.logs.observe(viewLifecycleOwner) { logs ->
            handleLogsState(logs)
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { timestamp ->
            selectedDate = timestamp
            updateSelectedDateText(timestamp)
            updateDateSelectorUi(timestamp)
        }
    }

    private fun handleLogsState(logs: List<FullLog>) {

        val isEmptyLogs = logs.isEmpty()

        binding.rvHome.visibility = if (isEmptyLogs) View.GONE else View.VISIBLE
        binding.llNoLogs.visibility = if (isEmptyLogs) View.VISIBLE else View.GONE

        setUpLineChart(logs)

        homeAdapter.differ.submitList(logs)
    }

    private fun updateSelectedDateText(timestamp: Long) {
        binding.tvChosenDate.text = sdf.format(Date(timestamp))
    }

    private fun updateDateSelectorUi(timestamp: Long) {
        val today = System.currentTimeMillis()
        val yesterday = today - 24 * 60 * 60 * 1000

        val isSameDay = { a: Long, b: Long ->
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            sdf.format(Date(a)) == sdf.format(Date(b))
        }

        resetDateSelector()

        when {
            isSameDay(timestamp, today) -> {
                binding.tvToday.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_active)
                binding.tvToday.setTextColor(requireContext().getColor(R.color.primary))
                binding.tvToday.isEnabled = false
            }
            isSameDay(timestamp, yesterday) -> {
                binding.tvYesterday.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_active)
                binding.tvYesterday.setTextColor(requireContext().getColor(R.color.primary))
                binding.tvYesterday.isEnabled = false
            }
            else -> {
                binding.ivCalendar.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_active)
                binding.ivCalendar.setColorFilter(requireContext().getColor(R.color.primary))
            }
        }
    }

    private fun setupListeners() {

        binding.ivProfile.setOnClickListener {

            viewModel.getUser().observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment(it.id)
                    findNavController().navigate(action)
                }
            }
        }

        binding.cdReminder.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_reminderFragment)
        }

        binding.cdChatbot.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chatbotFragment)
        }

        binding.tvToday.setOnClickListener {
            selectDate(System.currentTimeMillis(), binding.tvToday)
        }

        binding.tvYesterday.setOnClickListener {
            val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            selectDate(yesterday, binding.tvYesterday)
        }

        binding.ivCalendar.setOnClickListener {
            showDatePicker()
        }

        homeAdapter.setOnItemClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToLogDetailFragment(it)
            )
        }
    }

    private fun selectDate(date: Long, selectedView: View) {
        resetDateSelector()

        when (selectedView) {
            is android.widget.TextView -> {
                selectedView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_active)
                selectedView.setTextColor(requireContext().getColor(R.color.primary))
                selectedView.isEnabled = false
            }
            else -> {
                binding.ivCalendar.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_active)
                binding.ivCalendar.setColorFilter(requireContext().getColor(R.color.primary))
            }
        }

        viewModel.selectDate(date)
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

    private fun showDatePicker() {

        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(selectedDate)
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { date ->
            selectedDate = date
            selectDate(date, binding.ivCalendar)
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setUpLineChart(data: List<FullLog>) {

        val chart = binding.chart
        val englishFormat = NumberFormat.getInstance(Locale.ENGLISH)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_legend_dot)
        val areaBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_area_gradient)
        val colorWhite = ContextCompat.getColor(requireContext(), R.color.white)
        val transparentWhite = Color.parseColor("#1AFFFFFF")

        val glucoseData = data
            .sortedBy { it.log.logged_at }
            .mapNotNull { fullLog ->
                fullLog.glucose?.let {
                    fullLog.log.logged_at to it.glucose_level
                }
            }


        val entries = glucoseData.mapIndexed { index, glucose ->
            Entry(index.toFloat() + 1, glucose.second.toFloat(), drawable)
        }

        val dataSet = LineDataSet(entries, "Blood Glucose Level").apply {
            color = colorWhite              //Line color
            lineWidth = 2.5f
            setDrawFilled(true)             // Enable area fill under the line.
            fillDrawable = areaBackground
            setDrawIcons(true)              // Allows drawing icons
            setDrawCircles(false)           // Clean line (no dots)
            setDrawValues(false)            // Hide value labels on points
//          mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curve
        }

        chart.data = LineData(dataSet)

        // --- X Axis ---
        val xLabels = mutableListOf<String>()
        xLabels.add("")

        xLabels.addAll(glucoseData.map {
            val sdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
            sdf.format(Date(it.first))
        })

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(xLabels)   // maps each X value (0,1,2...) to a label from xLabels list
            position = XAxis.XAxisPosition.BOTTOM                       // places the X-axis at the bottom of the chart
            granularity = 1f                                            // ensures labels appear at every 1 unit (no skipping)
            axisMinimum = 0f                                            // sets the minimum X value (start of the axis)
            axisMaximum = (entries.size + 1).toFloat()
            spaceMin = 1f                                               // adds extra empty space before the first entry
            spaceMax = 1f                                               // adds extra empty space after the last entry
            setDrawGridLines(true)                                      // enables vertical grid lines across the chart
            axisLineColor = colorWhite                                  // sets the color of the X-axis line
            axisLineWidth = 1f                                          // sets thickness of the X-axis line
            gridColor = transparentWhite                                // sets color of grid lines (light transparent white)
            textColor = colorWhite                                      // sets color of X-axis labels (time text)
            textSize = 6f                                               // sets size of X-axis label text (small font)
        }

        // --- Y Axis (Left) ---
        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 500f
            granularity = 30f
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

        // --- General Chart Settings ---
        chart.apply {
            description.isEnabled = false   // Hide "Description" label
            legend.isEnabled = false        // Use custom legend in XML
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(Color.TRANSPARENT)
            animateX(400) // Animate on load
        }

        chart.invalidate() // Refresh

    }

    private fun deleteOnSwipe(){

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            var undoClicked = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val fullLog = homeAdapter.differ.currentList[position]

                lifecycleScope.launch {
                    viewModel.deleteLog(fullLog.log)
                }

                Snackbar.make(binding.root, "deleted", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo"){
                        undoClicked = true
                        undoDeletedLog(fullLog)
                    }
                    addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar?, event: Int)
                        {
                            if (!undoClicked && requireContext().isOnline()) {
                                lifecycleScope.launch {
                                    viewModel.syncToServer(token!!)
                                }
                            }
                            undoClicked = false
                        }
                    })
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvHome)
        }
    }

    private fun undoDeletedLog(fullLog : FullLog) {
        viewModel.insertLog(fullLog.log)
        fullLog.glucose?.let { viewModel.insertRecordGlucose(it) }
        fullLog.medication?.let { viewModel.insertRecordMedication(it) }
        fullLog.meal?.let { viewModel.insertRecordMeal(it) }
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
