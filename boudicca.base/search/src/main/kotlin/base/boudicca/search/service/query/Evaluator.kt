package base.boudicca.search.service.query

import base.boudicca.api.search.model.ResultDTO

@FunctionalInterface
interface Evaluator {
    fun evaluate(expression: Expression, page: Page): ResultDTO
}