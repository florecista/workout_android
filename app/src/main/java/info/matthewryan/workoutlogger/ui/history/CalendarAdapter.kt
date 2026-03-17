package info.matthewryan.workoutlogger.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.R
import info.matthewryan.workoutlogger.model.CalendarDayItem

class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private val items = mutableListOf<CalendarDayItem>()

    fun submitList(newItems: List<CalendarDayItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.day_text)
        val dot: View = view.findViewById(R.id.dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = items[position]

        holder.dayText.text = item.date.dayOfMonth.toString()

        holder.dayText.alpha = if (item.isCurrentMonth) 1f else 0.3f
        holder.dot.visibility = if (item.hasWorkout) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = items.size
}