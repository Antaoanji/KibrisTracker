package com.example.pushuptracker

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

@SuppressLint("ViewConstructor")
class CustomMarker(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val tvContent: TextView? = findViewById(context.resources.getIdentifier("tvContent", "id", context.packageName))

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        tvContent?.text = e?.y?.toInt()?.toString() ?: ""
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}
