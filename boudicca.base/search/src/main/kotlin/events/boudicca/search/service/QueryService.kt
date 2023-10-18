package events.boudicca.search.service

import events.boudicca.search.model.Event
import events.boudicca.search.model.QueryDTO
import events.boudicca.search.model.SearchResultDTO
import events.boudicca.search.service.query.Evaluator
import events.boudicca.search.service.query.Page
import events.boudicca.search.service.query.QueryParser
import events.boudicca.search.service.query.evaluator.NoopEvaluator
import events.boudicca.search.service.query.evaluator.SimpleEvaluator
import events.boudicca.search.service.util.Utils
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class QueryService {

    @Volatile
    private var events = emptyList<Event>()

    @Volatile
    private var evaluator: Evaluator = NoopEvaluator()

    fun query(queryDTO: QueryDTO): SearchResultDTO {
        if (queryDTO.query == null) {
            return Utils.offset(Utils.order(events), queryDTO.offset)
        }

        return evaluateQuery(queryDTO.query, Page(queryDTO.offset ?: 0, queryDTO.size ?: 30))
    }

    @EventListener
    fun onEventsUpdate(event: EventsUpdatedEvent) {
        this.events = Utils.order(event.events)
        this.evaluator = SimpleEvaluator(event.events)
    }

    private fun evaluateQuery(query: String, page: Page): SearchResultDTO {
        val expression = QueryParser.parseQuery(query)
        return evaluator.evaluate(expression, page)
    }
}