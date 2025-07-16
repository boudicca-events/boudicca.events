package base.boudicca.dateparser.dateparser.impl


internal class DatePairStep(private val debugTracing: DebugTracing, private val tokens: Tokens) {
    fun solve(): DatePairSolution? {
        var result = trySolve(
            debugTracing.startOperationWithChild("trying no range handling", tokens), listOf(
                tokens
            )
        )
        debugTracing.endOperation(result)
        if (result != null) {
            return result
        }
        val untilIndexes = findAllUntilIndexes(tokens)
        for (untilIndex in untilIndexes) {
            if (untilIndex == 0 || untilIndex == tokens.tokens.size - 1) {
                continue
            }
            val tokensList = listOf(
                Tokens(tokens.tokens.take(untilIndex)), Tokens(tokens.tokens.drop(untilIndex + 1))
            ).filter { it.isInteresting() }
            if (tokensList.size != 1) {
                val childDebugTracing = debugTracing.startOperationWithChild("trying range with children", tokensList)
                result = trySolve(childDebugTracing, tokensList)
                debugTracing.endOperation(result)
                if (result != null) {
                    return result
                }
            }
        }
        return null
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
            val startDate = DateStep(debugTracing, groups.single()).solve()
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

        val rightDateSolution = DateStep(debugTracing, rightGroup).solve()
        val leftDateSolution = DateStep(debugTracing, leftGroup).solve() ?: DateStep(
            debugTracing, Utils.tryTypeStealing(groups, 0)
        ).solve()

        if (leftDateSolution == null || rightDateSolution == null) {
            return null
        }

        val left = leftDateSolution.plusDateAndTimeFrom(rightDateSolution)
        val right = rightDateSolution.plusDateFrom(leftDateSolution)

        return DatePairSolution(left, right)
    }
}
