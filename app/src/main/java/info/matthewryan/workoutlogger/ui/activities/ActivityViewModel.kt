package info.matthewryan.workoutlogger.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.matthewryan.workoutlogger.dao.ActivityDao
import info.matthewryan.workoutlogger.dao.DailyVolumeRow
import info.matthewryan.workoutlogger.dao.ExerciseDao
import info.matthewryan.workoutlogger.model.Activity
import info.matthewryan.workoutlogger.model.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityViewModel(
    private val activityDao: ActivityDao,
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    private val _selectedExerciseName = MutableLiveData<String>()
    val selectedExerciseName: LiveData<String> = _selectedExerciseName

    private val _selectedExerciseId = MutableStateFlow<Int?>(null)
    val selectedExerciseId: StateFlow<Int?> = _selectedExerciseId.asStateFlow()

    fun updateExercise(exercise: Exercise) {
        _selectedExerciseName.value = exercise.name
        _selectedExerciseId.value = exercise.id
    }

    fun updateExerciseByName(name: String) {
        _selectedExerciseName.value = name
        viewModelScope.launch(Dispatchers.IO) {
            val ex = exerciseDao.getExerciseByName(name)
            _selectedExerciseId.value = ex?.id
        }
    }

    val dailyVolumeRows: StateFlow<List<DailyVolumeRow>> =
        _selectedExerciseId
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { exerciseId ->
                activityDao.getDailyVolumeForExerciseFlow(exerciseId)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun insertActivity(activity: Activity) {
        withContext(Dispatchers.IO) {
            activityDao.insert(activity)
        }
    }

    suspend fun countActivitiesInSession(sessionId: Long): Int =
        withContext(Dispatchers.IO) {
            activityDao.countActivitiesInSession(sessionId)
        }

    suspend fun countActivitiesByExerciseInSession(sessionId: Long, exerciseId: Int): Int =
        withContext(Dispatchers.IO) {
            activityDao.countActivitiesByExerciseInSession(sessionId, exerciseId)
        }

    suspend fun computeSessionVolume(sessionId: Long, exerciseId: Int): Double {
        val rows = withContext(Dispatchers.IO) {
            activityDao.getActivitiesForSession(sessionId)
        }

        return rows
            .filter { it.activity.exerciseId == exerciseId }
            .filter { it.activity.reps > 0 && it.activity.weight > 0.0 }
            .sumOf { it.activity.reps * it.activity.weight }
    }

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
