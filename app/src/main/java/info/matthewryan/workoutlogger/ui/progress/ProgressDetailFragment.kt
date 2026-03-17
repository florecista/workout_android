package info.matthewryan.workoutlogger.ui.progress

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.R
import info.matthewryan.workoutlogger.databinding.FragmentProgressDetailBinding
import info.matthewryan.workoutlogger.model.Exercise
import info.matthewryan.workoutlogger.dao.ExerciseRepository.calculatePersonalBests
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

        // ✅ Default selection
        binding.toggleChartMode.check(R.id.buttonStrength)

        // ✅ Toggle behaviour
        binding.toggleChartMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.buttonStrength -> loadStrengthData()
                R.id.buttonVolume -> loadVolumeData()
            }
        }

        // Initial load
        loadStrengthData()

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    private fun loadStrengthData() {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).activityDao()

            val activities = withContext(Dispatchers.IO) {
                dao.getActivitiesForExercise(exercise.id)
            }

            val sortedActivities = activities.sortedBy { it.timestamp }

            val maxPerSession = sortedActivities
                .groupBy { it.timestamp }
                .map { (timestamp, acts) ->
                    val maxWeight = acts
                        .mapNotNull { it.weight }
                        .filter { it > 20 } // ignore warmups
                        .maxOrNull() ?: 0.0

                    Pair(timestamp, maxWeight)
                }
                .sortedBy { it.first }

            val progressiveMax = mutableListOf<Pair<Long, Double>>()
            var currentMax = 0.0

            for ((timestamp, weight) in maxPerSession) {
                if (weight > currentMax) {
                    currentMax = weight
                    progressiveMax.add(timestamp to weight)
                }
            }

            val entries = progressiveMax.mapIndexed { index, (_, weight) ->
                Entry(index.toFloat(), weight.toFloat())
            }

            val dataSet = LineDataSet(entries, "Max Weight")

            val lineColor = ContextCompat.getColor(
                requireContext(),
                com.google.android.material.R.color.design_default_color_primary
            )

            // Styling
            dataSet.setDrawValues(false)
            dataSet.setDrawCircles(true)
            dataSet.setDrawCircleHole(true)
            dataSet.setDrawFilled(false)

            dataSet.lineWidth = 2f

            dataSet.circleRadius = 6f
            dataSet.circleHoleRadius = 3f

            dataSet.color = lineColor
            dataSet.setCircleColor(Color.WHITE)
            dataSet.setCircleHoleColor(lineColor)

            // Highlight latest point
            val lastIndex = entries.lastIndex
            dataSet.setCircleColors(
                entries.mapIndexed { index, _ ->
                    if (index == lastIndex) Color.YELLOW else Color.WHITE
                }
            )

            binding.lineChart.clear()
            binding.lineChart.data = LineData(dataSet)

            val textColor = ContextCompat.getColor(
                requireContext(),
                com.google.android.material.R.color.material_on_surface_emphasis_medium
            )

            val gridColor = ContextCompat.getColor(
                requireContext(),
                com.google.android.material.R.color.material_on_surface_disabled
            )

            val dateLabels = progressiveMax.map { (timestamp, _) ->
                val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR) % 100

                if (month == Calendar.JANUARY) {
                    String.format("%02d Jan '%02d", day, year)
                } else {
                    String.format(
                        "%02d %s",
                        day,
                        SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
                    )
                }
            }

            binding.lineChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dateLabels)
                this.textColor = textColor
                textSize = 10f
                setDrawGridLines(true)
                this.gridColor = gridColor
                gridLineWidth = 0.5f
            }

            binding.lineChart.axisLeft.apply {
                this.textColor = textColor
                textSize = 10f
                setDrawGridLines(true)
                this.gridColor = gridColor
                gridLineWidth = 0.5f
            }

            binding.lineChart.axisRight.isEnabled = false

            binding.lineChart.legend.isEnabled = false
            binding.lineChart.description.isEnabled = false

            binding.lineChart.setTouchEnabled(true)
            binding.lineChart.setPinchZoom(true)
            binding.lineChart.setScaleEnabled(true)
            binding.lineChart.setBackgroundColor(Color.TRANSPARENT)

            binding.lineChart.notifyDataSetChanged()
            binding.lineChart.invalidate()

            val personalBests = calculatePersonalBests(exercise, activities)
            adapter.submitList(personalBests)
        }
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

            val dataSet = LineDataSet(entries, "Training Volume")

            val lineColor = ContextCompat.getColor(
                requireContext(),
                com.google.android.material.R.color.design_default_color_primary
            )

            // 🔥 Match strength chart styling (but simpler)
            dataSet.setDrawValues(false)
            dataSet.setDrawCircles(false)   // cleaner for dense data
            dataSet.setDrawFilled(false)
            dataSet.lineWidth = 2f
            dataSet.color = lineColor

            binding.lineChart.clear()
            binding.lineChart.data = LineData(dataSet)

            // ----------------------------
            // Theme-aware colors
            // ----------------------------
            val textColor = ContextCompat.getColor(
                requireContext(),
                com.google.android.material.R.color.material_on_surface_emphasis_medium
            )

            val gridColor = ContextCompat.getColor(
                requireContext(),
                com.google.android.material.R.color.material_on_surface_disabled
            )

            // ----------------------------
            // X Axis (dates)
            // ----------------------------
            val dateLabels = volumePerSessions.map {
                val calendar = Calendar.getInstance().apply { timeInMillis = it.date }
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR) % 100

                if (month == Calendar.JANUARY) {
                    String.format("%02d Jan '%02d", day, year)
                } else {
                    String.format(
                        "%02d %s",
                        day,
                        SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
                    )
                }
            }

            binding.lineChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dateLabels)
                this.textColor = textColor
                textSize = 10f
                setDrawGridLines(true)
                this.gridColor = gridColor
                gridLineWidth = 0.5f
            }

            // ----------------------------
            // Y Axis
            // ----------------------------
            binding.lineChart.axisLeft.apply {
                this.textColor = textColor
                textSize = 10f
                setDrawGridLines(true)
                this.gridColor = gridColor
                gridLineWidth = 0.5f
            }

            binding.lineChart.axisRight.isEnabled = false

            // ----------------------------
            // Legend + Description
            // ----------------------------
            binding.lineChart.legend.isEnabled = false
            binding.lineChart.description.isEnabled = false

            // ----------------------------
            // General styling
            // ----------------------------
            binding.lineChart.setTouchEnabled(true)
            binding.lineChart.setPinchZoom(true)
            binding.lineChart.setScaleEnabled(true)
            binding.lineChart.setBackgroundColor(Color.TRANSPARENT)

            binding.lineChart.notifyDataSetChanged()
            binding.lineChart.invalidate()

            // ----------------------------
            // Personal Bests (unchanged)
            // ----------------------------
            val activities = withContext(Dispatchers.IO) {
                dao.getActivitiesForExercise(exercise.id)
            }
            val personalBests = calculatePersonalBests(exercise, activities)
            adapter.submitList(personalBests)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}