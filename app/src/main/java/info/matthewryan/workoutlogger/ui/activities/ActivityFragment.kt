package info.matthewryan.workoutlogger.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
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
import info.matthewryan.workoutlogger.model.Activity
import info.matthewryan.workoutlogger.model.ExerciseType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    private lateinit var spinnerExercises: Spinner
    private lateinit var editTextSet: EditText
    private lateinit var editTextOne: EditText
    private lateinit var editTextTwo: EditText
    private lateinit var btnEndSession: Button
    private lateinit var btnLog: Button
    private lateinit var btnDelete: Button
    private lateinit var btnSave: Button

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private val exerciseDao by lazy { db.exerciseDao() }

    private var sessionId: Long = -1L
    private var durationWatcher: TextWatcher? = null
    private var selectedExerciseType: ExerciseType? = null

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
        editTextOne = binding.editTextOne
        editTextTwo = binding.editTextTwo
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

        btnEndSession.setOnClickListener { endSession() }

        btnLog.setOnClickListener {
            val bundle = Bundle().apply { putLong("sessionId", sessionId) }
            findNavController().navigate(R.id.action_activityFragment_to_sessionLogFragment, bundle)
        }

        btnDelete.setOnClickListener {
            val focused = listOf(editTextOne, editTextTwo).find { it.hasFocus() }
            focused?.let {
                val current = it.text.toString()
                if (current.isNotEmpty()) {
                    it.setText(current.dropLast(1))
                    it.setSelection(it.text.length)
                }
            }
        }

        btnSave.setOnClickListener { saveAction() }

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
                    editTextOne.hint = ""
                    editTextTwo.hint = ""
                    selectedExerciseType = null
                    removeDurationMask()
                    return
                }

                val selectedExerciseName = parent?.getItemAtPosition(position).toString()
                activityViewModel.updateExercise(selectedExerciseName)

                lifecycleScope.launch {
                    val exercise = withContext(Dispatchers.IO) {
                        exerciseDao.getExerciseByName(selectedExerciseName)
                    }

                    exercise?.let {
                        selectedExerciseType = it.type

                        val count = withContext(Dispatchers.IO) {
                            db.activityDao().countActivitiesByExerciseInSession(sessionId, it.id)
                        }
                        editTextSet.setText((count + 1).toString())

                        when (it.type) {
                            ExerciseType.STRENGTH -> {
                                editTextOne.hint = "Weight (kg)"
                                editTextTwo.hint = "Reps"
                                editTextTwo.inputType = InputType.TYPE_CLASS_NUMBER
                                removeDurationMask()
                            }
                            ExerciseType.CARDIO -> {
                                editTextOne.hint = "Distance (km)"
                                editTextTwo.hint = "Duration (mm:ss)"
                                editTextTwo.inputType = InputType.TYPE_CLASS_NUMBER
                                setDurationInputMask()
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return root
    }

    private fun setDurationInputMask() {
        removeDurationMask()
        durationWatcher = object : TextWatcher {
            private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val digits = s.toString().filter { it.isDigit() }.takeLast(4)
                val padded = digits.padStart(4, '0')
                val minutes = padded.take(2)
                val seconds = padded.takeLast(2)
                val formatted = "$minutes:$seconds"

                isUpdating = true
                editTextTwo.setText(formatted)
                editTextTwo.setSelection(formatted.length)
                isUpdating = false
            }
        }
        editTextTwo.addTextChangedListener(durationWatcher)
    }

    private fun removeDurationMask() {
        durationWatcher?.let { editTextTwo.removeTextChangedListener(it) }
        durationWatcher = null
    }

    private fun appendToFocusedEditText(value: String) {
        val focused = listOf(editTextOne, editTextTwo).find { it.hasFocus() }
        focused?.apply {
            append(value)
            setSelection(text.length)
        }
    }

    private fun saveAction() {
        val selectedExerciseName = spinnerExercises.selectedItem.toString()
        if (selectedExerciseName == "Select" || selectedExerciseType == null) {
            println("Please select a valid exercise.")
            return
        }

        val timestamp = System.currentTimeMillis()
        if (sessionId == -1L) return

        lifecycleScope.launch {
            val exercise = withContext(Dispatchers.IO) {
                exerciseDao.getExerciseByName(selectedExerciseName)
            }

            if (exercise == null) {
                println("Exercise not found.")
                return@launch
            }

            val activity = when (exercise.type) {
                ExerciseType.STRENGTH -> {
                    val reps = editTextTwo.text.toString().toIntOrNull()
                    val weight = editTextOne.text.toString().toDoubleOrNull()
                    if (reps == null || weight == null) {
                        println("Reps and weight must be valid numbers.")
                        return@launch
                    }

                    Activity(
                        exerciseId = exercise.id,
                        reps = reps,
                        weight = weight,
                        timestamp = timestamp,
                        sessionId = sessionId
                    )
                }

                ExerciseType.CARDIO -> {
                    val distance = editTextOne.text.toString().toDoubleOrNull()
                    val durationInSeconds = parseDuration(editTextTwo.text.toString())
                    if (distance == null || durationInSeconds == 0) {
                        println("Distance and duration must be valid.")
                        return@launch
                    }

                    Activity(
                        exerciseId = exercise.id,
                        reps = 0,
                        weight = 0.0,
                        timestamp = timestamp,
                        sessionId = sessionId,
                        distance = distance,
                        durationInSeconds = durationInSeconds
                    )
                }
            }

            withContext(Dispatchers.IO) {
                db.activityDao().insert(activity)
            }

            val newCount = withContext(Dispatchers.IO) {
                db.activityDao().countActivitiesByExerciseInSession(sessionId, exercise.id)
            }
            editTextSet.setText((newCount + 1).toString())
            updateLogButtonState()
        }
    }

    private fun parseDuration(text: String): Int {
        val parts = text.split(":")
        return when {
            parts.size == 2 -> {
                val minutes = parts[0].toIntOrNull() ?: 0
                val seconds = parts[1].toIntOrNull() ?: 0
                minutes * 60 + seconds
            }
            else -> text.toIntOrNull() ?: 0
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
