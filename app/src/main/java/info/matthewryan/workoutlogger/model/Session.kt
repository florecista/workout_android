package info.matthewryan.workoutlogger.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Entity(tableName = "session")
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimestamp: Long,
    val endTimestamp: Long? = null
) {

    // --- UI HELPERS ---

    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        return sdf.format(Date(startTimestamp))
    }

    fun getDaysSince(): Int {
        val diff = System.currentTimeMillis() - startTimestamp
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    fun getDurationMinutes(): Long? {
        return endTimestamp?.let {
            TimeUnit.MILLISECONDS.toMinutes(it - startTimestamp)
        }
    }

    // --- EXISTING METHODS ---

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