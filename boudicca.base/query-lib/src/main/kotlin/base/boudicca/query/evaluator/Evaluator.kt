package base.boudicca.query.evaluator

import base.boudicca.query.Expression

fun interface Evaluator {
    fun evaluate(
        expression: Expression,
        page: Page,
    ): QueryResult
}
