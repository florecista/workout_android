package info.matthewryan.workoutlogger.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,  // Room will auto-generate the ID if it's not set
    val exerciseId: Int,
    val reps: Int,
    val weight: Double,
    val timestamp: Long,
    val sessionId: Long
) {
    override fun toString(): String {
        return "Activity{id=$id, exerciseId=$exerciseId, reps=$reps, weight=$weight, timestamp=$timestamp, sessionId=$sessionId}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as Activity
        return id == other.id &&
                exerciseId == other.exerciseId &&
                reps == other.reps &&
                weight == other.weight &&
                timestamp == other.timestamp &&
                sessionId == other.sessionId
    }

    override fun hashCode(): Int {
        val idHash = (id xor (id ushr 32)).toInt()
        val timestampHash = (timestamp xor (timestamp ushr 32)).toInt()
        val sessionIdHash = (sessionId xor (sessionId ushr 32)).toInt()

        return 31 * (31 * (31 * (31 * (31 + idHash) + exerciseId) + reps) + weight.hashCode()) + timestampHash + sessionIdHash
    }
}
