package base.boudicca.dateparser.dateparser.impl

import base.boudicca.dateparser.dateparser.DateParserConfig

@Suppress("TooManyFunctions", "MagicNumber")
internal object Patterns {
    //patterns which we are pretty certain about, those are not exhaustive
    val PATTERNS_GOOD_GUESSES = listOf(
        Pattern(
            listOf(
                type(ResultTypes.HOURS),
                separator(":"),
                type(ResultTypes.MINUTES),
                separator(":"),
                type(ResultTypes.SECONDS)
            )
        ),
        Pattern(
            listOf(
                type(ResultTypes.HOURS), separator(":"), type(ResultTypes.MINUTES)
            )
        ),
        Pattern(
            listOf(
                type(ResultTypes.DAY),
                separator("."),
                type(ResultTypes.MONTH),
                separator("."),
                type(ResultTypes.YEAR),
            )
        ),
        Pattern(
            listOf(
                type(ResultTypes.YEAR),
                separator("-"),
                type(ResultTypes.MONTH),
                separator("-"),
                type(ResultTypes.DAY),
            )
        ),
        Pattern(
            listOf(
                type(ResultTypes.MONTH),
                separator("/"),
                type(ResultTypes.DAY),
                separator("/"),
                type(ResultTypes.YEAR),
            )
        ),
    )
    val PATTERNS_WEAK_GUESSES = listOf(
        Pattern(
            listOf(
                type(ResultTypes.DAY),
                separator("."),
                type(ResultTypes.MONTH),
            )
        ),
        Pattern(
            listOf(
                type(ResultTypes.DAY),
                separator("."),
            )
        ),
        Pattern(
            listOf(
                type(ResultTypes.HOURS),
                noise(0, 1),
                type(ResultTypes.MINUTES),
                noise(0, 1),
                separator("Uhr"),
            )
        ),
        Pattern(
            listOf(
                type(ResultTypes.HOURS),
                noise(0, 1),
                separator("Uhr"),
            )
        ),
        Pattern(
            listOf(
                separator("um"),
                noise(0, 1),
                type(ResultTypes.HOURS),
            )
        ),

        Pattern(
            listOf(
                separator("am"),
                noise(0, 5),
                type(ResultTypes.DAY),
            )
        ),
    )

