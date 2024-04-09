package base.boudicca.search.service

import base.boudicca.api.search.model.QueryDTO
import base.boudicca.api.search.model.ResultDTO
import base.boudicca.model.Entry
import base.boudicca.query.BoudiccaQueryRunner
import base.boudicca.query.QueryException
import base.boudicca.query.Utils
import base.boudicca.query.evaluator.Evaluator
import base.boudicca.query.evaluator.NoopEvaluator
import base.boudicca.query.evaluator.Page
import base.boudicca.query.evaluator.SimpleEvaluator
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class QueryService {

    @Volatile
    private var entries = emptyList<Entry>()

    @Volatile
    private var evaluator: Evaluator = NoopEvaluator()

    @Throws(QueryException::class)
    fun query(queryDTO: QueryDTO): ResultDTO {
        if (queryDTO.query.isNullOrEmpty()) {
            return ResultDTO(Utils.offset(entries, queryDTO.offset, queryDTO.size), entries.size)
        }
        return evaluateQuery(queryDTO.query!!, Page(queryDTO.offset ?: 0, queryDTO.size ?: 30))
    }

    @EventListener
    fun onEventsUpdate(event: EntriesUpdatedEvent) {
        this.entries = Utils.order(event.entries)
        this.evaluator = SimpleEvaluator(event.entries)
    }

    private fun evaluateQuery(query: String, page: Page): ResultDTO {
        return try {
            val expression = BoudiccaQueryRunner.parseQuery(query)
            val queryResult = evaluator.evaluate(expression, page)
            ResultDTO(queryResult.result, queryResult.totalResults, queryResult.error)
        } catch (e: QueryException) {
            //TODO this should return a 400 error or something, not a 200 message with an error message...
            ResultDTO(emptyList(), 0, e.message)
        }
    }
}