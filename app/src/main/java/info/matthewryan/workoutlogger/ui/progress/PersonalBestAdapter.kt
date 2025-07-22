package info.matthewryan.workoutlogger.ui.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.databinding.ItemPersonalBestBinding
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
            binding.textReps.text = "${item.reps} reps"
            binding.textWeight.text = "${item.weight} kg"
            binding.textDate.text = dateFormat.format(Date(item.timestamp))
        }
    }
}
