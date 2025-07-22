package info.matthewryan.workoutlogger.ui.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.databinding.ItemProgressBinding
import info.matthewryan.workoutlogger.model.Exercise

class ExerciseAdapter(private val onClick: (Exercise) -> Unit) :
    ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = getItem(position)
        holder.bind(exercise)
    }

    inner class ExerciseViewHolder(private val binding: ItemProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: Exercise) {
            // Use data binding to bind the exercise object to the layout
            binding.exercise = exercise
            binding.root.setOnClickListener { onClick(exercise) }
        }
    }

    class ExerciseDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }
}
