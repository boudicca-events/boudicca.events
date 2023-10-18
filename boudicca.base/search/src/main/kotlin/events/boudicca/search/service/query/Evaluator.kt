package events.boudicca.search.service.query

import events.boudicca.search.model.SearchResultDTO

@FunctionalInterface
interface Evaluator {
    fun evaluate(expression: Expression, page: Page): SearchResultDTO
}