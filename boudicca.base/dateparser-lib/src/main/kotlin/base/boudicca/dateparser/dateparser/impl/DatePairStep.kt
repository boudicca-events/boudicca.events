package base.boudicca.dateparser.dateparser.impl

import base.boudicca.dateparser.dateparser.DateParserConfig


internal class DatePairStep(
    private val config: DateParserConfig,
    private val debugTracing: DebugTracing,
    private val tokens: Tokens,
    private val canGetMoreData: Boolean
) {
    fun solve(): DatePairSolution? {
        val untilIndexes = findAllUntilIndexes(tokens)
        for (untilIndex in untilIndexes) {
            if (untilIndex == 0 || untilIndex == tokens.tokens.size - 1) {
                continue
            }
            val tokensList = listOf(
                Tokens(tokens.tokens.take(untilIndex)), Tokens(tokens.tokens.drop(untilIndex + 1))
            ).filter { it.isInteresting() }
            if (tokensList.size != 1) {
                val childDebugTracing = debugTracing.startOperationWithChild("trying until with children", tokensList)
                val result = trySolve(childDebugTracing, tokensList)
                debugTracing.endOperation(result)
                if (result != null) {
                    return result
                }
            }
        }
        val result = trySolve(
            debugTracing.startOperationWithChild("trying no until handling", tokens), listOf(
                tokens
            )
        )
        debugTracing.endOperation(result)
        return result
    }

    private fun findAllUntilIndexes(tokens: Tokens): List<Int> {
        return tokens.tokens.mapIndexed { i, component -> Pair(i, component) }
            .filter { (_, component) -> isUntil(component) }
            .map { it.first }
    }

    private fun isUntil(component: Token): Boolean {
        val value = component.value.trim()
        return setOf("-", "bis", "–", "―", "—").any { value.contains(it) }
    }

    private fun trySolve(debugTracing: DebugTracing, groups: List<Tokens>): DatePairSolution? {
        if (groups.size == 1) {
            val startDate = DateStep(config, debugTracing, groups.single(), canGetMoreData).solve()
            return if (startDate != null) {
                DatePairSolution(startDate, null)
            } else {
                null
            }
        }
        if (groups.size != 2) {
            //dunno how to handle this
            return null
        }
        val leftGroup = groups[0]
        val rightGroup = groups[1]

        val rightDateSolution = DateStep(config, debugTracing, rightGroup, true).solve()
        val leftDateSolution = DateStep(config, debugTracing, leftGroup, true).solve() ?: DateStep(
            config, debugTracing, Utils.tryTypeStealing(groups, 0), true
        ).solve()

        //the "hasNothingInCommonWith" check is true if we should have parsed this without until handling
        //so we have a date on one and a time on the other side like "10.12.2025 - 17:00"
        if (leftDateSolution == null || rightDateSolution == null
            || leftDateSolution.hasNothingInCommonWith(rightDateSolution)
        ) {
            return null
        }

        val left = leftDateSolution.plusDateAndTimeFrom(rightDateSolution)
        val right = rightDateSolution.plusDateFrom(leftDateSolution)

        return if (canGetMoreData || (left.isSolved() && right.isSolved())) {
            DatePairSolution(left, right)
        } else {
            null
        }

    }
}
