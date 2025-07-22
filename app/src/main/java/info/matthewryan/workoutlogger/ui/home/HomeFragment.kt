package info.matthewryan.workoutlogger.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.R
import info.matthewryan.workoutlogger.databinding.FragmentHomeBinding
import info.matthewryan.workoutlogger.model.Session
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

        // Start session button logic
        binding.startSessionButton.setOnClickListener {
            lifecycleScope.launch {
                val newSession = Session(startTimestamp = System.currentTimeMillis())

                val db = AppDatabase.getDatabase(requireContext())
                val sessionId = withContext(Dispatchers.IO) {
                    db.sessionDao().insert(newSession)
                }

                val bundle = Bundle().apply {
                    putLong("sessionId", sessionId)
                }
                Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_activityFragment, bundle)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
