package base.boudicca.dateparser.dateparser.impl

import base.boudicca.dateparser.dateparser.DateParserConfig


@Suppress("MagicNumber")
internal class DateStep(
    private val config: DateParserConfig,
    private val debugTracing: DebugTracing,
    private val tokens: Tokens,
    private val canGetMoreData: Boolean,
) {
    fun solve(): DateSolution? {
        if (tokens.tokens.count { it.needSolving } > 6) {
            debugTracing.startOperation("too many tokens to solve", tokens)
            return null
        }
        var result = trySolve(
            listOf(tokens),
            debugTracing.startOperationWithChild("trying to solve without groupings", tokens),
        )
        debugTracing.endOperation(result)
        if (result != null) {
            return result
        }
        val joinedSeparators = joinAllSeparators(tokens)
        val allSeparatorValues = collectSeparatorValues(joinedSeparators).sorted()
        for (separatorThreshold in allSeparatorValues) {
            var lastSeparator = -2
            val separatingSeparators = mutableSetOf<Int>(0, joinedSeparators.size)
            for (i in joinedSeparators.indices) {
                val currentToken = joinedSeparators[i]
                if (!currentToken.needSolving) {
                    if (calculateSeparatorWeight(currentToken.value) > separatorThreshold) {
                        if (lastSeparator == -1) {
                            separatingSeparators.add(i)
                        }
                        lastSeparator = i
                    } else {
                        if (lastSeparator > 0) {
                            separatingSeparators.add(lastSeparator)
                        }
                        lastSeparator = -1
                    }
                }
            }

            val groups = mutableListOf<Tokens>()
            separatingSeparators.remove(-1)
            val subListSeparators = separatingSeparators.toList().sorted()
            for ((start, end) in subListSeparators.windowed(2)) {
                groups.add(Tokens(joinedSeparators.subList(start, end)))
            }

            if (groups.filter { it.isInteresting() }.size == 2) {
                result = trySolve(
                    groups,
                    debugTracing.startOperationWithChild("trying grouping with children", groups),
                )
                debugTracing.endOperation(result)
                if (result != null) {
                    return result
                }
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

    private fun trySolve(
        tokenGroups: List<Tokens>,
        debugTracing: DebugTracing,
    ): DateSolution? {
        return trySolve(tokenGroups, debugTracing, null) ?: trySolve(tokenGroups, debugTracing, config.dayMonthOrder)
    }

    private fun trySolve(
        tokenGroups: List<Tokens>, debugTracing: DebugTracing, dayMonthOrder: DateParserConfig.DayMonthOrder?
    ): DateSolution? {
        var solvedTokens = tokenGroups
        var lastTokens: List<Tokens>
        do {
            lastTokens = solvedTokens
            solvedTokens = applyPatternsAndElimination(lastTokens, dayMonthOrder)
        } while (lastTokens != solvedTokens)

        if (dayMonthOrder == null) {
            debugTracing.startOperation("applying patterns", solvedTokens)
        }
        if (dayMonthOrder == null) {
            debugTracing.startOperation("applying patterns + stricter patterns", solvedTokens)
        }

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
        if (result.isSolved() || canGetMoreData) {
            debugTracing.endOperation(result)
            return result
        } else {
            debugTracing.endOperation(null)
            return null
        }
    }

    private fun applyPatternsAndElimination(
        tokenGroups: List<Tokens>, dayMonthOrder: DateParserConfig.DayMonthOrder?
    ): List<Tokens> {
        var result = applyElimination(tokenGroups)
        result = result.map {
            var tokens = Patterns.applyExhaustivePatterns(it.tokens)
            if (dayMonthOrder != null) {
                tokens = Patterns.applyStricterPatternsAndMergeResult(it.tokens, dayMonthOrder)
            }
            Tokens(tokens)
        }
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

