package info.matthewryan.workoutlogger.data.importcsv

data class CsvImportReport(
    val rowsRead: Int,
    val validRows: Int,
    val sessionsCreated: Int,
    val exercisesCreated: Int,
    val activitiesCreated: Int,
    val warnings: List<String>,
    val errors: List<String>
)