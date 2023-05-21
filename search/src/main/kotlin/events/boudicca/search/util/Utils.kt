package events.boudicca.search.util

import events.boudicca.search.model.Event
import events.boudicca.search.model.SearchDTO
import java.time.ZonedDateTime
import java.util.function.Function

object Utils {
    private const val ROWS = 30

    fun offset(events: List<Event>, offset: Int?): List<Event> {
        return events.drop(offset ?: 0).take(ROWS)
    }

    fun order(events: Collection<Event>): List<Event> {
        return events
            .toList()
            .sortedWith(
                Comparator
                    .comparing<Event?, ZonedDateTime?> { it.startDate }
                    .thenComparing(Function { it.name })
            )
    }

}