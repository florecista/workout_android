package info.matthewryan.workoutlogger.ui.progress

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentProgressBinding
import info.matthewryan.workoutlogger.model.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!
    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var exerciseList: List<Exercise>

    private val exerciseDao by lazy { AppDatabase.getDatabase(requireContext()).exerciseDao() }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize RecyclerView
        val recyclerView: RecyclerView = binding.recyclerViewExercises
        recyclerView.layoutManager = LinearLayoutManager(context)
        exerciseAdapter = ExerciseAdapter { exercise -> onExerciseClick(exercise) }
        recyclerView.adapter = exerciseAdapter

        // Load exercises from the database
        loadExercises()

        val editTextSearch = binding.editTextSearch

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterExercises(s.toString())

                // Show clear icon when there's text
                val icon = if (s.isNullOrEmpty()) null
                else ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_close_clear_cancel)

                editTextSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        editTextSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editTextSearch.compoundDrawablesRelative[2]
                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()
                    val touchAreaStart = editTextSearch.width - editTextSearch.paddingEnd - drawableWidth
                    if (event.x >= touchAreaStart) {
                        editTextSearch.text?.clear()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        return root
    }

    private fun filterExercises(query: String) {
        val filtered = exerciseList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        exerciseAdapter.submitList(filtered)
    }

    private fun loadExercises() {
        lifecycleScope.launch {
            // Fetch exercises from the database in the background thread
            val exercisesFromDb = withContext(Dispatchers.IO) {
                exerciseDao.getAllExercises()
            }

            // Map the exercises to get the full list
            exerciseList = exercisesFromDb

            // Update the adapter with the fetched exercises
            exerciseAdapter.submitList(exerciseList)
        }
    }

    private fun onExerciseClick(exercise: Exercise) {
        // Use Safe Args to create the action and navigate to ProgressDetailFragment
        val action = ProgressFragmentDirections.actionProgressFragmentToProgressDetailFragment(exercise)
        view?.findNavController()?.navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
