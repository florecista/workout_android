package info.matthewryan.workoutlogger.ui.activities

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.R
import info.matthewryan.workoutlogger.databinding.FragmentActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    private lateinit var spinnerExercises: Spinner
    private lateinit var editTextSet: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var editTextReps: EditText
    private lateinit var btnEndSession: Button
    private lateinit var btnLog: Button
    private lateinit var btnDelete: Button
    private lateinit var btnSave: Button

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private val exerciseDao by lazy { db.exerciseDao() }

    private var sessionId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sessionId = arguments?.getLong("sessionId") ?: -1L
        updateLogButtonState()

        val activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        val root = binding.root

        // Bind views
        spinnerExercises = binding.spinnerExercises
        editTextSet = binding.editTextSet
        editTextWeight = binding.editTextWeight
        editTextReps = binding.editTextReps
        btnEndSession = binding.btnEndSession
        btnLog = binding.btnLog
        btnDelete = binding.btnDelete
        btnSave = binding.btnSave

        // Unified number pad handling
        val inputButtons = mapOf(
            binding.btn0 to "0",
            binding.btn1 to "1",
            binding.btn2 to "2",
            binding.btn3 to "3",
            binding.btn4 to "4",
            binding.btn5 to "5",
            binding.btn6 to "6",
            binding.btn7 to "7",
            binding.btn8 to "8",
            binding.btn9 to "9",
            binding.btnDecimal to "."
        )
        inputButtons.forEach { (button, value) ->
            button.setOnClickListener { appendToFocusedEditText(value) }
        }

        // Session control
        btnEndSession.setOnClickListener { endSession() }

        btnLog.setOnClickListener {
            val bundle = Bundle().apply { putLong("sessionId", sessionId) }
            findNavController().navigate(R.id.action_activityFragment_to_sessionLogFragment, bundle)
        }

        btnDelete.setOnClickListener {
            val focused = listOf(editTextWeight, editTextReps).find { it.hasFocus() }
            focused?.let {
                val current = it.text.toString()
                if (current.isNotEmpty()) {
                    it.setText(current.dropLast(1))
                    it.setSelection(it.text.length)
                }
            }
        }

        btnSave.setOnClickListener { saveAction() }

        // Load exercises into spinner
        lifecycleScope.launch {
            val exercisesFromDb = withContext(Dispatchers.IO) {
                exerciseDao.getAllExercises()
            }
            val names = listOf("Select") + exercisesFromDb.map { it.name }
            spinnerExercises.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
        }

        spinnerExercises.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    editTextSet.setText("")
                    return
                }
                val selectedExercise = parent?.getItemAtPosition(position).toString()
                activityViewModel.updateExercise(selectedExercise)

                lifecycleScope.launch {
                    val exercise = withContext(Dispatchers.IO) {
                        exerciseDao.getExerciseByName(selectedExercise)
                    }

                    exercise?.let {
                        val count = withContext(Dispatchers.IO) {
                            db.activityDao().countActivitiesByExerciseInSession(sessionId, it.id)
                        }
                        editTextSet.setText((count + 1).toString())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return root
    }

    override fun onStop() {
        super.onStop()
        autoCloseSessionIfNotAlready()
    }

    private fun autoCloseSessionIfNotAlready() {
        if (sessionId == -1L) return

        lifecycleScope.launch {
            val session = withContext(Dispatchers.IO) {
                db.sessionDao().getSessionById(sessionId)
            }

            if (session?.endTimestamp == null) {
                withContext(Dispatchers.IO) {
                    db.sessionDao().markSessionAsEnded(sessionId, System.currentTimeMillis())
                }
                println("Session $sessionId automatically closed onStop().")
            }
        }
    }

    private fun appendToFocusedEditText(value: String) {
        val focused = listOf(editTextWeight, editTextReps).find { it.hasFocus() }
        focused?.apply {
            append(value)
            setSelection(text.length)
        }
    }

    private fun saveAction() {
        val selectedExercise = spinnerExercises.selectedItem.toString()
        if (selectedExercise == "Select") {
            println("Please select an exercise.")
            return
        }

        val reps = editTextReps.text.toString().toIntOrNull()
        val weight = editTextWeight.text.toString().toDoubleOrNull()
        if (reps == null || weight == null) {
            println("Reps and weight must be valid numbers.")
            return
        }

        val timestamp = System.currentTimeMillis()
        val sessionId = this.sessionId
        if (sessionId == -1L) return

        lifecycleScope.launch {
            val exercise = withContext(Dispatchers.IO) {
                exerciseDao.getExerciseByName(selectedExercise)
            }

            if (exercise == null) {
                println("Exercise not found.")
                return@launch
            }

            val newActivity = info.matthewryan.workoutlogger.model.Activity(
                exerciseId = exercise.id,
                reps = reps,
                weight = weight,
                timestamp = timestamp,
                sessionId = sessionId
            )

            withContext(Dispatchers.IO) {
                db.activityDao().insert(newActivity)
            }

            val newCount = withContext(Dispatchers.IO) {
                db.activityDao().countActivitiesByExerciseInSession(sessionId, exercise.id)
            }
            editTextSet.setText((newCount + 1).toString())
            updateLogButtonState()
        }
    }

    private fun endSession() {
        if (sessionId == -1L) {
            println("No session ID found to end.")
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.sessionDao().markSessionAsEnded(sessionId, System.currentTimeMillis())
            }
            Navigation.findNavController(requireView()).navigate(R.id.action_activityFragment_to_homeFragment)
        }
    }

    private fun updateLogButtonState() {
        lifecycleScope.launch {
            val count = withContext(Dispatchers.IO) {
                db.activityDao().countActivitiesInSession(sessionId)
            }
            btnLog.isEnabled = count > 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
