package events.boudicca.search

import events.boudicca.search.model.Event
import events.boudicca.search.model.QueryDTO
import events.boudicca.search.model.SearchResultDTO
import events.boudicca.search.query.Evaluator
import events.boudicca.search.query.Page
import events.boudicca.search.query.QueryParser
import events.boudicca.search.query.evaluator.NoopEvaluator
import events.boudicca.search.query.evaluator.SimpleEvaluator
import events.boudicca.search.util.Utils
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes

@ApplicationScoped
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

    fun onEventsUpdate(@Observes events: EventsUpdatedEvent) {
        this.events = Utils.order(events.events)
        this.evaluator = SimpleEvaluator(events.events)
    }

    private fun evaluateQuery(query: String, page: Page): SearchResultDTO {
        val expression = QueryParser.parseQuery(query)
        return evaluator.evaluate(expression, page)
    }


}