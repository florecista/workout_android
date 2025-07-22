package info.matthewryan.workoutlogger.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
    private val exerciseDao by lazy { AppDatabase.getDatabase(requireContext()).exerciseDao() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sessionId = SessionDetailFragmentArgs.fromBundle(requireArguments()).sessionId

        val adapter = SessionActivityAdapter()
        binding.recyclerViewActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewActivities.adapter = adapter

        lifecycleScope.launch {
            val activities = withContext(Dispatchers.IO) {
                activityDao.getActivitiesForSession(sessionId).sortedBy { it.activity.timestamp }
            }
            adapter.submitList(activities)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
