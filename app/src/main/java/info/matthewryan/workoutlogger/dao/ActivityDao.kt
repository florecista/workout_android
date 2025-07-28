package info.matthewryan.workoutlogger.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import info.matthewryan.workoutlogger.model.Activity
import info.matthewryan.workoutlogger.model.ActivityWithExercise
import info.matthewryan.workoutlogger.model.Exercise
import info.matthewryan.workoutlogger.model.PersonalBest
import info.matthewryan.workoutlogger.model.VolumePerSession

@Dao
interface ActivityDao {

    @Insert
    fun insert(activity: Activity)

    @Query("SELECT * FROM activity WHERE id = :id LIMIT 1")
    fun getActivityById(id: Long): Activity?

    @Query("SELECT * FROM activity")
    fun getAllActivities(): List<Activity>

    @Transaction
    @Query("SELECT * FROM activity WHERE id = :id LIMIT 1")
    fun getActivityWithExercise(id: Long): Activity?

    @Transaction
    @Query("SELECT * FROM activity WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getActivitiesForSession(sessionId: Long): List<ActivityWithExercise>

    @Query("SELECT * FROM activity WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    fun getActivitiesForExercise(exerciseId: Int): List<Activity>

    @Query("SELECT COUNT(*) FROM activity WHERE sessionId = :sessionId AND exerciseId = :exerciseId")
    fun countActivitiesByExerciseInSession(sessionId: Long, exerciseId: Int): Int

    @Delete
    fun deleteActivity(activity: Activity)

    @Query("SELECT COUNT(*) FROM activity WHERE sessionId = :sessionId")
    fun countActivitiesInSession(sessionId: Long): Int

    @Query("""
        SELECT sessionId, SUM(reps * weight) AS totalVolume, MIN(timestamp) as date
        FROM activity
        WHERE exerciseId = :exerciseId
        GROUP BY sessionId
        ORDER BY date
    """)
    fun getVolumePerSessionForExercise(exerciseId: Int): List<VolumePerSession>

    @Query("DELETE FROM activity")
    fun deleteAll()
}
