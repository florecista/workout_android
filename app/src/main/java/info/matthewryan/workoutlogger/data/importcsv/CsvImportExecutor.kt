package info.matthewryan.workoutlogger.data.importcsv

import androidx.room.withTransaction
import info.matthewryan.workoutlogger.AppDatabase
import info.matthewryan.workoutlogger.model.Activity
import info.matthewryan.workoutlogger.model.Exercise
import info.matthewryan.workoutlogger.model.Session

class CsvImportExecutor(
    private val db: AppDatabase
) {

    suspend fun execute(plan: CsvImportPlan): CsvImportReport {

        return db.withTransaction {

            val exerciseDao = db.exerciseDao()
            val sessionDao = db.sessionDao()
            val activityDao = db.activityDao()

            // Load existing exercises
            val exerciseMap = exerciseDao.getAllExercises()
                .associateBy { it.name }
                .toMutableMap()

            var exercisesCreated = 0

            // Create missing exercises
            plan.exerciseNames.forEach { name ->

                if (!exerciseMap.containsKey(name)) {

                    val exercise = Exercise(
                        name = name,
                        factory = false,
                        isUnilateral = false,
                        isTimed = false,
                        duration = null
                    )

                    val id = exerciseDao.insert(exercise)

                    exerciseMap[name] = exercise.copy(id = id.toInt())

                    exercisesCreated++
                }
            }

            // Group activities by session date (performance improvement)
            val activitiesBySession = plan.activities.groupBy { it.sessionDate }

            val sessionMap = mutableMapOf<String, Long>()

            // Create sessions
            activitiesBySession.forEach { (date, rows) ->

                val start = rows.minOf { it.timestamp }
                val end = rows.maxOf { it.timestamp }

                val sessionId = sessionDao.insert(
                    Session(
                        startTimestamp = start,
                        endTimestamp = end
                    )
                )

                sessionMap[date] = sessionId
            }

            // Create activities
            val activities = plan.activities.map { row ->

                val exercise = exerciseMap[row.exerciseName]!!

                Activity(
                    exerciseId = exercise.id,
                    reps = row.reps,
                    weight = row.weightKg,
                    timestamp = row.timestamp,
                    sessionId = sessionMap[row.sessionDate]!!
                )
            }

            activityDao.insertAll(activities)

            CsvImportReport(
                rowsRead = plan.activities.size,
                validRows = plan.activities.size,
                sessionsCreated = sessionMap.size,
                exercisesCreated = exercisesCreated,
                activitiesCreated = activities.size,
                warnings = emptyList(),
                errors = emptyList()
            )
        }
    }
}