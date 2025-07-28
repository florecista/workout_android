package info.matthewryan.workoutlogger.ui.exercises

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.databinding.FragmentExerciseBinding
import info.matthewryan.workoutlogger.model.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExerciseFragment : Fragment() {

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!
    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var exerciseList: List<Exercise>

    private val exerciseDao by lazy { AppDatabase.getDatabase(requireContext()).exerciseDao() }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize RecyclerView
        val recyclerView: RecyclerView = binding.recyclerViewExercises
        recyclerView.layoutManager = LinearLayoutManager(context)
        exerciseAdapter = ExerciseAdapter(
            onClick = { exercise -> onExerciseClick(exercise) },
            onDelete = { exercise -> deleteExercise(exercise) }
        )
        recyclerView.adapter = exerciseAdapter

        // Load exercises from the database
        observeExercises()

        val editTextSearch = binding.editTextSearch

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterExercises(s.toString())

                val icon = if (s.isNullOrEmpty()) null
                else ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_close_clear_cancel)

                editTextSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, icon, null
                )
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

        binding.fabAddExercise.setOnClickListener {
            val action = ExerciseFragmentDirections
                .actionExerciseFragmentToExerciseDetailFragment(null)
            view?.findNavController()?.navigate(action)
        }

        return root
    }

    private fun deleteExercise(exercise: Exercise) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                exerciseDao.delete(exercise)
            }
        }
    }

    private fun filterExercises(query: String) {
        val filteredList = exerciseList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        exerciseAdapter.submitList(filteredList)
    }

    private fun observeExercises() {
        lifecycleScope.launch {
            exerciseDao.getAllExercisesFlow().collect { exercises ->
                exerciseList = exercises
                exerciseAdapter.submitList(exerciseList)
            }
        }
    }

    private fun onExerciseClick(exercise: Exercise) {
        val action = ExerciseFragmentDirections
            .actionExerciseFragmentToExerciseDetailFragment(exercise)
        view?.findNavController()?.navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}