package info.matthewryan.workoutlogger.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActivityViewModel : ViewModel() {

    private val _selectedExercise = MutableLiveData<String>()
    val selectedExercise: LiveData<String> = _selectedExercise

    private val _sets = MutableLiveData<String>()
    val sets: LiveData<String> = _sets

    private val _kg = MutableLiveData<String>()
    val kg: LiveData<String> = _kg

    private val _reps = MutableLiveData<String>()
    val reps: LiveData<String> = _reps

    // Use this method to update the exercise and other data
    fun updateExercise(exercise: String) {
        _selectedExercise.value = exercise
    }

    fun updateSets(set: String) {
        _sets.value = set
    }

    fun updateKg(kg: String) {
        _kg.value = kg
    }

    fun updateReps(reps: String) {
        _reps.value = reps
    }
}
