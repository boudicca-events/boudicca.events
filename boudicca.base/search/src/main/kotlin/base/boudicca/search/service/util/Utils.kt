package base.boudicca.search.service.util

import base.boudicca.Event
import base.boudicca.search.model.SearchResultDTO
import java.time.OffsetDateTime
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
                    .comparing<Event?, OffsetDateTime?> { it.startDate }
                    .thenComparing(Function { it.name })
            )
    }
}