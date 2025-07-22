package info.matthewryan.workoutlogger.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.databinding.ItemActivityBinding
import info.matthewryan.workoutlogger.model.ActivityWithExercise
import info.matthewryan.workoutlogger.model.ExerciseType
import java.util.Locale
import kotlin.math.floor

class SessionActivityAdapter :
    ListAdapter<ActivityWithExercise, SessionActivityAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ActivityWithExercise, position: Int) {
            val setNumber = position + 1
            val exerciseName = item.exercise?.name ?: "Unknown"
            binding.activityTitle.text = "Set $setNumber: $exerciseName"

            val type = item.exercise?.type
            val details = when (type) {
                ExerciseType.CARDIO -> {
                    val distance = item.activity.distance ?: 0.0
                    val durationSeconds = (item.activity.durationInSeconds ?: 0.0).toDouble()
                    val minutes = (durationSeconds / 60).toInt()
                    val seconds = (durationSeconds % 60).toInt()
                    String.format(Locale.getDefault(), "%.2f km in %02d:%02d", distance, minutes, seconds)
                }

                ExerciseType.STRENGTH, null -> {
                    "${item.activity.reps} reps @ ${item.activity.weight} kg"
                }
            }

            binding.activityDetails.text = details
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position)
    }

    class DiffCallback : DiffUtil.ItemCallback<ActivityWithExercise>() {
        override fun areItemsTheSame(old: ActivityWithExercise, new: ActivityWithExercise) =
            old.activity.id == new.activity.id

        override fun areContentsTheSame(old: ActivityWithExercise, new: ActivityWithExercise) =
            old == new
    }
}
