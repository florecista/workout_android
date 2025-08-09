package info.matthewryan.workoutlogger.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.matthewryan.workoutlogger.dao.ActivityDao
import info.matthewryan.workoutlogger.dao.DailyVolumeRow
import info.matthewryan.workoutlogger.dao.ExerciseDao
import info.matthewryan.workoutlogger.model.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityViewModel(
    private val activityDao: ActivityDao,
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    // ---- UI state you already had ----
    private val _selectedExerciseName = MutableLiveData<String>()
    val selectedExerciseName: LiveData<String> = _selectedExerciseName

    private val _sets = MutableLiveData<String>()
    val sets: LiveData<String> = _sets

    private val _kg = MutableLiveData<String>()
    val kg: LiveData<String> = _kg

    private val _reps = MutableLiveData<String>()
    val reps: LiveData<String> = _reps

    fun updateSets(set: String) { _sets.value = set }
    fun updateKg(value: String) { _kg.value = value }
    fun updateReps(value: String) { _reps.value = value }

    // ---- Exercise selection (by id is canonical) ----
    private val _selectedExerciseId = MutableStateFlow<Int?>(null)
    val selectedExerciseId: StateFlow<Int?> = _selectedExerciseId.asStateFlow()

    /** Call this if your Spinner gives you the Exercise object */
    fun updateExercise(exercise: Exercise) {
        _selectedExerciseName.value = exercise.name
        _selectedExerciseId.value = exercise.id
    }

    /** Call this if your Spinner gives you the Exercise name */
    fun updateExerciseByName(name: String) {
        _selectedExerciseName.value = name
        viewModelScope.launch(Dispatchers.IO) {
            val ex = exerciseDao.getExerciseByName(name)
            _selectedExerciseId.value = ex?.id
        }
    }

    /** Or call this directly if you already have the id */
    fun updateExerciseId(id: Int) {
        _selectedExerciseId.value = id
        // optionally resolve name for display
        viewModelScope.launch(Dispatchers.IO) {
            val ex = exerciseDao.getExerciseById(id)
            if (ex != null) _selectedExerciseName.postValue(ex.name)
        }
    }

    // ---- Chart data: Daily volume rows for the selected exercise ----
    // When the selected exercise changes, we switch to that DAO Flow.
    val dailyVolumeRows: StateFlow<List<DailyVolumeRow>> =
        _selectedExerciseId
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { exerciseId ->
                // Room Flow emits on DB changes; no extra dispatcher needed
                activityDao.getDailyVolumeForExerciseFlow(exerciseId)
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Optional: expose a one-shot load if you prefer non-reactive calls somewhere
    suspend fun loadDailyVolumeOnce(exerciseId: Int): List<DailyVolumeRow> =
        withContext(Dispatchers.IO) {
            // If you add the non-Flow DAO method, you can call it here instead
            // activityDao.getDailyVolumeForExercise(exerciseId)
            emptyList() // placeholder if you don't keep the non-Flow version
        }

    // ---- Convenience: clear inputs after logging ----
    fun clearEntryFields() {
        _sets.value = ""
        _kg.value = ""
        _reps.value = ""
    }

    // ---- Factory (if you’re not using Hilt) ----
    class Factory(
        private val activityDao: ActivityDao,
        private val exerciseDao: ExerciseDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ActivityViewModel::class.java))
            return ActivityViewModel(activityDao, exerciseDao) as T
        }
    }
}
