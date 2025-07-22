package info.matthewryan.workoutlogger.ui.history

import info.matthewryan.workoutlogger.model.Session

data class SessionDisplay(
    val session: Session,
    val exerciseNames: List<String>
)
