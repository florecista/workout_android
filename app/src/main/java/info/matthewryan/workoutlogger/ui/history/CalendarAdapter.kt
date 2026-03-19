package info.matthewryan.workoutlogger.ui.history

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.R
import info.matthewryan.workoutlogger.model.CalendarDayItem
import java.time.LocalDate

class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private val items = mutableListOf<CalendarDayItem>()

    var onDayClick: ((CalendarDayItem) -> Unit)? = null

    var selectedDate: LocalDate? = null

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

        val today = LocalDate.now()
        val isToday = item.date == today

        holder.dayText.text = item.date.dayOfMonth.toString()

        // Dim non-month days
        holder.dayText.alpha = if (item.isCurrentMonth) 1f else 0.3f

        // Dot visibility
        holder.dot.visibility = if (item.hasWorkout) View.VISIBLE else View.GONE

        val isSelected = item.isCurrentMonth && item.date == selectedDate

        val context = holder.itemView.context

        // Text color
        holder.dayText.setTextColor(
            if (isSelected) {
                Color.WHITE
            } else {
                val typedValue = TypedValue()
                context.theme.resolveAttribute(
                    com.google.android.material.R.attr.colorOnSurface,
                    typedValue,
                    true
                )
                context.getColor(typedValue.resourceId)
            }
        )

        // ✅ BACKGROUND LOGIC (NEW)
        holder.dayText.setBackgroundResource(0)

        if (isToday && !isSelected) {
            holder.dayText.setBackgroundResource(R.drawable.calendar_today_bg)
        }

        if (isSelected) {
            holder.dayText.setBackgroundResource(R.drawable.calendar_selected_bg)
        }

        holder.itemView.setOnClickListener {
            onDayClick?.invoke(item)
        }
    }

    override fun getItemCount() = items.size
}