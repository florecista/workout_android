package info.matthewryan.workoutlogger.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.R
import info.matthewryan.workoutlogger.databinding.FragmentHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HistoryAdapter
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private val sessionDao by lazy { db.sessionDao() }

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

        binding.calendarView.setHeaderTextAppearance(R.style.CalendarHeaderText)
        binding.calendarView.setDateTextAppearance(R.style.CalendarDateText)
        binding.calendarView.setWeekDayTextAppearance(R.style.CalendarWeekText)

        adapter = HistoryAdapter(
            onDeleteClick = { sessionDisplay ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        sessionDao.deleteSessionById(sessionDisplay.session.id)
                    }

                    val updatedList = adapter.currentList.toMutableList().apply {
                        remove(sessionDisplay)
                    }
                    adapter.submitList(updatedList)
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

        // Attach ItemTouchHelper for swipe-to-delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val sessionDisplay = adapter.currentList[position]

                // Trigger the same delete logic
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        sessionDao.deleteSessionById(sessionDisplay.session.id)
                    }

                    val updatedList = adapter.currentList.toMutableList().apply {
                        remove(sessionDisplay)
                    }
                    adapter.submitList(updatedList)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHistory)

        loadSessions()
    }

    private fun loadSessions() {
        val sessionDao = db.sessionDao()
        val exerciseDao = db.exerciseDao()

        lifecycleScope.launch {
            val sessionDisplays = withContext(Dispatchers.IO) {
                val sessionsWithActivities = sessionDao.getAllSessions()

                sessionsWithActivities.map { sessionWithActivities ->
                    val exerciseNames = sessionWithActivities.activities
                        .mapNotNull { activity ->
                            exerciseDao.getExerciseById(activity.exerciseId)?.name
                        }
                        .distinct()

                    SessionDisplay(sessionWithActivities.session, exerciseNames)
                }
            }

            adapter.submitList(sessionDisplays.sortedByDescending { it.session.startTimestamp })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
