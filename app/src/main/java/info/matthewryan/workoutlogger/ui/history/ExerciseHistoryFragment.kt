package info.matthewryan.workoutlogger.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentExerciseHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseHistoryFragment : Fragment() {

    private var _binding: FragmentExerciseHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExerciseHistoryAdapter

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private val sessionDao by lazy { db.sessionDao() }
    private val exerciseDao by lazy { db.exerciseDao() }

    private var exerciseId: Int = -1

    private val dateFormatter = SimpleDateFormat(
        "EEEE, dd MMMM yyyy",
        Locale.getDefault()
    )

    private val timeFormatter = SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseId = arguments?.getInt("exerciseId") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = ExerciseHistoryAdapter()

        binding.recyclerViewHistory.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerViewHistory.adapter = adapter

        loadExerciseSessions()
    }

    private fun loadExerciseSessions() {

        lifecycleScope.launch {

            val rows = withContext(Dispatchers.IO) {

                val rows = mutableListOf<ExerciseHistoryRow>()

                val sessionsWithActivities = sessionDao.getAllSessions()

                val exerciseName =
                    exerciseDao.getExerciseById(exerciseId)?.name ?: "Unknown"

                var lastDate: String? = null

                var bestWeight = 0.0
                var bestVolume = 0.0

                sessionsWithActivities
                    .flatMap { it.activities }
                    .filter { it.activity.exerciseId == exerciseId }
                    .sortedByDescending { it.activity.timestamp }
                    .forEachIndexed { index, activityWithExercise ->

                        val activity = activityWithExercise.activity

                        val date = dateFormatter.format(Date(activity.timestamp))
                        val time = timeFormatter.format(Date(activity.timestamp))

                        if (date != lastDate) {
                            rows.add(ExerciseHistoryRow.DateHeader(date))
                            lastDate = date
                        }

                        val weight = activity.weight
                        val reps = activity.reps
                        val volume = weight * reps

                        val isPr =
                            weight > bestWeight ||
                                    volume > bestVolume

                        if (weight > bestWeight) {
                            bestWeight = weight
                        }

                        if (volume > bestVolume) {
                            bestVolume = volume
                        }

                        rows.add(
                            ExerciseHistoryRow.ActivityItem(
                                time = time,
                                exerciseName = exerciseName,
                                weight = weight,
                                reps = reps,
                                setIndex = index + 1,
                                isPr = isPr
                            )
                        )
                    }

                rows
            }

            adapter.submitList(rows)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}