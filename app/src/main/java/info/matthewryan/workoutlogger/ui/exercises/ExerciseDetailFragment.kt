package info.matthewryan.workoutlogger.ui.exercises

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentExerciseDetailBinding
import info.matthewryan.workoutlogger.databinding.ViewSettingRowBinding
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

        // Set name
        binding.editTextExerciseName.setText(exercise?.name ?: "")

        // Setup rows
        binding.rowUnilateral.setTitle("Unilateral Exercise")
        binding.rowUnilateral.setDescription(
            "Counts volume for both sides when the weight represents one limb."
        )
        binding.rowUnilateral.isChecked = exercise?.isUnilateral ?: false

        binding.rowTimed.setTitle("Timed Exercise")
        binding.rowTimed.setDescription(
            "Records duration instead of repetitions."
        )
        binding.rowTimed.isChecked = exercise?.isTimed ?: false

        binding.rowFactory.setTitle("Factory Exercise")
        binding.rowFactory.setDescription(
            "Included with the app and cannot be edited."
        )
        binding.rowFactory.isChecked = exercise?.factory ?: false
        binding.rowFactory.isRowEnabled = false

        // ✅ Save button handler
        binding.fabSaveExercise.setOnClickListener {
            val name = binding.editTextExerciseName.text.toString().trim()

            if (name.isNotEmpty()) {

                val unilateral = binding.rowUnilateral.isChecked
                val timed = binding.rowTimed.isChecked
                val factory = binding.rowFactory.isChecked

                val updatedExercise = Exercise(
                    id = exercise?.id ?: 0,
                    name = name,
                    factory = factory,
                    isUnilateral = unilateral,
                    isTimed = timed,
                    duration = exercise?.duration,
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

    private fun setupSettingRow(
        row: ViewSettingRowBinding,
        title: String,
        description: String,
        checked: Boolean,
        enabled: Boolean = true
    ) {
        row.title.text = title
        row.description.text = description
        row.switchView.isChecked = checked
        row.switchView.isEnabled = enabled

        setSwitchColor(row.switchView, checked)

        row.root.setOnClickListener {
            if (enabled) {
                val newValue = !row.switchView.isChecked
                row.switchView.isChecked = newValue
                setSwitchColor(row.switchView, newValue)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Factory exercises cannot be modified",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setSwitchColor(switch: SwitchCompat, isChecked: Boolean) {
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