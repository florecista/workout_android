package info.matthewryan.workoutlogger.ui.history

import android.content.Context
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import info.matthewryan.workoutlogger.R

class SessionDayDecorator(
    private val dates: Set<CalendarDay>,
    context: Context
) : DayViewDecorator {

    private val dotSpan = DotSpan(4f, ContextCompat.getColor(context, R.color.teal_200))

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(dotSpan)
    }
}