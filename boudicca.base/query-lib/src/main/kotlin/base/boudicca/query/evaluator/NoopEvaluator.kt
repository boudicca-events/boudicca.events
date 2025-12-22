package base.boudicca.query.evaluator

import base.boudicca.query.Expression

class NoopEvaluator : Evaluator {
    override fun evaluate(
        expression: Expression,
        page: Page,
    ): QueryResult = QueryResult(emptyList(), 0)
}
