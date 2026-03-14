package info.matthewryan.workoutlogger.ui.config

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.data.importcsv.CsvImportCoordinator
import info.matthewryan.workoutlogger.data.importcsv.ImportVerifier
import info.matthewryan.workoutlogger.databinding.FragmentConfigBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfigFragment : Fragment() {

    private var _binding: FragmentConfigBinding? = null
    private val binding get() = _binding!!

    private val db by lazy {
        AppDatabase.getDatabase(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentConfigBinding.inflate(inflater, container, false)

        // Clear Activities
        binding.buttonClearActivities.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Clear Activities")
                .setMessage("This will delete all activity records. Continue?")
                .setPositiveButton("Delete") { _, _ ->

                    lifecycleScope.launch {

                        db.activityDao().deleteAll()

                        val remaining = db.activityDao().getAllActivities().size
                        Log.d("DB_RESET", "Activities remaining: $remaining")
                    }

                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Factory Reset
        binding.buttonFactoryReset.isEnabled = true
        binding.buttonFactoryReset.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Factory Reset")
                .setMessage(
                    "This will delete ALL sessions and activities and remove any custom exercises.\n\nContinue?"
                )
                .setPositiveButton("Reset") { _, _ ->

                    lifecycleScope.launch {

                        db.activityDao().deleteAll()
                        db.sessionDao().deleteAll()
                        db.exerciseDao().deleteNonFactoryExercises()

                        Log.d("DB_RESET", "Factory reset complete")
                    }

                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Units selection
        binding.radioGroupUnits.setOnCheckedChangeListener { _, checkedId ->
            val unit = when (checkedId) {
                binding.radioKg.id -> "kg"
                binding.radioLbs.id -> "lbs"
                else -> "kg"
            }
            Log.d("CONFIG", "Selected unit: $unit")
        }

        // Theme switching
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

        // Import CSV button
        binding.buttonImportCsv.setOnClickListener {
            csvPicker.launch(arrayOf("text/*", "application/csv"))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val csvPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->

            uri ?: return@registerForActivityResult

            lifecycleScope.launch {

                val result = withContext(Dispatchers.IO) {

                    requireContext().contentResolver
                        .openInputStream(uri)!!
                        .use { inputStream ->

                            val coordinator = CsvImportCoordinator(db)
                            val report = coordinator.importCsv(inputStream)

                            val verifier = ImportVerifier(db)
                            val verification = verifier.verify(report.rowsRead)

                            Pair(report, verification)
                        }
                }

                val report = result.first
                val verification = result.second

                Log.d("CSV_IMPORT", "Rows: ${report.rowsRead}")
                Log.d("CSV_IMPORT", "Sessions: ${report.sessionsCreated}")
                Log.d("CSV_IMPORT", "Exercises created: ${report.exercisesCreated}")
                Log.d("CSV_IMPORT", "Activities created: ${report.activitiesCreated}")

                AlertDialog.Builder(requireContext())
                    .setTitle("Import Complete")
                    .setMessage(
                        """
                        Rows read: ${report.rowsRead}
                        Sessions created: ${report.sessionsCreated}
                        Exercises created: ${report.exercisesCreated}
                        Activities created: ${report.activitiesCreated}
                
                        $verification
                        """.trimIndent()
                    )
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
}