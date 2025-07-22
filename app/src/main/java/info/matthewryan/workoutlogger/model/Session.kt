package info.matthewryan.workoutlogger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimestamp: Long,
    val endTimestamp: Long? = null
) {
    override fun toString(): String {
        return "Session{id=$id, start=$startTimestamp, end=$endTimestamp}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Session) return false
        return id == other.id &&
                startTimestamp == other.startTimestamp &&
                endTimestamp == other.endTimestamp
    }

    override fun hashCode(): Int {
        val startHash = (startTimestamp xor (startTimestamp ushr 32)).toInt()
        val endHash = (endTimestamp?.let { it xor (it ushr 32) } ?: 0).toInt()
        return 31 * id.hashCode() + startHash + endHash
    }
}
