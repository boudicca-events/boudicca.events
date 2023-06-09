package events.boudicca.search.query

@FunctionalInterface
interface Evaluator {
    fun evaluate(expression: Expression, page: Page): List<Map<String, String>>
}