package base.boudicca.dateparser.dateparser.impl


@Suppress("MagicNumber")
internal class DateStep(private val debugTracing: DebugTracing, private val tokens: Tokens) {
    fun solve(): DateSolution? {
        if (tokens.tokens.count { it.needSolving } > 6) {
            debugTracing.startOperation("too many tokens to solve", tokens)
            return null
        }
        var result =
            trySolve(listOf(tokens), debugTracing.startOperationWithChild("trying to solve without groupings", tokens))
        debugTracing.endOperation(result)
        if (result != null) {
            return result
        }
        val joinedSeparators = joinAllSeparators(tokens)
        val allSeparatorValues = collectSeparatorValues(joinedSeparators)
        for (separatorThreshold in allSeparatorValues) {
            val groups = mutableListOf<Tokens>()
            var currentGroup = mutableListOf<Token>()
            for (component in joinedSeparators) {
                if (!component.needSolving) {
                    if (calculateSeparatorWeight(component.value) >= separatorThreshold) {
                        if (currentGroup.isNotEmpty()) {
                            groups.add(Tokens(currentGroup))
                            currentGroup = mutableListOf()
                        }
                    } else {
                        currentGroup.add(component)
                    }
                } else {
                    currentGroup.add(component)
                }
            }
            if (currentGroup.isNotEmpty()) {
                groups.add(Tokens(currentGroup))
            }

            result = trySolve(groups, debugTracing.startOperationWithChild("trying grouping with children", groups))
            debugTracing.endOperation(result)
            if (result != null) {
                return result
            }
        }
        return null
    }

    private fun joinAllSeparators(tokens: Tokens): List<Token> {
        val result = mutableListOf<Token>()
        var currentString = ""

        for (token in tokens.tokens) {
            if (!token.needSolving) {
                currentString += token.value
            } else {
                if (currentString.isNotEmpty()) {
                    result.add(Token.create(currentString, emptySet()))
                    currentString = ""
                }
                result.add(token)
            }
        }

        return result
    }

    private fun calculateSeparatorWeight(value: String): Int {
        return value.map {
            when (it) {
                '.', '-', '/', ':' -> 1
                ' ' -> 2
                '\n' -> 100
                else -> 3
            }
        }.sum()
    }

    private fun collectSeparatorValues(components: List<Token>): List<Int> {
        return components.asSequence()
            .filter { !it.needSolving }
            .map { calculateSeparatorWeight(it.value) }
            .distinct()
            .sortedDescending()
            .toList()
    }

    private fun trySolve(tokenGroups: List<Tokens>, debugTracing: DebugTracing): DateSolution? {
        val solvedTokens = if (!tokens.tokens.all { it.isSolved() }) {
            var lastTokens = tokenGroups
            var nextTokens = applyPatternsAndElimination(lastTokens)
            while (lastTokens != nextTokens) {
                lastTokens = nextTokens
                nextTokens = applyPatternsAndElimination(lastTokens)
            }
            lastTokens
        } else {
            tokenGroups
        }

        debugTracing.startOperation("applying patterns", solvedTokens)

        if (solvedTokens.any { it.tokens.any { token -> !token.isSolved() } }) {
            return null
        }
        val mappings = mutableMapOf<ResultTypes, Token>()
        for (token in solvedTokens.flatMap { it.tokens }) {
            if (token.possibleTypes.size == 1) {
                val type = token.possibleTypes.single()
                //double solved thingies are bad
                if (mappings.containsKey(type)) {
                    return null
                }
                mappings[type] = token
            }
        }
        val result = DateSolution.create(
            mappings[ResultTypes.DAY],
            mappings[ResultTypes.MONTH],
            mappings[ResultTypes.YEAR],
            mappings[ResultTypes.HOURS],
            mappings[ResultTypes.MINUTES],
            mappings[ResultTypes.SECONDS],
            tokenGroups
        )
        debugTracing.endOperation(result)
        return result
    }

    private fun applyPatternsAndElimination(tokenGroups: List<Tokens>): List<Tokens> {
        var result = applyElimination(tokenGroups)
        result = result.map { Tokens(Patterns.applyCorrectPatternsAndMergeResult(it.tokens)) }
        return result
    }

    private fun applyElimination(tokenGroups: List<Tokens>): List<Tokens> {
        var result = tokenGroups
        var foundNewSingleElement: Boolean
        val allSingleElements = mutableSetOf<ResultTypes>()
        do {
            foundNewSingleElement = false
            for (token in result.map { it.tokens }.flatten()) {
                if (token.possibleTypes.size == 1) {
                    val singleElement = token.possibleTypes.single()
                    if (!allSingleElements.contains(singleElement)) {
                        foundNewSingleElement = true
                        allSingleElements.add(singleElement)
                        result = removeSingleElement(result, singleElement)
                    }
                }
            }
        } while (foundNewSingleElement)
        return result
    }

    private fun removeSingleElement(tokenGroups: List<Tokens>, singleElement: ResultTypes): List<Tokens> {
        return tokenGroups.map {
            it.map { token ->
                if (token.possibleTypes.size == 1) {
                    //TODO this is a workaround to avoid deleting our own single element... not sure if there is a better way to do this?
                    token
                } else {
                    token.minusType(singleElement)
                }
            }
        }
    }
}

