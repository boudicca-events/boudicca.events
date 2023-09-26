package events.boudicca.search.service.query.evaluator

import events.boudicca.SemanticKeys
import events.boudicca.search.model.Event
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

object EvaluatorUtil {

    fun toEvent(event: Map<String, String>): Event {
        val data = event.toMutableMap()
        val name = event[SemanticKeys.NAME]!!
        val startDate = ZonedDateTime.parse(event[SemanticKeys.STARTDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
        data.remove(SemanticKeys.NAME)
        data.remove(SemanticKeys.STARTDATE)
        return Event(name, startDate, data)
    }

    fun mapEventToMap(event: Event): Map<String, String> {
        val data = event.data?.toMutableMap() ?: mutableMapOf()
        data[SemanticKeys.NAME] = event.name
        data[SemanticKeys.STARTDATE] = event.startDate.format(DateTimeFormatter.ISO_DATE_TIME)
        return data
    }


    fun getDuration(event: Map<String, String>): Double {
        if (!event.containsKey(SemanticKeys.STARTDATE) || !event.containsKey(SemanticKeys.ENDDATE)) {
            return 0.0
        }
        try {
            val startDate = OffsetDateTime.parse(event[SemanticKeys.STARTDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
            val endDate = OffsetDateTime.parse(event[SemanticKeys.ENDDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
            return Duration.of(endDate.toEpochSecond() - startDate.toEpochSecond(), ChronoUnit.SECONDS)
                .toMillis()
                .toDouble() / 1000.0 / 60.0 / 60.0
        } catch (e: DateTimeParseException) {
            return 0.0
        }
    }
}