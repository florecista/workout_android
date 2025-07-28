package info.matthewryan.workoutlogger.ui.exercises

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import info.matthewryan.workoutlogger.databinding.ItemExerciseBinding
import info.matthewryan.workoutlogger.model.Exercise

class ExerciseAdapter(
    private val onClick: (Exercise) -> Unit,
    private val onDelete: (Exercise) -> Unit // NEW
) : ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding =
            ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = getItem(position)
        viewBinderHelper.bind(holder.binding.swipeLayout, exercise.id.toString()) // ensure stable id
        holder.bind(exercise)
    }

    inner class ExerciseViewHolder(val binding: ItemExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: Exercise) {
            binding.exercise = exercise
            binding.foregroundLayout.setOnClickListener { onClick(exercise) }
            binding.deleteButton.setOnClickListener { onDelete(exercise) }
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
