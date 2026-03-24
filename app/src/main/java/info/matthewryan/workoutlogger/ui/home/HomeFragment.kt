package info.matthewryan.workoutlogger.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.R
import info.matthewryan.workoutlogger.databinding.FragmentHomeBinding
import info.matthewryan.workoutlogger.model.SessionWithActivities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        lifecycleScope.launch {
            val sessionWithActivities = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext())
                    .sessionDao()
                    .getLastSessionWithActivities()
            }

            sessionWithActivities?.let {
                showLastSession(it)
            }
        }

        // Start session button logic
        binding.startSessionButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_homeFragment_to_activityFragment)
        }

        return root
    }

    private fun showLastSession(sessionWithActivities: SessionWithActivities) {
        val session = sessionWithActivities.session

        // Date
        binding.lastSessionDate.text = session.getFormattedDate()

        // Rest days (improved wording)
        val daysSince = session.getDaysSince()
        binding.restDaysText.text = when {
            daysSince == 0 -> "Trained today"
            daysSince == 1 -> "Rested 1 day"
            daysSince < 30 -> "Rested $daysSince days"
            else -> "Last trained $daysSince days ago"
        }

        // Exercises (deduplicated)
        binding.exerciseList.removeAllViews()

        val exerciseNames = sessionWithActivities.activities
            .sortedBy { it.activity.timestamp }
            .map { it.exercise.name }
            .distinct()

        exerciseNames.take(5).forEach { name ->
            val tv = TextView(requireContext())
            tv.text = "• $name"
            tv.textSize = 14f
            binding.exerciseList.addView(tv)
        }

        // Optional: show "+X more"
        if (exerciseNames.size > 5) {
            val more = TextView(requireContext())
            more.text = "+${exerciseNames.size - 5} more"
            more.alpha = 0.7f
            binding.exerciseList.addView(more)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}