package info.matthewryan.workoutlogger.ui.history

import androidx.recyclerview.widget.DiffUtil

class SessionDiffCallback : DiffUtil.ItemCallback<SessionDisplay>() {
    override fun areItemsTheSame(oldItem: SessionDisplay, newItem: SessionDisplay): Boolean {
        return oldItem.session.id == newItem.session.id
    }

    override fun areContentsTheSame(oldItem: SessionDisplay, newItem: SessionDisplay): Boolean {
        return oldItem == newItem
    }
}
