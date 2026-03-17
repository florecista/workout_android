package info.matthewryan.workoutlogger.ui.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.databinding.ItemPersonalBestBinding
import info.matthewryan.workoutlogger.databinding.ItemProgressHeaderBinding
import info.matthewryan.workoutlogger.model.ExerciseType
import info.matthewryan.workoutlogger.model.PersonalBest
import java.text.SimpleDateFormat
import java.util.*

class PersonalBestAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ProgressListItem> = emptyList()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    fun submitList(list: List<PersonalBest>) {
        items = groupByReps(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProgressListItem.Header -> TYPE_HEADER
            is ProgressListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemProgressHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemPersonalBestBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProgressListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ProgressListItem.Item -> (holder as ItemViewHolder).bind(item.personalBest)
        }
    }

    // ----------------------------
    // ViewHolders
    // ----------------------------

    inner class HeaderViewHolder(private val binding: ItemProgressHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProgressListItem.Header) {
            binding.textHeader.text = "${item.reps} Rep Max"
        }
    }

    inner class ItemViewHolder(private val binding: ItemPersonalBestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PersonalBest) {
            val type = item.exercise?.type ?: ExerciseType.STRENGTH

            when (type) {
                ExerciseType.STRENGTH -> {
                    binding.textOne.text = "${item.weight} kg × ${item.reps}"
                }
                ExerciseType.CARDIO -> {
                    val minutes = (item.duration?.toInt() ?: 0) / 60
                    val seconds = (item.duration?.toInt() ?: 0) % 60
                    binding.textOne.text = "${item.distance} km"
                }
            }

            binding.textDate.text = dateFormat.format(Date(item.timestamp))
        }
    }

    // ----------------------------
    // Grouping Logic
    // ----------------------------

    private fun groupByReps(list: List<PersonalBest>): List<ProgressListItem> {
        val grouped = list
            .filter { it.reps != null }
            .groupBy { it.reps!! }
            .toSortedMap() // sorts by reps ascending

        val result = mutableListOf<ProgressListItem>()

        for ((reps, items) in grouped) {
            result.add(ProgressListItem.Header(reps))

            // pick best (highest weight) OR keep all
            val sortedItems = items.sortedByDescending { it.weight ?: 0.0 }

            sortedItems.forEach {
                result.add(ProgressListItem.Item(it))
            }
        }

        return result
    }
}