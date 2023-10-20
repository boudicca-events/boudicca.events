package base.boudicca.search.service.util

import base.boudicca.search.model.Event
import base.boudicca.search.model.SearchResultDTO
import java.time.ZonedDateTime
import java.util.function.Function

object Utils {
    private const val ROWS = 30

    fun offset(events: List<Event>, offset: Int?): SearchResultDTO {
        return SearchResultDTO(events.drop(offset ?: 0).take(ROWS), events.size)
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