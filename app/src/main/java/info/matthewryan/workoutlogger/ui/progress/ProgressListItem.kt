package info.matthewryan.workoutlogger.ui.progress

import info.matthewryan.workoutlogger.model.PersonalBest

sealed class ProgressListItem {
    data class Header(val reps: Int) : ProgressListItem()
    data class Item(val personalBest: PersonalBest) : ProgressListItem()
}