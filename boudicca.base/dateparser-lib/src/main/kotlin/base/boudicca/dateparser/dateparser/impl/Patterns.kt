package base.boudicca.dateparser.dateparser.impl

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

    //TODO make order configurable
    val DAY_DMY = Pattern(
        listOf(
            type(ResultTypes.DAY),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.MONTH),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.YEAR),
        )
    )
    val DAY_DM = Pattern(
        listOf(
            type(ResultTypes.DAY),
            noise(1, Int.MAX_VALUE),
            type(ResultTypes.MONTH),
        )
    )
    val DAY_D = Pattern(
        listOf(
            type(ResultTypes.DAY),
        )
    )

    fun type(
        resultTypes: ResultTypes,
        minMatches: Int = 1,
        maxMatches: Int = 1,
    ): Matcher {
        return TypeMatcher(minMatches, maxMatches, resultTypes)
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

    internal data class TypeMatcher(
        override val minMatches: Int, override val maxMatches: Int, val resultTypes: ResultTypes
    ) : Matcher()

    internal data class SeparatorMatcher(
        override val minMatches: Int, override val maxMatches: Int, val separator: String
    ) : Matcher()

    fun applyGoodGuesses(tokens: List<Token>): List<Token> {
        return applyGuessesAndIntersect(tokens, PATTERNS_GOOD_GUESSES)
    }

    fun applyWeakGuesses(tokens: List<Token>): List<Token> {
        return applyGuessesAndIntersect(tokens, PATTERNS_WEAK_GUESSES)
    }

    fun applyCorrectPatternsAndMergeResult(inputTokens: List<Token>): List<Token> {
        val countOfInterestingTokens = inputTokens.count { it.needSolving }
        val patternsToApply = getPatternsForCount(countOfInterestingTokens)
        return applyPatternsAndMergeResult(patternsToApply, inputTokens)
    }

    @Suppress("MagicNumber")
    private fun getPatternsForCount(countOfInterestingTokens: Int): List<Pattern> {
        return when (countOfInterestingTokens) {
            6 -> listOf(TIME_HMS, DAY_DMY)
            5 -> listOf(TIME_HM, DAY_DMY)
            4 -> listOf(TIME_H, DAY_DMY)
            3 -> listOf(TIME_HMS, DAY_DMY)
            2 -> listOf(TIME_HM, DAY_DM)
            1 -> listOf(TIME_H, DAY_D)
            else -> emptyList()
        }
    }

    //TODO probably rethink this merging again.. we want to leave ones without matches alone.. right? maybe also don't make empty if nothing matches?
    private fun applyPatternsAndMergeResult(patterns: List<Pattern>, inputTokens: List<Token>): List<Token> {
        val allReplacements = patterns.map { findAllReplacements(inputTokens, it, true) }.flatten()
        if (allReplacements.isEmpty()) {
            return inputTokens.map { it.withTypes(emptySet()) }
        }
        return mergeGuesserTypes(inputTokens, allReplacements)
    }

    private fun mergeGuesserTypes(inputGuesses: List<Token>, componentLists: List<List<Token>>): List<Token> {
        val result = mutableListOf<Token>()

        for (i in inputGuesses.indices) {
            var newTypes = setOf<ResultTypes>()
            for (list in componentLists) {
                val component = list[i]
                newTypes = newTypes.union(component.possibleTypes)
            }
            result.add(inputGuesses[i].withTypes(newTypes))
        }

        return result
    }

    private fun applyGuessesAndIntersect(
        inputGuesses: List<Token>, guesses: List<Pattern>
    ): List<Token> {
        var result = inputGuesses.toList()
        for (pattern in guesses) {
            val replacements = findAllReplacements(result, pattern, false)
            if (replacements.isNotEmpty()) {
                result = intersectGuesserTypes(inputGuesses, replacements)
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

    private fun findAllReplacements(inputGuesses: List<Token>, pattern: Pattern, whiteout: Boolean): List<List<Token>> {
        val result = mutableListOf<List<Token>>()
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
    ): List<Token>? {
        val matchedGroups = mutableListOf<List<Token>>()
        var matchedComponentsCount = 0
        for (matcher in pattern.matchers) {
            val matchedGroup = tryMatchMatcherAtPosition(inputGuesses, matcher, i + matchedComponentsCount)
            if (matchedGroup != null) {
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

        return if (whiteout) {
            whiteout(prefix) + replacement + whiteout(suffix)
        } else {
            prefix + replacement + suffix

        }
    }

    private fun whiteout(tokens: List<Token>): List<Token> {
        return tokens.map { it.withTypes(emptySet()) }
    }

    private fun tryMatchMatcherAtPosition(
        inputGuesses: List<Token>, matcher: Matcher, i: Int
    ): List<Token>? {
        val result = mutableListOf<Token>()
        var j = i
        @Suppress("LoopWithTooManyJumpStatements") while (j < inputGuesses.size) {
            if (matches(matcher, inputGuesses[j])) {
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
            return result
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
