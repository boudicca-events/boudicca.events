package base.boudicca.search.service

import base.boudicca.api.search.model.QueryDTO
import base.boudicca.api.search.model.ResultDTO
import base.boudicca.model.Entry
import base.boudicca.query.BoudiccaQueryRunner
import base.boudicca.query.QueryException
import base.boudicca.query.Utils
import base.boudicca.query.evaluator.Evaluator
import base.boudicca.query.evaluator.NoopEvaluator
import base.boudicca.query.evaluator.OptimizingEvaluator
import base.boudicca.query.evaluator.Page
import base.boudicca.query.evaluator.SimpleEvaluator
import base.boudicca.search.BoudiccaSearchProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

private const val DEFAULT_PAGE_SIZE = 30

@Service
class QueryService @Autowired constructor(
    private val boudiccaSearchProperties: BoudiccaSearchProperties, private val openTelemetry: OpenTelemetry
) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private var clock = Clock.system(ZoneId.of("Europe/Vienna")) //TODO make configurable
    }

    @Volatile
    private var entries = emptyList<Entry>()

    @Volatile
    private var evaluator: Evaluator = NoopEvaluator()

    @Throws(QueryException::class)
    fun query(queryDTO: QueryDTO): ResultDTO {
        if (queryDTO.query.isNullOrEmpty()) {
            return ResultDTO(Utils.offset(entries, queryDTO.offset, queryDTO.size), entries.size)
        }
        return evaluateQuery(queryDTO.query!!, Page(queryDTO.offset ?: 0, queryDTO.size ?: DEFAULT_PAGE_SIZE))
    }

    @EventListener
    fun onEventsUpdate(event: EntriesUpdatedEvent) {
        this.entries = Utils.order(event.entries, ConcurrentHashMap())
        if (boudiccaSearchProperties.devMode) {
            //for local mode we only want the simple, the optimizing has quite some startup
            this.evaluator = SimpleEvaluator(event.entries, clock)
        } else {
            val optimizingEvaluator = OptimizingEvaluator(event.entries, clock)
            val fallbackEvaluator = SimpleEvaluator(event.entries, clock)
            this.evaluator = Evaluator { expression, page ->
                try {
                    optimizingEvaluator.evaluate(expression, page)
                } catch (e: Exception) {
                    logger.error(e) { "optimizing evaluator threw exception" }
                    fallbackEvaluator.evaluate(expression, page)
                }

            }
        }
    }

    private fun evaluateQuery(query: String, page: Page): ResultDTO {
        return try {
            val queryResult = BoudiccaQueryRunner.evaluateQuery(query, page, evaluator, openTelemetry)
            ResultDTO(queryResult.result, queryResult.totalResults, queryResult.error)
        } catch (e: QueryException) {
            //TODO this should return a 400 error or something, not a 200 message with an error message...
            ResultDTO(emptyList(), 0, e.message)
        }
    }
}
