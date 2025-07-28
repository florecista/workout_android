package info.matthewryan.workoutlogger.ui.exercises

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentExerciseDetailBinding
import info.matthewryan.workoutlogger.model.Exercise
import info.matthewryan.workoutlogger.model.ExerciseType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    private val exerciseDao by lazy { AppDatabase.getDatabase(requireContext()).exerciseDao() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        val root = binding.root

        val exercise = ExerciseDetailFragmentArgs.fromBundle(requireArguments()).exercise

        if (exercise != null) {
            // Edit mode
            binding.editTextExerciseName.setText(exercise.name)
            binding.switchUnilateral.isChecked = exercise.isUnilateral
            binding.switchTimed.isChecked = exercise.isTimed
            binding.switchFactory.isChecked = exercise.factory

            // Green highlight when true
            setSwitchColor(binding.switchUnilateral, exercise.isUnilateral)
            setSwitchColor(binding.switchTimed, exercise.isTimed)
            setSwitchColor(binding.switchFactory, exercise.factory)
        } else {
            // Add mode — start blank
            binding.editTextExerciseName.setText("")
            binding.switchUnilateral.isChecked = false
            binding.switchTimed.isChecked = false
            binding.switchFactory.isChecked = false

            // No need to color initially
            setSwitchColor(binding.switchUnilateral, false)
            setSwitchColor(binding.switchTimed, false)
            setSwitchColor(binding.switchFactory, false)
        }

        // Update color when toggled
        binding.switchUnilateral.setOnCheckedChangeListener { _, isChecked ->
            setSwitchColor(binding.switchUnilateral, isChecked)
        }
        binding.switchTimed.setOnCheckedChangeListener { _, isChecked ->
            setSwitchColor(binding.switchTimed, isChecked)
        }

        // ✅ Save button handler
        binding.fabSaveExercise.setOnClickListener {
            val name = binding.editTextExerciseName.text.toString().trim()
            if (name.isNotEmpty()) {
                val updatedExercise = Exercise(
                    id = exercise?.id ?: 0, // preserve ID for update
                    name = name,
                    factory = binding.switchFactory.isChecked,
                    isUnilateral = binding.switchUnilateral.isChecked,
                    isTimed = binding.switchTimed.isChecked,
                    duration = exercise?.duration, // retain old duration if editing
                    type = exercise?.type ?: ExerciseType.STRENGTH
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        if (exercise != null) {
                            exerciseDao.update(updatedExercise)
                            Log.d("ExerciseSave", "Updated Exercise ID: ${updatedExercise.id}")
                        } else {
                            val newId = exerciseDao.insert(updatedExercise)
                            Log.d("ExerciseSave", "Inserted Exercise ID: $newId")
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Exercise saved", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    } catch (e: SQLiteConstraintException) {
                        Log.e("ExerciseSave", "Save failed: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Name already exists!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        return root
    }

    private fun setSwitchColor(switch: androidx.appcompat.widget.SwitchCompat, isChecked: Boolean) {
        switch.thumbTintList = if (isChecked) {
            android.content.res.ColorStateList.valueOf(
                resources.getColor(android.R.color.holo_green_dark, null)
            )
        } else {
            android.content.res.ColorStateList.valueOf(
                resources.getColor(android.R.color.darker_gray, null)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
