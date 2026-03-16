package info.matthewryan.workoutlogger.ui.activities

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.R
import info.matthewryan.workoutlogger.dao.DailyVolumeRow
import info.matthewryan.workoutlogger.databinding.FragmentActivityBinding
import info.matthewryan.workoutlogger.model.Activity
import info.matthewryan.workoutlogger.model.ExerciseType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    private lateinit var dropdownExercises: AutoCompleteTextView
    private lateinit var editTextSet: EditText
    private lateinit var editTextOne: EditText
    private lateinit var editTextTwo: EditText
    private lateinit var btnEndSession: Button
    private lateinit var btnLog: Button
    private lateinit var btnDelete: Button
    private lateinit var btnSave: Button

    private lateinit var btnSessionHistory: Button

    private lateinit var btnExerciseHistory: Button

    private lateinit var chart: LineChart

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private val exerciseDao by lazy { db.exerciseDao() }
    private val activityDao by lazy { db.activityDao() }

    private val activityViewModel: ActivityViewModel by viewModels {
        ActivityViewModel.Factory(activityDao, exerciseDao)
    }

    private var sessionId: Long = -1L
    private var durationWatcher: TextWatcher? = null
    private var selectedExerciseType: ExerciseType? = null

    // NEW: track current selected exercise id to refresh the limit line after saves
    private var selectedExerciseId: Int? = null
    private var lastSessionVolume: Double? = null

    private val zone = ZoneId.of("Australia/Sydney")
    private val dayLabelFmt = DateTimeFormatter.ofPattern("d-MMM", Locale.getDefault())

    private var axisLabelColor: Int = android.graphics.Color.BLACK

    private lateinit var prBanner: View
    private lateinit var prBannerText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sessionId = arguments?.getLong("sessionId") ?: -1L

        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        val root = binding.root

        // Bind views
        dropdownExercises = binding.dropdownExercises
        editTextSet = binding.editTextSet
        editTextOne = binding.editTextOne
        editTextTwo = binding.editTextTwo
        btnEndSession = binding.btnEndSession
        btnLog = binding.btnLog
        btnDelete = binding.btnDelete
        btnSave = binding.btnSave
        btnSessionHistory = binding.btnSessionHistory
        btnExerciseHistory = binding.btnExerciseHistory
        chart = binding.chart1

        prBanner = binding.prBanner
        prBannerText = binding.prBannerText

        initChart()

        // number pad
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
        inputButtons.forEach { (button, value) -> button.setOnClickListener { appendToFocusedEditText(value) } }

        btnEndSession.setOnClickListener { endSession() }

        btnLog.setOnClickListener {
            val bundle = Bundle().apply { putLong("sessionId", sessionId) }
            findNavController().navigate(R.id.action_activityFragment_to_sessionLogFragment, bundle)
        }

        btnSessionHistory.setOnClickListener {
            val bundle = Bundle().apply { putLong("sessionId", sessionId) }
            findNavController().navigate(R.id.action_activityFragment_to_sessionLogFragment, bundle)
        }

        btnExerciseHistory.setOnClickListener {

            selectedExerciseId?.let { id ->

                val bundle = Bundle().apply {
                    putInt("exerciseId", id)
                }

                findNavController().navigate(
                    R.id.navigation_exercise_history,
                    bundle
                )
            }
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

        // Populate spinner
        lifecycleScope.launch {
            val exercisesFromDb = withContext(Dispatchers.IO) { exerciseDao.getAllExercises() }
            println("DEBUG: Loaded ${exercisesFromDb.size} exercises from DB: ${exercisesFromDb.map { it.name }}")
            val names: List<String> = listOf("Select") + exercisesFromDb.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
            dropdownExercises.setAdapter(adapter)
        }

        // Spinner selection → update VM + adjust hints + set 'Set' number
        dropdownExercises.setOnItemClickListener { parent, _, position, _ ->

            if (position == 0) {
                editTextSet.setText("")
                editTextOne.hint = ""
                editTextTwo.hint = ""
                selectedExerciseType = null
                selectedExerciseId = null
                removeDurationMask()
                renderDailyVolume(emptyList())
                chart.axisLeft.removeAllLimitLines()
                chart.invalidate()
                return@setOnItemClickListener
            }

            val selectedExerciseName = parent.getItemAtPosition(position).toString()
            activityViewModel.updateExerciseByName(selectedExerciseName)

            lifecycleScope.launch {
                val exercise = withContext(Dispatchers.IO) {
                    exerciseDao.getExerciseByName(selectedExerciseName)
                }

                if (exercise == null) return@launch

                selectedExerciseType = exercise.type
                selectedExerciseId = exercise.id

                val count = withContext(Dispatchers.IO) {
                    db.activityDao().countActivitiesByExerciseInSession(sessionId, exercise.id)
                }

                editTextSet.setText((count + 1).toString())

                when (exercise.type) {
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

        // set log button enabled/disabled
        updateLogButtonState()

        // Collect chart data; it auto-updates with selected exercise
        viewLifecycleOwner.lifecycleScope.launch {
            activityViewModel.dailyVolumeRows.collectLatest { rows ->
                renderDailyVolume(rows) // rows: List<DailyVolumeRow>
                // Re-apply session volume line after data refresh (if we have one)
                lastSessionVolume?.let { updateSessionVolumeLine(it) }
            }
        }

        return root
    }

    private fun initChart() {
        chart.description.isEnabled = false
        chart.setNoDataText("No volume yet")
        chart.axisRight.isEnabled = false

        chart.xAxis.apply {
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            isGranularityEnabled = true
            labelRotationAngle = -30f
            setDrawAxisLine(true)
            setDrawGridLines(false)
            textSize = 9f          // small tick font
            yOffset = 4f           // nudge labels away from axis
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                    val date = java.time.LocalDate.ofEpochDay(value.toLong())
                    return dayLabelFmt.format(date)
                }
            }
            setAvoidFirstLastClipping(true)
        }

        chart.axisLeft.apply {
            axisMinimum = 0f
            // keep other left-axis defaults
        }

        chart.legend.isEnabled = false
        chart.setPinchZoom(true)
        chart.isDoubleTapToZoomEnabled = true

        // Give the chart extra bottom padding so X labels aren't clipped
        chart.setExtraOffsets(0f, 0f, 0f, 20f)

        // Detect dark/light theme safely
        val night = requireContext().resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        axisLabelColor = if (night == android.content.res.Configuration.UI_MODE_NIGHT_YES)
            android.graphics.Color.WHITE
        else
            android.graphics.Color.BLACK

        // Tint tick labels to match theme
        chart.xAxis.textColor = axisLabelColor
        chart.axisLeft.textColor = axisLabelColor

        // Add axis titles once (we’ll refresh after data updates too)
        addAxisTitles()
    }

    private fun addAxisTitles() {
        // Y-axis title
        val left = chart.axisLeft
        left.removeAllLimitLines()
        val yLabel = com.github.mikephil.charting.components.LimitLine(
            left.axisMaximum,
            "Volume"
        ).apply {
            textSize = 10f
            lineWidth = 0f          // hide the line, show only text
            textColor = axisLabelColor
            labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.RIGHT_TOP
            yOffset = 2f            // slight nudge so it doesn't collide with data
        }
        left.addLimitLine(yLabel)

        // X-axis title
        val x = chart.xAxis
        x.removeAllLimitLines()
        val xLabel = com.github.mikephil.charting.components.LimitLine(
            x.axisMaximum,
            "Date"
        ).apply {
            textSize = 10f
            lineWidth = 0f
            textColor = axisLabelColor
            labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.RIGHT_BOTTOM
            yOffset = 12f           // push title below tick labels
        }
        x.addLimitLine(xLabel)
    }

    private fun renderDailyVolume(rows: List<DailyVolumeRow>) {
        if (rows.isEmpty()) {
            chart.data = null
            chart.invalidate()
            return
        }

        val entries = rows.map { r ->
            val day = java.time.LocalDate.parse(r.day) // "YYYY-MM-DD"
            val x = day.toEpochDay().toFloat()
            val y = r.volume.toFloat()
            Entry(x, y)
        }

        val dataSet = LineDataSet(entries, "Daily Volume").apply {
            setDrawCircles(true)
            circleRadius = 2.5f
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.data = LineData(dataSet)
        chart.invalidate()
        chart.animateX(250)
    }

    private fun updateSessionVolumeLine(volume: Double) {
        val axis = chart.axisLeft
        axis.removeAllLimitLines()

        val ll = com.github.mikephil.charting.components.LimitLine(volume.toFloat(), "Session volume")
        ll.lineWidth = 1.5f
        ll.enableDashedLine(12f, 8f, 0f)
        ll.textSize = 10f
        ll.labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.RIGHT_TOP

        axis.addLimitLine(ll)
        chart.invalidate()
    }

    // ========== Input helpers ==========

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

    // ========== Save / Session ==========

    private fun saveAction() {
        val selectedExerciseName = dropdownExercises.text.toString()
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

            var reps: Int? = null
            var weight: Double? = null
            var volume: Double? = null

            val activity = when (exercise.type) {
                ExerciseType.STRENGTH -> {
                    reps = editTextTwo.text.toString().toIntOrNull()
                    weight = editTextOne.text.toString().toDoubleOrNull()
                    if (reps == null || weight == null) {
                        println("Reps and weight must be valid numbers.")
                        return@launch
                    }

                    volume = weight * reps

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

            // 1️⃣ Check weight PR BEFORE insert
            val isWeightPR = if (reps != null && weight != null) {
                activityViewModel.isNewWeightPrForRepCount(exercise.id, reps, weight)
            } else {
                false
            }

            // 2️⃣ Insert the activity
            activityViewModel.insertActivity(activity)

            // 3️⃣ Recalculate session volume
            val sessionVol = activityViewModel.computeSessionVolume(sessionId, exercise.id)

            // 4️⃣ Get historical best session volume
            val maxSessionVol = activityViewModel.getMaxSessionVolumeForExercise(exercise.id)

            // 5️⃣ Check session volume PR
            val isSessionVolumePR = sessionVol > maxSessionVol

            // 6️⃣ Trigger alerts
            if (isWeightPR) {
                showPRAlert(weight, reps)
            } else if (isSessionVolumePR) {
                showSessionVolumePRAlert(sessionVol)
            }

            // Update set number
            val newCount = activityViewModel.countActivitiesByExerciseInSession(sessionId, exercise.id)
            editTextSet.setText((newCount + 1).toString())

            // NEW: refresh the session volume line after save
            lastSessionVolume = sessionVol
            updateSessionVolumeLine(sessionVol)

            updateLogButtonState()
        }
    }

    // compute SUM(reps*weight) for this session & exercise — no DAO change needed
    private suspend fun computeSessionVolumeForExercise(sessionId: Long, exerciseId: Int): Double {
        val rows = db.activityDao().getActivitiesForSession(sessionId) // IO-called by caller
        return rows.asSequence()
            .filter { it.activity.exerciseId == exerciseId }
            .filter { it.activity.reps > 0 && it.activity.weight > 0.0 }
            .sumOf { it.activity.reps * it.activity.weight }
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
            Navigation.findNavController(requireView())
                .navigate(R.id.action_activityFragment_to_homeFragment)
        }
    }

    private fun updateLogButtonState() {
        lifecycleScope.launch {
            val count = activityViewModel.countActivitiesInSession(sessionId)
            btnLog.isEnabled = count > 0
        }
    }

    private fun showPRAlert(weight: Double?, reps: Int?) {

        if (weight == null || reps == null) return

        // 🔔 Haptic feedback
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        val message = "🏆 Personal Record!  ${weight} kg × $reps reps"

        prBannerText.text = message
        prBanner.visibility = View.VISIBLE

        prBanner.translationY = -prBanner.height.toFloat()

        prBanner.animate()
            .translationY(0f)
            .setDuration(300)
            .withEndAction {

                // Small bounce
                prBanner.animate()
                    .translationY(-10f)
                    .setDuration(120)
                    .withEndAction {

                        prBanner.animate()
                            .translationY(0f)
                            .setDuration(120)
                            .withEndAction {

                                // Pulse animation
                                prBanner.animate()
                                    .scaleX(1.05f)
                                    .scaleY(1.05f)
                                    .setDuration(120)
                                    .withEndAction {
                                        prBanner.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(120)
                                            .start()
                                    }
                                    .start()

                            }
                            .start()

                    }
                    .start()

            }
            .start()

        prBanner.postDelayed({

            prBanner.animate()
                .translationY(-prBanner.height.toFloat())
                .setDuration(300)
                .withEndAction {
                    prBanner.visibility = View.GONE
                }
                .start()

        }, 2500)
    }

    private fun showSessionVolumePRAlert(volume: Double) {

        // 🔔 Haptic feedback
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        val message = "🏆 Session Volume PR!  ${volume.toInt()} kg"

        prBannerText.text = message
        prBanner.visibility = View.VISIBLE

        prBanner.translationY = -prBanner.height.toFloat()

        prBanner.animate()
            .translationY(0f)
            .setDuration(300)
            .start()

        prBanner.postDelayed({

            prBanner.animate()
                .translationY(-prBanner.height.toFloat())
                .setDuration(300)
                .withEndAction {
                    prBanner.visibility = View.GONE
                }
                .start()

        }, 2500)
    }

    // ========== Lifecycle ==========

    override fun onStop() {
        super.onStop()
        autoCloseSessionIfNotAlready()
    }

    private fun autoCloseSessionIfNotAlready() {
        if (sessionId == -1L) return

        lifecycleScope.launch {
            val session = withContext(Dispatchers.IO) { db.sessionDao().getSessionById(sessionId) }
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
