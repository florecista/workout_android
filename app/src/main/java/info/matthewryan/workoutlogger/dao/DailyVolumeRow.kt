package info.matthewryan.workoutlogger.dao

data class DailyVolumeRow(
    val day: String,    // "YYYY-MM-DD" local time
    val volume: Double  // sum(weight * reps) for that day
)