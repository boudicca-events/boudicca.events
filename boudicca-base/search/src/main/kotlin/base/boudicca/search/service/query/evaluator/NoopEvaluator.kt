package base.boudicca.search.service.query.evaluator

import base.boudicca.search.model.ResultDTO
import base.boudicca.search.service.query.Evaluator
import base.boudicca.search.service.query.Expression
import base.boudicca.search.service.query.Page

class NoopEvaluator : Evaluator {
    override fun evaluate(expression: Expression, page: Page): ResultDTO {
        return ResultDTO(emptyList(), 0)
    }
}
