package info.matthewryan.workoutlogger.ui.history

sealed class ExerciseHistoryRow {

    data class DateHeader(
        val date: String
    ) : ExerciseHistoryRow()

    data class ActivityItem(
        val time: String,
        val exerciseName: String,
        val weight: Double,
        val reps: Int,
        val setIndex: Int,
        val isPr: Boolean
    ) : ExerciseHistoryRow()
}