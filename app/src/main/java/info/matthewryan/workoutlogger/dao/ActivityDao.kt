package info.matthewryan.workoutlogger.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import info.matthewryan.workoutlogger.model.Activity
import info.matthewryan.workoutlogger.model.ActivityWithExercise
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

    @Query("""
        SELECT 
            strftime('%Y-%m-%d', datetime(timestamp/1000, 'unixepoch', 'localtime')) AS day,
            SUM(reps * weight) AS volume
        FROM activity
        WHERE exerciseId = :exerciseId
          AND reps > 0
          AND weight > 0
        GROUP BY day
        ORDER BY day
    """)
    fun getDailyVolumeForExercise(exerciseId: Int): List<DailyVolumeRow>

    // If you prefer reactive updates, expose it as Flow as well:
    @Query("""
    SELECT 
        date(timestamp/1000, 'unixepoch', 'localtime') AS day,
        SUM(CAST(weight AS REAL) * CAST(reps AS REAL)) AS volume
    FROM activity
    WHERE exerciseId = :exerciseId
    GROUP BY day
    ORDER BY day
""")
    fun getDailyVolumeForExerciseFlow(exerciseId: Int): kotlinx.coroutines.flow.Flow<List<DailyVolumeRow>>

    @Query("""
    SELECT COALESCE(SUM(reps * weight), 0)
    FROM activity
    WHERE exerciseId = :exerciseId AND sessionId = :sessionId
""")
    fun getSessionVolumeForExercise(sessionId: Long, exerciseId: Int): Double

}
