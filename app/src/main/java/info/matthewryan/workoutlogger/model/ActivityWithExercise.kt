package info.matthewryan.workoutlogger.model

import androidx.room.Embedded
import androidx.room.Relation
import info.matthewryan.workoutlogger.model.Activity
import info.matthewryan.workoutlogger.model.Exercise

data class ActivityWithExercise(
    @Embedded val activity: Activity,

    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: Exercise
)
