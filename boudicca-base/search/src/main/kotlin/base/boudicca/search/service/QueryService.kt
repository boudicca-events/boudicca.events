package base.boudicca.search.service

import base.boudicca.model.Entry
import base.boudicca.search.model.QueryDTO
import base.boudicca.search.model.ResultDTO
import base.boudicca.search.service.query.Evaluator
import base.boudicca.search.service.query.Page
import base.boudicca.search.service.query.QueryParser
import base.boudicca.search.service.query.evaluator.NoopEvaluator
import base.boudicca.search.service.query.evaluator.SimpleEvaluator
import base.boudicca.search.service.util.Utils
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class QueryService {

    @Volatile
    private var entries = emptyList<Entry>()

    @Volatile
    private var evaluator: Evaluator = NoopEvaluator()

    fun query(queryDTO: QueryDTO): ResultDTO {
        val query = queryDTO.query ?: return Utils.offset(entries, queryDTO.offset, queryDTO.size)

        return evaluateQuery(query, Page(queryDTO.offset ?: 0, queryDTO.size ?: 30))
    }

    @EventListener
    fun onEventsUpdate(event: EntriesUpdatedEvent) {
        this.entries = Utils.order(event.entries)
        this.evaluator = SimpleEvaluator(event.entries)
    }

    private fun evaluateQuery(query: String, page: Page): ResultDTO {
        val expression = QueryParser.parseQuery(query)
        return evaluator.evaluate(expression, page)
    }
}