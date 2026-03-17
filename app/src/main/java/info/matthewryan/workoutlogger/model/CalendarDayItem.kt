package info.matthewryan.workoutlogger.model

data class CalendarDayItem(
    val date: java.time.LocalDate,
    val isCurrentMonth: Boolean,
    val hasWorkout: Boolean
)