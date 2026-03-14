package info.matthewryan.workoutlogger.data.importcsv

data class CsvImportPlan(
    val exerciseNames: Set<String>,
    val sessionDates: Set<String>,
    val activities: List<NormalizedImportRow>
)

class CsvImportPlanner {

    fun plan(rows: List<NormalizedImportRow>): CsvImportPlan {

        val exerciseNames = mutableSetOf<String>()
        val sessionDates = mutableSetOf<String>()

        rows.forEach { row ->

            exerciseNames.add(row.exerciseName)

            sessionDates.add(row.sessionDate)
        }

        return CsvImportPlan(
            exerciseNames = exerciseNames,
            sessionDates = sessionDates,
            activities = rows
        )
    }
}