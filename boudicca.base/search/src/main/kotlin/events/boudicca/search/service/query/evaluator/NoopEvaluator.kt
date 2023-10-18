package events.boudicca.search.service.query.evaluator

import events.boudicca.search.model.SearchResultDTO
import events.boudicca.search.service.query.Evaluator
import events.boudicca.search.service.query.Expression
import events.boudicca.search.service.query.Page

class NoopEvaluator : Evaluator {
    override fun evaluate(expression: Expression, page: Page): SearchResultDTO {
        return SearchResultDTO(emptyList(), 0)
    }
}
