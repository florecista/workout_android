package info.matthewryan.workoutlogger.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentHistoryBinding
import info.matthewryan.workoutlogger.model.CalendarDayItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter
    private lateinit var calendarAdapter: CalendarAdapter

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private val sessionDao by lazy { db.sessionDao() }

    private val currentCalendar = Calendar.getInstance()
    private var sessionDates: Set<String> = emptySet() // yyyy-MM-dd

    private var selectedDate: LocalDate? = null
    private var allSessions: List<SessionDisplay> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSessionList()
        setupCalendar()

        loadSessions()
    }

    // ---------------------------
    // Session List
    // ---------------------------
    private fun setupSessionList() {
        adapter = HistoryAdapter(
            onDeleteClick = { sessionDisplay ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        sessionDao.deleteSessionById(sessionDisplay.session.id)
                    }

                    // ✅ Update master list
                    allSessions = allSessions.filter { it != sessionDisplay }

                    // ✅ Respect current filter state
                    if (selectedDate == null) {
                        adapter.submitList(allSessions)
                    } else {
                        filterSessions()
                    }
                }
            },
            onItemClick = { sessionDisplay ->
                val action = HistoryFragmentDirections
                    .actionHistoryFragmentToSessionDetailFragment(sessionDisplay.session.id)
                view?.findNavController()?.navigate(action)
            }
        )

        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val sessionDisplay = adapter.currentList[position]

                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            sessionDao.deleteSessionById(sessionDisplay.session.id)
                        }

                        // ✅ Update master list
                        allSessions = allSessions.filter { it != sessionDisplay }

                        // ✅ Respect filter state
                        if (selectedDate == null) {
                            adapter.submitList(allSessions)
                        } else {
                            filterSessions()
                        }
                    }
                }
            }
        )

        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHistory)
    }

    // ---------------------------
    // Calendar Setup
    // ---------------------------
    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter()

        binding.calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.calendarRecyclerView.adapter = calendarAdapter

        // ✅ Handle day clicks
        calendarAdapter.onDayClick = { day ->
            if (day.isCurrentMonth) {

                selectedDate = if (selectedDate == day.date) null else day.date

                updateCalendar()

                if (selectedDate == null) {
                    adapter.submitList(allSessions)
                } else {
                    filterSessions()
                }
            }
        }

        binding.prevMonth.setOnClickListener {
            if (_binding == null) return@setOnClickListener
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        binding.nextMonth.setOnClickListener {
            if (_binding == null) return@setOnClickListener
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }

    private fun filterSessions() {
        val selected = selectedDate ?: return

        val filtered = allSessions.filter { session ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = session.session.startTimestamp
            }

            val sessionDate = LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            sessionDate == selected
        }

        adapter.submitList(filtered)
    }

    private fun updateCalendar() {
        _binding?.let { binding ->
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.monthTitle.text = formatter.format(currentCalendar.time)
        }

        val days = generateCalendarDays(currentCalendar)
        calendarAdapter.selectedDate = selectedDate
        calendarAdapter.submitList(days)
    }

    private fun generateCalendarDays(calendar: Calendar): List<CalendarDayItem> {
        val result = mutableListOf<CalendarDayItem>()

        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Leading empty days
        repeat(firstDayOfWeek) {
            result.add(
                CalendarDayItem(
                    date = LocalDate.MIN,
                    isCurrentMonth = false,
                    hasWorkout = false
                )
            )
        }

        // Actual days
        for (day in 1..daysInMonth) {
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            val dateKey = dateFormat.format(tempCal.time)

            val localDate = tempCal.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            result.add(
                CalendarDayItem(
                    date = localDate,
                    isCurrentMonth = true,
                    hasWorkout = sessionDates.contains(dateKey)
                )
            )
        }

        return result
    }

    // ---------------------------
    // Data Loading
    // ---------------------------
    private fun loadSessions() {
        val exerciseDao = db.exerciseDao()

        viewLifecycleOwner.lifecycleScope.launch {
            val sessionDisplays = withContext(Dispatchers.IO) {

                val sessionsWithActivities = sessionDao.getAllSessions()

                sessionsWithActivities.map { sessionWithActivities ->

                    val exerciseNames = sessionWithActivities.activities
                        .map { it.exercise.name }
                        .distinct()
                        .sorted()

                    SessionDisplay(sessionWithActivities.session, exerciseNames)
                }
            }

            // 🔒 Bail out if view is gone
            val binding = _binding ?: return@launch

            // Convert to date keys for calendar dots
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            sessionDates = sessionDisplays.map {
                dateFormat.format(Date(it.session.startTimestamp))
            }.toSet()

            // Safe call
            updateCalendar()

            allSessions = sessionDisplays.sortedByDescending { it.session.startTimestamp }

            adapter.submitList(allSessions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}