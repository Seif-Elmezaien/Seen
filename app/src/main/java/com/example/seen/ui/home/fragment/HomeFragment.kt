package com.example.seen.ui.home.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.seen.R
import com.example.seen.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.Locale


class HomeFragment : Fragment() {
    var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpLineChart()
    }

    private fun setUpLineChart() {

        val chart = binding.chart
        val englishFormat = NumberFormat.getInstance(Locale.ENGLISH)

        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.primary)
        val colorText = ContextCompat.getColor(requireContext(), R.color.grey)

        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_legend_dot)

        val areaBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_area_gradient)

        val entries = listOf(
            Entry(1f, 88f, drawable),   // 8:00
            Entry(2f, 93f, drawable),   // 14:00
            Entry(3f, 85f, drawable),   // 18:00
            Entry(4f, 80f, drawable)    // 23:00
        )

        val dataSet = LineDataSet(entries, "Blood Glucose Level").apply {
            color = colorPrimary //Line color
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
            axisLineColor = Color.parseColor("#A6A6A6")
            axisLineWidth = 1f
            gridColor = Color.parseColor("#828898")
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
            axisLineColor = Color.parseColor("#A6A6A6")
            axisLineWidth = 1f
            setDrawGridLines(true)
            gridColor = Color.parseColor("#828898")
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
            setBackgroundColor(Color.WHITE)
            animateX(1000) // Animate on load
        }

        chart.invalidate() // Refresh

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
