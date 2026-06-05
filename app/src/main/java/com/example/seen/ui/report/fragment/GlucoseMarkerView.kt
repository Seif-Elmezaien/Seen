package com.example.seen.ui.report.fragment

import android.content.Context
import android.widget.TextView
import com.example.seen.R
import com.example.seen.domain.model.entites.GraphPoint
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlucoseMarkerView(
    context: Context,
    layoutResource: Int,
    private val indexToPoint: Map<Int, GraphPoint>
) : MarkerView(context, layoutResource) {

    private val tvValue: TextView = findViewById(R.id.tvValue)
    private val tvDate: TextView = findViewById(R.id.tvDate)

    private val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.ENGLISH)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val index = e?.x?.toInt()
        val point = index?.let { indexToPoint[it] }

        if (point != null) {
            tvValue.text = "${point.glucoseValue} mg/dL"
            tvDate.text = dateFormat.format(Date(point.loggedAt))
        } else {
            tvValue.text = "${e?.y?.toInt()} mg/dL"
            tvDate.text = ""
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat() - 10f)
    }
}