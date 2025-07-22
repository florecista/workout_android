package info.matthewryan.workoutlogger.ui.progress

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentProgressDetailBinding
import info.matthewryan.workoutlogger.model.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ProgressDetailFragment : Fragment() {

    private var _binding: FragmentProgressDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var exercise: Exercise
    private lateinit var adapter: PersonalBestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressDetailBinding.inflate(inflater, container, false)
        exercise = ProgressDetailFragmentArgs.fromBundle(requireArguments()).exercise

        binding.textExerciseName.text = exercise.name

        adapter = PersonalBestAdapter()
        binding.recyclerViewPersonalBests.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPersonalBests.adapter = adapter

        loadVolumeData()

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    private fun loadVolumeData() {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).activityDao()
            val volumePerSessions = withContext(Dispatchers.IO) {
                dao.getVolumePerSessionForExercise(exercise.id)
            }

            val entries = volumePerSessions.mapIndexed { index, session ->
                Entry(index.toFloat(), session.totalVolume.toFloat())
            }

            val dataSet = LineDataSet(entries, "Volume per Session")
            dataSet.setDrawValues(false)
            dataSet.setDrawCircles(true)

            binding.lineChart.data = LineData(dataSet)

            // Format X-axis labels with conditional formatting
            val dateLabels = volumePerSessions.map {
                val calendar = Calendar.getInstance().apply { timeInMillis = it.date }
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR) % 100

                if (month == Calendar.JANUARY) {
                    String.format("%02d Jan '%02d", day, year) // e.g. 16 Jan '25
                } else {
                    String.format("%02d %s", day, SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time))
                }
            }

            binding.lineChart.xAxis.apply {
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dateLabels)
            }

            binding.lineChart.invalidate()

            // Populate personal best list
            val personalBests = withContext(Dispatchers.IO) {
                dao.getPersonalBestsForExercise(exercise.id)
            }
            adapter.submitList(personalBests)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
