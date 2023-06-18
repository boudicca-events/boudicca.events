package events.boudicca.search.query.evaluator

import events.boudicca.SemanticKeys
import events.boudicca.search.model.Event
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
}