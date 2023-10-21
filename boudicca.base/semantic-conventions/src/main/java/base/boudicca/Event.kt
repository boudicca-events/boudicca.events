package base.boudicca

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class Event(
    val name: String,
    val startDate: OffsetDateTime,
    val data: Map<String, String> = mapOf()
) {
    companion object {
        fun toEntry(event: Event): Entry {
            val entry = event.data.toMutableMap()
            entry[SemanticKeys.NAME] = event.name
            entry[SemanticKeys.STARTDATE] = DateTimeFormatter.ISO_DATE_TIME.format(event.startDate)
            return entry
        }

        fun fromEntry(entry: Entry): Event? {
            if (!entry.containsKey(SemanticKeys.NAME) || !entry.containsKey(SemanticKeys.STARTDATE)) {
                return null
            }
            val name = entry[SemanticKeys.NAME]!!
            val startDateString = entry[SemanticKeys.STARTDATE]
            val startDate = try {
                OffsetDateTime.parse(startDateString, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: DateTimeParseException) {
                return null
            }
            val data = entry.toMutableMap()
            data.remove(SemanticKeys.NAME)
            data.remove(SemanticKeys.STARTDATE)
            return Event(name, startDate, data)
        }
    }
}