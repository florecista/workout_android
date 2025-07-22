package info.matthewryan.workoutlogger.ui.sessionlog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentSessionLogBinding
import info.matthewryan.workoutlogger.model.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionLogFragment : Fragment() {

    private var _binding: FragmentSessionLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SessionLogAdapter
    private var sessionId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionId = arguments?.getLong("sessionId") ?: -1L

        Log.d("SessionLog", "Received sessionId: $sessionId")

        adapter = SessionLogAdapter(emptyList()) { activityWithExercise ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(requireContext())
                        .activityDao()
                        .deleteActivity(activityWithExercise.activity) // ✅ fixed
                }
                loadActivities() // Reload after deletion
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SessionLogFragment.adapter
        }

        loadActivities()
    }

    private fun loadActivities() {
        lifecycleScope.launch {
            val activities = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext())
                    .activityDao()
                    .getActivitiesForSession(sessionId)
            }
            adapter.updateData(activities)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
