package info.matthewryan.workoutlogger.data.importcsv

import info.matthewryan.workoutlogger.AppDatabase

class ImportVerifier(private val db: AppDatabase) {

    suspend fun verify(expectedRows: Int): String {

        val activities = db.activityDao().getAllActivities()
        val sessions = db.sessionDao().getAllSessions()
        val exercises = db.exerciseDao().getAllExercises()

        val activityCount = activities.size
        val sessionCount = sessions.size
        val exerciseCount = exercises.size

        return buildString {

            appendLine("=== Import Verification ===")
            appendLine("Expected CSV rows: $expectedRows")
            appendLine("Activities in DB: $activityCount")

            if (activityCount == expectedRows)
                appendLine("✔ Activity count matches CSV")
            else
                appendLine("✘ Activity count mismatch")

            appendLine("")
            appendLine("Sessions created: $sessionCount")
            appendLine("Exercises in database: $exerciseCount")
        }
    }
}