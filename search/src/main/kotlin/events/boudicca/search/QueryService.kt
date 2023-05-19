package events.boudicca.search

import events.boudicca.search.model.Event
import events.boudicca.search.model.QueryDTO
import events.boudicca.search.query.Evaluator
import events.boudicca.search.query.QueryParser
import events.boudicca.search.util.Utils
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

        val evaluator = parseQuery(queryDTO.query)
        return Utils.offset(Utils.order(filter(evaluator)), queryDTO.offset)
    }

    private fun parseQuery(query: String): Evaluator {
        return QueryParser(query).parse()
    }

    private fun filter(evaluator: Evaluator): Collection<Event> {
        return synchronizationService.getEvents().filter { evaluator.evaluate(it) }
    }

}