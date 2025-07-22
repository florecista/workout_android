package info.matthewryan.workoutlogger.model

data class VolumePerSession(
    val sessionId: Long,
    val totalVolume: Double,
    val date: Long
)
