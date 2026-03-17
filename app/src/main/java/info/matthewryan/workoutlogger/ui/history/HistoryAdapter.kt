package info.matthewryan.workoutlogger.ui.history

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onDeleteClick: (SessionDisplay) -> Unit,
    private val onItemClick: (SessionDisplay) -> Unit
) : ListAdapter<SessionDisplay, HistoryAdapter.SessionViewHolder>(SessionDiffCallback()) {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sessionTitle: TextView = itemView.findViewById(R.id.session_title)
        val sessionDate: TextView = itemView.findViewById(R.id.session_date)
        val exerciseList: TextView = itemView.findViewById(R.id.exercise_list)
        val deleteButton: View = itemView.findViewById(R.id.delete_button)
        val foregroundLayout: View = itemView.findViewById(R.id.foreground_layout) // ✅ add this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session_history, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val item = getItem(position)

        val date = Date(item.session.startTimestamp)

        // LEFT: title
        holder.sessionTitle.text = "Workout"

        // RIGHT: date (you’ll need a new TextView for this)
        val formatter = SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault())
        holder.sessionDate.text = formatter.format(date)

        // BELOW: exercises (comma-separated like HeavySet)
        holder.exerciseList.text = item.exerciseNames.joinToString(", ")

        // ✅ Attach to foreground layout only
        holder.foregroundLayout.setOnClickListener {
            Log.d("HistoryAdapter", "Clicked session: ${item.session.id}")
            onItemClick(item)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(item)
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}
