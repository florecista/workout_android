package info.matthewryan.workoutlogger.ui.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.databinding.ItemPersonalBestBinding
import info.matthewryan.workoutlogger.model.ExerciseType
import info.matthewryan.workoutlogger.model.PersonalBest
import java.text.SimpleDateFormat
import java.util.*

class PersonalBestAdapter : RecyclerView.Adapter<PersonalBestAdapter.ViewHolder>() {

    private var bestList: List<PersonalBest> = emptyList()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun submitList(list: List<PersonalBest>) {
        bestList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPersonalBestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = bestList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bestList[position])
    }

    inner class ViewHolder(private val binding: ItemPersonalBestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PersonalBest) {
            val type = item.exercise?.type ?: ExerciseType.STRENGTH
            when (type) {
                ExerciseType.STRENGTH -> {
                    binding.textOne.text = "${item.weight} kg"
                    binding.textTwo.text = "${item.reps} reps"
                }
                ExerciseType.CARDIO -> {
                    val minutes = (item.duration?.toInt() ?: 0) / 60
                    val seconds = (item.duration?.toInt() ?: 0) % 60
                    binding.textOne.text = "${item.distance} km"
                    binding.textTwo.text = String.format("%02d:%02d", minutes, seconds)
                }
            }

            binding.textDate.text = dateFormat.format(Date(item.timestamp))
        }
    }
}
