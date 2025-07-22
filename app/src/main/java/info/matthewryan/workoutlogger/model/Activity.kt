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
    val sessionId: Long,
    val distance: Double? = null,        // in kilometers
    val durationInSeconds: Int? = null   // duration stored as total seconds
) {
    override fun toString(): String {
        return "Activity{id=$id, exerciseId=$exerciseId, reps=$reps, weight=$weight, timestamp=$timestamp, sessionId=$sessionId, distance=$distance, durationInSeconds=$durationInSeconds}"
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
                sessionId == other.sessionId &&
                distance == other.distance &&
                durationInSeconds == other.durationInSeconds
    }

    override fun hashCode(): Int {
        val idHash = (id xor (id ushr 32)).toInt()
        val timestampHash = (timestamp xor (timestamp ushr 32)).toInt()
        val sessionIdHash = (sessionId xor (sessionId ushr 32)).toInt()

        var result = 31 * (31 * (31 * (31 * (31 + idHash) + exerciseId) + reps) + weight.hashCode())
        result = 31 * result + timestampHash
        result = 31 * result + sessionIdHash
        result = 31 * result + (distance?.hashCode() ?: 0)
        result = 31 * result + (durationInSeconds ?: 0)
        return result
    }
}
