package events.boudicca.search.query.evaluator

import events.boudicca.search.query.Evaluator
import events.boudicca.search.query.Expression
import events.boudicca.search.query.Page

class NoopEvaluator : Evaluator {
    override fun evaluate(expression: Expression, page: Page): List<Map<String, String>> {
        return emptyList()
    }
}
