package info.matthewryan.workoutlogger.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentSessionDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionDetailFragment : Fragment() {

    private var _binding: FragmentSessionDetailBinding? = null
    private val binding get() = _binding!!

    private val activityDao by lazy { AppDatabase.getDatabase(requireContext()).activityDao() }

    private lateinit var adapter: SessionActivityAdapter   // ✅ NEW

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = SessionActivityAdapter { activityWithExercise ->

            val action = SessionDetailFragmentDirections
                .actionSessionDetailFragmentToEditActivityFragment(
                    activityWithExercise.activity.id
                )

            findNavController().navigate(action)
        }

        binding.recyclerViewActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewActivities.adapter = adapter

        reloadActivities() // ✅ initial load
    }

    private fun reloadActivities() {
        val sessionId = SessionDetailFragmentArgs.fromBundle(requireArguments()).sessionId

        lifecycleScope.launch {
            val activities = withContext(Dispatchers.IO) {
                activityDao.getActivitiesForSession(sessionId)
                    .sortedBy { it.activity.timestamp }
            }
            adapter.submitList(activities)
        }
    }

    override fun onResume() {
        super.onResume()
        reloadActivities() // ✅ refresh after edit
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}