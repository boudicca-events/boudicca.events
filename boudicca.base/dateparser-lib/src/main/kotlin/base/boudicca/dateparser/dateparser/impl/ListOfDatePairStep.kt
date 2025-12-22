package base.boudicca.dateparser.dateparser.impl

import base.boudicca.dateparser.dateparser.DateParserConfig

internal class ListOfDatePairStep(private val config: DateParserConfig, private val debugTracing: DebugTracing, private val tokens: Tokens) {
    @Suppress("ReturnCount")
    fun solve(): ListOfDatePairSolution? {
        // TODO think if this could result in wrong results when we have the no list handling as first
        var result =
            trySolve(
                debugTracing.startOperationWithChild("trying no list handling", tokens),
                listOf(
                    tokens,
                ),
            )
        debugTracing.endOperation(result)
        if (result != null) {
            return result
        }

        val listIndexes = listOf(0) + findAllListIndexes(tokens) + listOf(tokens.tokens.size)
        if (listIndexes.isEmpty()) {
            return null
        }

        val subListIndexes = listIndexes.windowed(2)
        val subLists =
            subListIndexes.map { (start, end) ->
                Tokens(tokens.tokens.subList(start, end))
            }.filter { it.isInteresting() }
        if (subLists.size < 2) {
            return null
        }
        val childDebugTracing = debugTracing.startOperationWithChild("trying list with children", subLists)
        result = trySolve(childDebugTracing, subLists)
        debugTracing.endOperation(result)
        return result
    }

    private fun findAllListIndexes(tokens: Tokens): List<Int> = tokens.tokens.mapIndexed { i, component -> Pair(i, component) }
        .filter { (_, component) -> isList(component) }
        .map { it.first }

    private fun isList(component: Token): Boolean {
        val value = component.value.trim()
        return setOf(",", "+", "und").any { value.contains(it) }
    }

    private fun trySolve(debugTracing: DebugTracing, groups: List<Tokens>): ListOfDatePairSolution? {
        val canHaveMoreData = groups.filter { it.isInteresting() }.size > 1
        var results = groups.map { DatePairStep(config, debugTracing, it, canHaveMoreData).solve() }

        // type stealing
        results =
            results.mapIndexed { i, result ->
                result ?: DatePairStep(config, debugTracing, Utils.tryTypeStealing(groups, i), canHaveMoreData).solve()
            }

        if (results.any { it == null }) {
            return null
        }

        results = dataShareBetweenResults(results.filterNotNull())

        if (results.any { !it.isSolved() }) {
            return null
        }

        return ListOfDatePairSolution(results)
    }

    private fun dataShareBetweenResults(results: List<DatePairSolution>): List<DatePairSolution> = results.map {
        var withShared = it
        for (pair in results) {
            withShared = withShared.plusDataFrom(pair)
        }
        withShared
    }
}
