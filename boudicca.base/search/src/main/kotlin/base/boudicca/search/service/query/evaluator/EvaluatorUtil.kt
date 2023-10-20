package base.boudicca.search.service.query.evaluator

import base.boudicca.SemanticKeys
import base.boudicca.search.model.Event
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

object EvaluatorUtil {

    fun toEvent(event: Map<String, String>): Event {
        val data = event.toMutableMap()
        val name = event[base.boudicca.SemanticKeys.NAME]!!
        val startDate = ZonedDateTime.parse(event[base.boudicca.SemanticKeys.STARTDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
        data.remove(base.boudicca.SemanticKeys.NAME)
        data.remove(base.boudicca.SemanticKeys.STARTDATE)
        return Event(name, startDate, data)
    }

    fun mapEventToMap(event: Event): Map<String, String> {
        val data = event.data?.toMutableMap() ?: mutableMapOf()
        data[base.boudicca.SemanticKeys.NAME] = event.name
        data[base.boudicca.SemanticKeys.STARTDATE] = event.startDate.format(DateTimeFormatter.ISO_DATE_TIME)
        return data
    }


    fun getDuration(event: Map<String, String>): Double {
        if (!event.containsKey(base.boudicca.SemanticKeys.STARTDATE) || !event.containsKey(base.boudicca.SemanticKeys.ENDDATE)) {
            return 0.0
        }
        return try {
            val startDate = OffsetDateTime.parse(event[base.boudicca.SemanticKeys.STARTDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
            val endDate = OffsetDateTime.parse(event[base.boudicca.SemanticKeys.ENDDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
            Duration.of(endDate.toEpochSecond() - startDate.toEpochSecond(), ChronoUnit.SECONDS)
                .toMillis()
                .toDouble() / 1000.0 / 60.0 / 60.0
        } catch (e: DateTimeParseException) {
            0.0
        }
    }
}