package info.matthewryan.workoutlogger.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import info.matthewryan.workoutlogger.databinding.FragmentExerciseDetailBinding

class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)

        val exercise = ExerciseDetailFragmentArgs.fromBundle(requireArguments()).exercise

        binding.editTextExerciseName.setText(exercise.name)
        binding.switchUnilateral.isChecked = exercise.isUnilateral
        binding.switchTimed.isChecked = exercise.isTimed
        binding.switchFactory.isChecked = exercise.factory

        // Green highlight when true
        setSwitchColor(binding.switchUnilateral, exercise.isUnilateral)
        setSwitchColor(binding.switchTimed, exercise.isTimed)
        setSwitchColor(binding.switchFactory, exercise.factory)

        // Optional: update colors live if toggled
        binding.switchUnilateral.setOnCheckedChangeListener { _, isChecked ->
            setSwitchColor(binding.switchUnilateral, isChecked)
        }
        binding.switchTimed.setOnCheckedChangeListener { _, isChecked ->
            setSwitchColor(binding.switchTimed, isChecked)
        }

        return binding.root
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
