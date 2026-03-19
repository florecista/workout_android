package info.matthewryan.workoutlogger.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentEditActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditActivityFragment : Fragment() {

    private var _binding: FragmentEditActivityBinding? = null
    private val binding get() = _binding!!

    private val activityDao by lazy { AppDatabase.getDatabase(requireContext()).activityDao() }
    private val exerciseDao by lazy { AppDatabase.getDatabase(requireContext()).exerciseDao() }

    private var activityId: Long = -1
    private var currentActivity: info.matthewryan.workoutlogger.model.Activity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityId = arguments?.getLong("activityId") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        loadActivity()

        binding.buttonSave.setOnClickListener {
            saveActivity()
        }
    }

    private fun loadActivity() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                activityDao.getActivityById(activityId)
            }

            result?.let { activity ->
                currentActivity = activity

                binding.editReps.setText(activity.reps.toString())
                binding.editWeight.setText(activity.weight.toString())

                // Load exercise name
                val exercise = withContext(Dispatchers.IO) {
                    exerciseDao.getExerciseById(activity.exerciseId)
                }

                binding.textExerciseName.text = exercise?.name ?: "Unknown"
            }
        }
    }

    private fun saveActivity() {
        val reps = binding.editReps.text.toString().toIntOrNull()
        val weight = binding.editWeight.text.toString().toDoubleOrNull()

        if (reps == null || weight == null) {
            Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
            return
        }

        val updated = currentActivity?.copy(
            reps = reps,
            weight = weight
        ) ?: return

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                activityDao.update(updated)
            }

            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()

            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}