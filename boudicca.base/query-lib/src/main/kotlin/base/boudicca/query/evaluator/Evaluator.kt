package base.boudicca.query.evaluator

import base.boudicca.query.Expression

@FunctionalInterface
interface Evaluator {
    fun evaluate(expression: Expression, page: Page): QueryResult
}
