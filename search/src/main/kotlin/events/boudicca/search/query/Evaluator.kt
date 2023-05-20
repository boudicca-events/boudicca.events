package events.boudicca.search.query


@FunctionalInterface
interface Evaluator {
    fun evaluate(expression: Expression): Collection<Map<String, String>>
}