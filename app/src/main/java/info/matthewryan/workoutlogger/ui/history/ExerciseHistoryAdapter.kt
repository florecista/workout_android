package info.matthewryan.workoutlogger.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.databinding.ItemExerciseHistoryActivityBinding
import info.matthewryan.workoutlogger.databinding.ItemExerciseHistoryDateBinding

class ExerciseHistoryAdapter :
    ListAdapter<ExerciseHistoryRow, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_DATE = 0
        private const val TYPE_ACTIVITY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ExerciseHistoryRow.DateHeader -> TYPE_DATE
            is ExerciseHistoryRow.ActivityItem -> TYPE_ACTIVITY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_DATE -> {
                val binding =
                    ItemExerciseHistoryDateBinding.inflate(inflater, parent, false)
                DateViewHolder(binding)
            }

            else -> {
                val binding =
                    ItemExerciseHistoryActivityBinding.inflate(inflater, parent, false)
                ActivityViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = getItem(position)) {

            is ExerciseHistoryRow.DateHeader ->
                (holder as DateViewHolder).bind(item)

            is ExerciseHistoryRow.ActivityItem ->
                (holder as ActivityViewHolder).bind(item)
        }
    }

    class DateViewHolder(
        private val binding: ItemExerciseHistoryDateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExerciseHistoryRow.DateHeader) {
            binding.dateHeader.text = item.date
        }
    }

    class ActivityViewHolder(
        private val binding: ItemExerciseHistoryActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExerciseHistoryRow.ActivityItem) {

            binding.setTitle.text =
                "${item.setIndex}. ${item.exerciseName}"

            binding.setTime.text =
                item.time

            binding.setWeight.text =
                "${item.weight} kg × ${item.reps}"

            binding.prStar.visibility =
                if (item.isPr) View.VISIBLE else View.GONE
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExerciseHistoryRow>() {

        override fun areItemsTheSame(
            oldItem: ExerciseHistoryRow,
            newItem: ExerciseHistoryRow
        ) = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: ExerciseHistoryRow,
            newItem: ExerciseHistoryRow
        ) = oldItem == newItem
    }
}