package info.matthewryan.workoutlogger.ui.sessionlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import info.matthewryan.workoutlogger.R
import info.matthewryan.workoutlogger.model.ActivityWithExercise

class SessionLogAdapter(
    private var activities: List<ActivityWithExercise>,
    private val onDelete: (ActivityWithExercise) -> Unit
) : RecyclerView.Adapter<SessionLogAdapter.ViewHolder>() {

    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = activities.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = activities[position]

        // Uniquely identify each swipe layout by Activity ID
        viewBinderHelper.bind(holder.swipeLayout, item.activity.id.toString())

        val setIndex = position + 1
        val exerciseName = item.exercise?.name ?: "Unknown"
        val title = "Set $setIndex: $exerciseName"

        holder.sessionTitle.text = title
        holder.exerciseList.text = "${item.activity.reps} reps @ ${item.activity.weight} kg"

        holder.deleteButton.setOnClickListener {
            onDelete(item)
        }
    }

    fun updateData(newData: List<ActivityWithExercise>) {
        this.activities = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val swipeLayout = view.findViewById<com.chauthai.swipereveallayout.SwipeRevealLayout>(R.id.swipe_layout)
        val sessionTitle = view.findViewById<TextView>(R.id.activity_title)        // 🔁 changed
        val exerciseList = view.findViewById<TextView>(R.id.activity_details)     // 🔁 changed
        val deleteButton = view.findViewById<Button>(R.id.delete_button)
    }

}
