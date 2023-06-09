package events.boudicca.search.util

import events.boudicca.SemanticKeys
import events.boudicca.search.model.Event
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
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

    fun mapEventToMap(event: Event): Map<String, String> {
        return mapEventToMap(event.name, event.startDate.toOffsetDateTime(), event.data)
    }

    fun mapEventToMap(event: events.boudicca.openapi.model.Event): Map<String, String> {
        return mapEventToMap(event.name, event.startDate, event.data)
    }

    private fun mapEventToMap(
        name: String,
        startDate: OffsetDateTime,
        data: Map<String, String>?
    ): Map<String, String> {
        val data = data?.toMutableMap() ?: mutableMapOf()
        data[SemanticKeys.NAME] = name
        data[SemanticKeys.STARTDATE] = startDate.format(DateTimeFormatter.ISO_DATE_TIME)
        return data
    }
}