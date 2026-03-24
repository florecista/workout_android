package info.matthewryan.workoutlogger.model

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithActivities(
    @Embedded val session: Session,

    @Relation(
        entity = Activity::class,
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val activities: List<ActivityWithExercise>
)