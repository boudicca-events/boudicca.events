package events.boudicca.search.query.simple

import events.boudicca.search.query.Evaluator
import events.boudicca.search.query.Expression

class SimpleEvaluator(private val events: Collection<Map<String, String>>) : Evaluator {
    override fun evaluate(expression: Expression): Collection<Map<String, String>> {
        return events
    }
}