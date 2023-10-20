package base.boudicca.search.service.query

import base.boudicca.search.model.SearchResultDTO

@FunctionalInterface
interface Evaluator {
    fun evaluate(expression: Expression, page: Page): SearchResultDTO
}