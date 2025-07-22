package info.matthewryan.workoutlogger.dao

import info.matthewryan.workoutlogger.model.Activity
import info.matthewryan.workoutlogger.model.Exercise
import info.matthewryan.workoutlogger.model.ExerciseType
import info.matthewryan.workoutlogger.model.PersonalBest

// File: ExerciseRepository.kt
object ExerciseRepository {

    fun calculatePersonalBests(exercise: Exercise, activities: List<Activity>): List<PersonalBest> {
        return when (exercise.type) {
            ExerciseType.STRENGTH -> {
                activities
                    .filter { it.reps > 0 && it.weight > 0 }
                    .groupBy { it.reps }
                    .mapNotNull { (_, group) ->
                        group.maxByOrNull { it.weight }?.let {
                            PersonalBest(
                                reps = it.reps,
                                weight = it.weight,
                                timestamp = it.timestamp,
                                exercise = exercise
                            )
                        }
                    }.sortedBy { it.reps }
            }

            ExerciseType.CARDIO -> {
                activities
                    .filter { it.distance != null && it.durationInSeconds != null }
                    .groupBy { it.distance }
                    .mapNotNull { (_, group) ->
                        group.minByOrNull { it.durationInSeconds!! }?.let {
                            PersonalBest(
                                distance = it.distance,
                                duration = it.durationInSeconds,
                                timestamp = it.timestamp,
                                exercise = exercise
                            )
                        }
                    }.sortedBy { it.distance }
            }
        }
    }
}
