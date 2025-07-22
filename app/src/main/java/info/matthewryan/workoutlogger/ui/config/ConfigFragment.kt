package info.matthewryan.workoutlogger.ui.config

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentConfigBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfigFragment : Fragment() {

    private var _binding: FragmentConfigBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)

        // Clear Activities button
        binding.buttonClearActivities.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.activityDao().deleteAll()
                }
            }
        }

        // Factory Reset (disabled for now)
        binding.buttonFactoryReset.isEnabled = false
        binding.buttonFactoryReset.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.activityDao().deleteAll()
                    db.exerciseDao().deleteNonFactoryExercises()
                }
            }
        }

        // Units of Measure selection logic
        binding.radioGroupUnits.setOnCheckedChangeListener { _, checkedId ->
            val unit = when (checkedId) {
                binding.radioKg.id -> "kg"
                binding.radioLbs.id -> "lbs"
                else -> "kg"
            }
            println("Selected unit: $unit")
        }

        // --- Theme Switching ---
        val sharedPref = requireContext().getSharedPreferences("settings", 0)
        val savedTheme = sharedPref.getString("app_theme", "system")

        when (savedTheme) {
            "light" -> binding.radioLight.isChecked = true
            "dark" -> binding.radioDark.isChecked = true
            else -> binding.radioSystem.isChecked = true
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val newTheme = when (checkedId) {
                binding.radioLight.id -> "light"
                binding.radioDark.id -> "dark"
                else -> "system"
            }

            with(sharedPref.edit()) {
                putString("app_theme", newTheme)
                apply()
            }

            val mode = when (newTheme) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

            AppCompatDelegate.setDefaultNightMode(mode)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
