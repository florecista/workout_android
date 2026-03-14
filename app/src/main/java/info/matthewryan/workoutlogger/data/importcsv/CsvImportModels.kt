package info.matthewryan.workoutlogger.data.importcsv

data class CsvImportRow(
    val lineNumber: Int,
    val date: String,
    val workoutName: String?,
    val exerciseName: String?,
    val reps: String?,
    val weightKg: String?,
    val weightLb: String?,
    val notes: String?,
    val duration: String?
)

data class NormalizedImportRow(
    val lineNumber: Int,
    val timestamp: Long,
    val sessionDate: String,
    val workoutName: String?,
    val exerciseName: String,
    val reps: Int,
    val weightKg: Double,
    val notes: String?,
    val durationSeconds: Int?
)