package info.matthewryan.workoutlogger.model

data class PersonalBest(
    val reps: Int = 0,
    val weight: Double = 0.0,
    val distance: Double? = null,
    val duration: Int? = null,
    val timestamp: Long,
    val exercise: Exercise? = null
)
