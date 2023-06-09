package events.boudicca.search

import events.boudicca.SemanticKeys
import events.boudicca.search.model.Event
import events.boudicca.search.model.QueryDTO
import events.boudicca.search.query.Evaluator
import events.boudicca.search.query.Page
import events.boudicca.search.query.QueryParser
import events.boudicca.search.query.evaluator.NoopEvaluator
import events.boudicca.search.query.evaluator.SimpleEvaluator
import events.boudicca.search.util.Utils
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes

@ApplicationScoped
class QueryService {

    @Volatile
    private var events = emptyList<Event>()

    @Volatile
    private var evaluator: Evaluator = NoopEvaluator()

    fun query(queryDTO: QueryDTO): List<Event> {
        if (queryDTO.query == null) {
            return Utils.offset(Utils.order(events), queryDTO.offset)
        }

        return evaluateQuery(queryDTO.query, Page(queryDTO.offset ?: 0, 30))
    }

    fun onEventsUpdate(@Observes events: EventsUpdatedEvent) {
        this.events = Utils.order(events.events)
        this.evaluator = SimpleEvaluator(events.events.map { toMap(it) })
    }

    private fun evaluateQuery(query: String, page: Page): List<Event> {
        val expression = QueryParser.parseQuery(query)
        val filteredEvents = evaluator.evaluate(expression, page)
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