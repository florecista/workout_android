package info.matthewryan.workoutlogger.dao

import androidx.room.*
import info.matthewryan.workoutlogger.model.Session
import info.matthewryan.workoutlogger.model.SessionWithActivities

@Dao
interface SessionDao {

    @Query("SELECT * FROM session_table WHERE id = :sessionId LIMIT 1")
    fun getSessionById(sessionId: Long): Session?

    @Transaction
    @Query("SELECT * FROM session_table")
    fun getAllSessions(): List<SessionWithActivities>

    @Transaction
    @Query("SELECT * FROM session_table WHERE id = :sessionId")
    fun getSessionWithActivities(sessionId: Long): SessionWithActivities?

    @Insert
    fun insertSession(session: Session): Long

    @Insert
    fun insert(session: Session): Long

    @Delete
    fun deleteSession(session: Session)

    // 🔴 Add this method:
    @Query("DELETE FROM session_table WHERE id = :sessionId")
    fun deleteSessionById(sessionId: Long)

    @Query("UPDATE session_table SET endTimestamp = :endTime WHERE id = :sessionId")
    fun markSessionAsEnded(sessionId: Long, endTime: Long)

}