    val TIME_HMS = Pattern(
        listOf(
            type(ResultTypes.HOURS),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.MINUTES),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.SECONDS),
        )
    )
    val TIME_HM = Pattern(
        listOf(
            type(ResultTypes.HOURS),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.MINUTES),
        )
    )
    val TIME_H = Pattern(
        listOf(
            type(ResultTypes.HOURS),
        )
    )

    val DATE_D = Pattern(
        listOf(
            type(ResultTypes.DAY),
        )
    )
    val DATE_DY = Pattern(
        listOf(
            type(ResultTypes.DAY),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.YEAR),
        )
    )
    val DATE_YD = Pattern(
        listOf(
            type(ResultTypes.YEAR),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.DAY),
        )
    )
    val DATE_YDM = Pattern(
        listOf(
            type(ResultTypes.YEAR),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.DAY),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.MONTH),
        )
    )
    val DATE_YMD = Pattern(
        listOf(
            type(ResultTypes.YEAR),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.MONTH),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.DAY),
        )
    )
    val DATE_DM = Pattern(
        listOf(
            type(ResultTypes.DAY),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.MONTH),
        )
    )
    val DATE_DMY = Pattern(
        listOf(
            type(ResultTypes.DAY),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.MONTH),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.YEAR),
        )
    )
    val DATE_MD = Pattern(
        listOf(
            type(ResultTypes.MONTH),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.DAY),
        )
    )
    val DATE_MDY = Pattern(
        listOf(
            type(ResultTypes.MONTH),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.DAY),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.YEAR),
        )
    )

    val EXHAUSTIVE_DATE_PATTERNS = listOf(
        DATE_D,
        DATE_DY,
        DATE_YD,
        DATE_DM,
        DATE_DMY,
        DATE_MD,
        DATE_MDY,
        DATE_YDM,
        DATE_YMD,
    )
    val EXHAUSTIVE_TIME_PATTERNS = listOf(TIME_HMS, TIME_HM, TIME_H)

    fun type(
        resultTypes: ResultTypes,
    ): Matcher {
        return TypeMatcher(resultTypes)
    }

    fun noise(minMatches: Int, maxMatches: Int): Matcher {
        return NoiseMatcher(minMatches, maxMatches)
    }

    fun separator(
        separator: String,
        minMatches: Int = 1,
        maxMatches: Int = 1,
    ): Matcher {
        return SeparatorMatcher(minMatches, maxMatches, separator)
    }

    internal class Pattern(val matchers: List<Matcher>) {
        override fun toString(): String {
            return "Pattern(matchers=$matchers)"
        }
    }

    internal sealed class Matcher {
        abstract val minMatches: Int
        abstract val maxMatches: Int
    }

    internal data class NoiseMatcher(
        override val minMatches: Int,
        override val maxMatches: Int,
    ) : Matcher()

    @ConsistentCopyVisibility
    internal data class TypeMatcher private constructor(
        override val minMatches: Int, override val maxMatches: Int, val resultTypes: ResultTypes
    ) : Matcher() {
        constructor(resultTypes: ResultTypes) : this(1, 1, resultTypes)
    }

    internal data class SeparatorMatcher(
        override val minMatches: Int, override val maxMatches: Int, val separator: String
    ) : Matcher()

    fun applyGoodGuesses(tokens: List<Token>): List<Token> {
        return applyPatternsAndMergeResult(PATTERNS_GOOD_GUESSES, tokens)
    }

    fun applyWeakGuesses(tokens: List<Token>): List<Token> {
        return applyGuessesAndIntersect(tokens, PATTERNS_WEAK_GUESSES)
    }

    fun applyExhaustivePatterns(
        inputTokens: List<Token>,
    ): List<Token> {
        val patterns = EXHAUSTIVE_TIME_PATTERNS + EXHAUSTIVE_DATE_PATTERNS

        val allMatchesPerPatterns = patterns.map { findAllReplacements(inputTokens, it, true) }

        val allValidCombinations = findAllValidReplacements(inputTokens, allMatchesPerPatterns)

        return mergeResultTypes(inputTokens, allValidCombinations, true)
    }

    fun applyStricterPatternsAndMergeResult(
        inputTokens: List<Token>,
        dayMonthOrder: DateParserConfig.DayMonthOrder
    ): List<Token> {
        val countOfInterestingTokens = inputTokens.count { it.needSolving }
        val patternsToApply = getStricterPatternsForCount(countOfInterestingTokens, dayMonthOrder)
        return applyPatternsAndMergeResult(patternsToApply, inputTokens)
    }

    @Suppress("MagicNumber")
    private fun getStricterPatternsForCount(
        countOfInterestingTokens: Int,
        dayMonthOrder: DateParserConfig.DayMonthOrder
    ): List<Pattern> {
        val isDM = dayMonthOrder == DateParserConfig.DayMonthOrder.DAY_MONTH
        return when (countOfInterestingTokens) {
            6 -> listOf(TIME_HMS, if (isDM) DATE_DMY else DATE_MDY)
            5 -> listOf(TIME_HM, if (isDM) DATE_DMY else DATE_MDY)
            4 -> listOf(TIME_H, if (isDM) DATE_DMY else DATE_MDY)
            3 -> listOf(TIME_HMS, if (isDM) DATE_DMY else DATE_MDY)
            2 -> listOf(TIME_HM, if (isDM) DATE_DM else DATE_MD)
            1 -> listOf(TIME_H, DATE_D)
            else -> emptyList()
        }
    }

    private fun findAllValidReplacements(
        inputTokens: List<Token>,
        matchesPerPattern: List<List<Pair<Set<Int>, List<Token>>>>
    ): List<List<Token>> {
        val validCombinations = mutableListOf<List<Pair<Set<Int>, List<Token>>>>()
        val neededTokenIndices =
            inputTokens.count { it.needSolving }

        findAllValidReplacementsRecursively(
            neededTokenIndices,
            validCombinations,
            mutableListOf(),
            matchesPerPattern.flatten().toMutableList(),
            mutableSetOf<Int>(),
            mutableSetOf<ResultTypes>()
        )

        return validCombinations.map { mergeResultTypes(inputTokens, it.map { it.second }, true) }
    }

    @Suppress("LongParameterList")
    private fun findAllValidReplacementsRecursively(
        neededTokenIndicesCount: Int,
        result: MutableList<List<Pair<Set<Int>, List<Token>>>>,
        currentResult: MutableList<Pair<Set<Int>, List<Token>>>,
        remainingMatches: MutableList<Pair<Set<Int>, List<Token>>>,
        currentTokenIndices: MutableSet<Int>,
        currentResultTypes: MutableSet<ResultTypes>
    ) {
        if (remainingMatches.isEmpty()) {
            return
        }
        val currentMatch = remainingMatches.removeLast()
        //first call without us
        findAllValidReplacementsRecursively(
            neededTokenIndicesCount,
            result,
            currentResult,
            remainingMatches,
            currentTokenIndices,
            currentResultTypes
        )

        //do we match the same tokens?
        if (currentMatch.first.intersect(currentTokenIndices).isEmpty()) {
            val matchingResultTypes = currentMatch.first.map { currentMatch.second[it].possibleTypes }.flatten().toSet()
            //do we match the same resulttypes?
            if (matchingResultTypes.intersect(currentResultTypes).isEmpty()) {
                //now can add ourselves as a matched pattern
                currentTokenIndices.addAll(currentMatch.first)
                currentResultTypes.addAll(matchingResultTypes)
                currentResult.add(currentMatch)

                //do we have a new match?
                if (currentTokenIndices.size == neededTokenIndicesCount) {
                    result.add(currentResult.toList())
                    //can we still grow?
                } else if (currentTokenIndices.size < neededTokenIndicesCount) {
                    findAllValidReplacementsRecursively(
                        neededTokenIndicesCount,
                        result,
                        currentResult,
                        remainingMatches,
                        currentTokenIndices,
                        currentResultTypes
                    )
                }

                currentTokenIndices.removeAll(currentMatch.first)
                currentResultTypes.removeAll(matchingResultTypes)
                currentResult.removeLast()
            }
        }

        remainingMatches.add(currentMatch)
    }

    private fun applyPatternsAndMergeResult(
        patterns: List<Pattern>,
        inputTokens: List<Token>
    ): List<Token> {
        val allReplacements = patterns.map { findAllReplacements(inputTokens, it, true) }.flatten()
        if (allReplacements.isEmpty()) {
            return inputTokens
        }
        return mergeResultTypes(inputTokens, allReplacements.map { it.second }, false)
    }

    private fun mergeResultTypes(
        inputTokens: List<Token>,
        componentLists: List<List<Token>>,
        exhaustive: Boolean
    ): List<Token> {
        val result = mutableListOf<Token>()

        for (i in inputTokens.indices) {
            var newTypes = setOf<ResultTypes>()
            for (list in componentLists) {
                val component = list[i]
                newTypes = newTypes.union(component.possibleTypes)
            }
            result.add(inputTokens[i].withTypes(newTypes))
        }

        return if (exhaustive) {
            result
        } else {
            inputTokens.mapIndexed { i, token ->
                val mergedToken = result[i]
                if (mergedToken.possibleTypes.isEmpty()) {
                    token
                } else {
                    token.withTypes(mergedToken.possibleTypes)
                }
            }
        }
    }

    private fun applyGuessesAndIntersect(
        inputGuesses: List<Token>, guesses: List<Pattern>
    ): List<Token> {
        var result = inputGuesses.toList()
        for (pattern in guesses) {
            val replacements = findAllReplacements(result, pattern, false)
            if (replacements.isNotEmpty()) {
                result = intersectGuesserTypes(inputGuesses, replacements.map { it.second })
            }
        }
        return result
    }

    private fun intersectGuesserTypes(inputGuesses: List<Token>, componentLists: List<List<Token>>): List<Token> {
        val result = mutableListOf<Token>()

        for (i in inputGuesses.indices) {
            var newTypes = ResultTypes.entries.toSet()
            for (list in componentLists) {
                val component = list[i]
                newTypes = newTypes.intersect(component.possibleTypes)
            }
            result.add(inputGuesses[i].withTypes(newTypes))
        }

        return result
    }

    private fun findAllReplacements(
        inputGuesses: List<Token>,
        pattern: Pattern,
        whiteout: Boolean
    ): List<Pair<Set<Int>, List<Token>>> {
        val result = mutableListOf<Pair<Set<Int>, List<Token>>>()
        for (i in inputGuesses.indices) {
            val replacement = tryMatchAndReplacePatternAtPosition(inputGuesses, pattern, whiteout, i)
            if (replacement != null) {
                result.add(replacement)
            }
        }
        return result
    }

    private fun tryMatchAndReplacePatternAtPosition(
        inputGuesses: List<Token>, pattern: Pattern, whiteout: Boolean, i: Int
    ): Pair<Set<Int>, List<Token>>? {
        val matchedGroups = mutableListOf<List<Token>>()
        val matchedTypeTokens = mutableSetOf<Int>()
        var matchedComponentsCount = 0
        for (matcher in pattern.matchers) {
            val matchedResult = tryMatchMatcherAtPosition(inputGuesses, matcher, i + matchedComponentsCount)
            if (matchedResult != null) {
                matchedTypeTokens.addAll(matchedResult.first)
                val matchedGroup = matchedResult.second
                matchedGroups.add(matchedGroup)
                matchedComponentsCount += matchedGroup.size
            } else {
                return null
            }
        }
        val prefix = inputGuesses.subList(0, i)
        val suffix = inputGuesses.subList(i + matchedComponentsCount, inputGuesses.size)
        val replacement = replacement(
            pattern, matchedGroups
        )

        return Pair(
            matchedTypeTokens, if (whiteout) {
                whiteout(prefix) + replacement + whiteout(suffix)
            } else {
                prefix + replacement + suffix
            }
        )
    }

    private fun whiteout(tokens: List<Token>): List<Token> {
        return tokens.map { it.withTypes(emptySet()) }
    }

    private fun tryMatchMatcherAtPosition(
        inputGuesses: List<Token>, matcher: Matcher, i: Int
    ): Pair<Set<Int>, List<Token>>? {
        val result = mutableListOf<Token>()
        val matchedTypeIndices = mutableSetOf<Int>()
        var j = i
        @Suppress("LoopWithTooManyJumpStatements") while (j < inputGuesses.size) {
            if (matches(matcher, inputGuesses[j])) {
                if (matcher is TypeMatcher) {
                    matchedTypeIndices.add(j)
                }
                result.add(inputGuesses[j])
                if (result.size == matcher.maxMatches) {
                    break
                }
                j++
            } else {
                break
            }
        }
        if (result.size >= matcher.minMatches && result.size <= matcher.maxMatches) {
            return Pair(matchedTypeIndices, result)
        }
        return null
    }

    private fun matches(matcher: Matcher, guess: Token): Boolean {
        return when (matcher) {
            is TypeMatcher -> guess.possibleTypes.contains(matcher.resultTypes)
            is NoiseMatcher -> guess.possibleTypes.isEmpty()
            is SeparatorMatcher -> guess.possibleTypes.isEmpty() && guess.value.trim() == matcher.separator
        }
    }

    private fun replacement(pattern: Pattern, capturedComponents: List<List<Token>>): List<Token> {
        check(pattern.matchers.size == capturedComponents.size)
        return pattern.matchers.zip(capturedComponents).map { replacement(it.first, it.second) }.flatten()
    }

    private fun replacement(matcher: Matcher, components: List<Token>): List<Token> {
        return when (matcher) {
            is TypeMatcher -> components.map {
                it.withTypes(setOf(matcher.resultTypes))
            }

            else -> components
        }
    }

}
