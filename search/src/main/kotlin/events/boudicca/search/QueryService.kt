package events.boudicca.search

import events.boudicca.SemanticKeys
import events.boudicca.search.model.Event
import events.boudicca.search.model.QueryDTO
import events.boudicca.search.query.Page
import events.boudicca.search.query.QueryParser
import events.boudicca.search.query.evaluator.SimpleEvaluator
import events.boudicca.search.util.Utils
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class QueryService @Inject constructor(
    private val synchronizationService: SynchronizationService
) {

    fun query(queryDTO: QueryDTO): List<Event> {
        if (queryDTO.query == null) {
            return Utils.offset(Utils.order(synchronizationService.getEvents()), queryDTO.offset)
        }

        return evaluateQuery(queryDTO.query, Page(queryDTO.offset ?: 0, 30))
    }

    private fun evaluateQuery(query: String, page: Page): List<Event> {
        val expression = QueryParser.parseQuery(query)
        val events = synchronizationService.getEvents()
        val filteredEvents = SimpleEvaluator(events.map { toMap(it) }).evaluate(expression, page)
        return filteredEvents.map { toEvent(it) }
    }

    private fun toEvent(event: Map<String, String>): Event {
        val data = event.toMutableMap()
        val name = event[SemanticKeys.NAME]!!
        val startDate = ZonedDateTime.parse(event[SemanticKeys.STARTDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
        data.remove(SemanticKeys.NAME)
        data.remove(SemanticKeys.STARTDATE)
        return Event(name, startDate, data)
    }

    private fun toMap(event: Event): Map<String, String> {
        return Utils.mapEventToMap(event)
    }

}