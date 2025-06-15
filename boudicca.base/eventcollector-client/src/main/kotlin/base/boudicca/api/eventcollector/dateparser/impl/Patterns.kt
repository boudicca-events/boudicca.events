package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType

internal object Patterns {
    val PATTERNS_FIXED = listOf(
        Pattern(
            listOf(
                type(GuesserType.HOURS),
                separator(":"),
                type(GuesserType.MINUTES),
                separator(":"),
                type(GuesserType.SECONDS)
            )
        ),
        Pattern(
            listOf(
                type(GuesserType.HOURS), separator(":"), type(GuesserType.MINUTES)
            )
        ),
        Pattern(
            listOf(
                type(GuesserType.DAY),
                separator("."),
                type(GuesserType.MONTH),
                separator("."),
                type(GuesserType.YEAR),
            )
        ),
        Pattern(
            listOf(
                type(GuesserType.YEAR),
                separator("-"),
                type(GuesserType.MONTH),
                separator("-"),
                type(GuesserType.DAY),
            )
        ),
        Pattern(
            listOf(
                type(GuesserType.MONTH),
                separator("/"),
                type(GuesserType.DAY),
                separator("/"),
                type(GuesserType.YEAR),
            )
        ),
    )

    val PATTERNS_MAYBES = listOf(
        Pattern(
            listOf(
                type(GuesserType.HOURS),
                noise(true),
                type(GuesserType.MINUTES),
                noise(true),
                type(GuesserType.SECONDS),
            )
        ),
        Pattern(
            listOf(
                type(GuesserType.HOURS),
                noise(true),
                type(GuesserType.MINUTES),
            )
        ),
        Pattern(
            listOf(
                type(GuesserType.DAY),
                noise(true),
                type(GuesserType.MONTH),
                noise(true),
                type(GuesserType.YEAR),
            )
        ),
        Pattern(
            listOf(
                type(GuesserType.DAY),
                noise(true),
                type(GuesserType.MONTH),
            )
        ),
        Pattern(
            listOf(
                type(GuesserType.YEAR),
                noise(true),
                type(GuesserType.MONTH),
                noise(true),
                type(GuesserType.DAY),
            )
        ),
    )

    fun type(guesserType: GuesserType, canMatchMultipleTimes: Boolean = false): Matcher {
        return TypeMatcher(canMatchMultipleTimes, guesserType)
    }

    fun noise(canMatchMultipleTimes: Boolean = false): Matcher {
        return NoiseMatcher(canMatchMultipleTimes)
    }

    fun separator(
        separator: String, canMatchMultipleTimes: Boolean = false
    ): Matcher {
        return SeparatorMatcher(canMatchMultipleTimes, separator)
    }

    internal class Pattern(val matchers: List<Matcher>) {
        override fun toString(): String {
            return "Pattern(matchers=$matchers)"
        }
    }

    internal sealed class Matcher {
        abstract val canMatchMultipleTimes: Boolean
    }

    internal data class NoiseMatcher(
        override val canMatchMultipleTimes: Boolean,
    ) : Matcher()

    internal data class TypeMatcher(
        override val canMatchMultipleTimes: Boolean, val guesserType: GuesserType
    ) : Matcher()

    internal data class SeparatorMatcher(
        override val canMatchMultipleTimes: Boolean, val separator: String
    ) : Matcher()


    //TODO this code is fugly
    fun apply(inputGuesses: List<Component>, patterns: List<Pattern>): List<Component> {
        var guesses = inputGuesses
        var result = mutableListOf<Component>()
        for (pattern in patterns) {
            var i = 0
            while (i < guesses.size) {
                if (i + pattern.matchers.size <= guesses.size) {
                    var patternMatches = false
                    var capturedCount = 0
                    var stepCount = 0
                    val capturedMatches = mutableListOf<List<Any>>()
                    var currentCapturedMatches = mutableListOf<Any>()
                    while (i + capturedCount < guesses.size) {
                        val guess = guesses[i + capturedCount]
                        if (guess !is Any) {
                            break
                        }
                        val currentMatcher = pattern.matchers[stepCount]
                        val matches = matches(currentMatcher, guess)
                        if (!(matches || (currentMatcher.canMatchMultipleTimes && currentCapturedMatches.isNotEmpty()))) {
                            break
                        }
                        if (matches) {
                            capturedCount++
                            currentCapturedMatches.add(guess)
                        }
                        if (!currentMatcher.canMatchMultipleTimes || !matches) {
                            capturedMatches.add(currentCapturedMatches)
                            currentCapturedMatches = mutableListOf()
                            stepCount++
                        }
                        if (stepCount >= pattern.matchers.size) {
                            patternMatches = true
                            break
                        }
                    }
                    if (patternMatches) {
                        val replacements = replacement(pattern, capturedMatches)
                        result.addAll(replacements)
                        i += capturedCount
                        continue
                    }
                }
                result.add(guesses[i])
                i++
            }
            guesses = result
            result = mutableListOf()
        }
        return guesses
    }

    fun canApplyWithoutCollision(guesses: List<Component>, patterns: List<Pattern>): Boolean {
        if (patterns.isEmpty()) {
            return false
        }

        val results = mutableListOf<List<Component>>()
        for (pattern in patterns) {
            results.add(apply(guesses, listOf(pattern)))
        }

        for (guessIndex in results[0].indices) {
            for (resultIndex1 in results.indices) {
                for (resultIndex2 in results.indices) {
                    if (resultIndex1 == resultIndex2) {
                        continue
                    }
                    val resultGuess1 = results[resultIndex1][guessIndex]
                    val resultGuess2 = results[resultIndex2][guessIndex]
                    if (resultGuess1 is Any && resultGuess2 is Any && resultGuess1.possibleTypes.isNotEmpty() && resultGuess2.possibleTypes.isNotEmpty()) {
                        if (resultGuess1.possibleTypes.intersect(resultGuess2.possibleTypes).isEmpty()) {
                            return false
                        }
                    }
                }
            }
        }

        return true
    }

    private fun matches(matcher: Matcher, guess: Component): Boolean {
        return when (matcher) {
            is TypeMatcher -> guess is Any && guess.possibleTypes.contains(matcher.guesserType)
            is NoiseMatcher -> guess !is Any || guess.possibleTypes.isEmpty()
            is SeparatorMatcher -> guess is Any && guess.possibleTypes.isEmpty() && guess.value.trim() == matcher.separator
        }
    }

    private fun replacement(pattern: Pattern, capturedComponents: List<List<Component>>): List<Component> {
        check(pattern.matchers.size == capturedComponents.size)
        return pattern.matchers.zip(capturedComponents).map { replacement(it.first, it.second) }.flatten()
    }

    private fun replacement(matcher: Matcher, components: List<Component>): List<Component> {
        return when (matcher) {
            is TypeMatcher -> components.map {
                if (it is Any) {
                    Any(it.value, setOf(matcher.guesserType))
                } else {
                    it
                }
            }

            else -> components
        }
    }

}
