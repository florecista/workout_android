package info.matthewryan.workoutlogger.ui.progress

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import info.matthewryan.workoutlogger.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChartMarkerView(
    context: Context,
    private val data: List<Pair<Long, Double>>
) : MarkerView(context, R.layout.view_chart_marker) {

    private val textWeight: TextView = findViewById(R.id.textWeight)
    private val textDate: TextView = findViewById(R.id.textDate)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val index = e?.x?.toInt() ?: return
        val (timestamp, weight) = data[index]

        textWeight.text = String.format("%.1f kg", weight)

        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(timestamp))
        textDate.text = date

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}