package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType

internal object Patterns {
    val PATTERNS_FIXED = listOf(
        Pattern(
            listOf(
                canBe(GuesserType.HOURS),
                matches("matches a ':' for \"hours:minutes:seconds\"") { it.possibleTypes.isEmpty() && it.value.trim() == ":" },
                canBe(GuesserType.MINUTES),
                matches("matches a ':' for \"hours:minutes:seconds\"") { it.possibleTypes.isEmpty() && it.value.trim() == ":" },
                canBe(GuesserType.SECONDS)
            )
        ) { matches ->
            listOf(
                Time(
                    matches[0].first().value.toInt(),
                    matches[2].first().value.toInt(),
                    matches[4].first().value.toInt(),
                )
            )
        },
        Pattern(
            listOf(
                canBe(GuesserType.HOURS),
                matches("matches a ':' for \"hours:minutes\"") { it.possibleTypes.isEmpty() && it.value.trim() == ":" },
                canBe(GuesserType.MINUTES)
            )
        ) { matches ->
            listOf(
                Time(
                    matches[0].first().value.toInt(), matches[2].first().value.toInt(), null
                )
            )
        },
        Pattern(
            listOf(
                canBe(GuesserType.DAY),
                matches("matches a '.' for \"day.month.year\"") { it.possibleTypes.isEmpty() && it.value.trim() == "." },
                canBe(GuesserType.MONTH),
                matches("matches a '.' for \"day.month.year\"") { it.possibleTypes.isEmpty() && it.value.trim() == "." },
                canBe(GuesserType.YEAR),
            )
        ) { matches ->
            listOf(
                createDate(
                    matches[0].first(),
                    matches[2].first(),
                    matches[4].first(),
                )
            )
        },
        Pattern(
            listOf(
                canBe(GuesserType.YEAR),
                matches("matches a '-' for \"year-month-day\"") { it.possibleTypes.isEmpty() && it.value.trim() == "-" },
                canBe(GuesserType.MONTH),
                matches("matches a '-' for \"year-month-day\"") { it.possibleTypes.isEmpty() && it.value.trim() == "-" },
                canBe(GuesserType.DAY),
            )
        ) { matches ->
            listOf(
                createDate(
                    matches[4].first(),
                    matches[2].first(),
                    matches[0].first(),
                )
            )
        },
        Pattern(
            listOf(
                canBe(GuesserType.MONTH),
                matches("matches a '/' for \"month/day/year\"") { it.possibleTypes.isEmpty() && it.value.trim() == "/" },
                canBe(GuesserType.DAY),
                matches("matches a '/' for \"month/day/year\"") { it.possibleTypes.isEmpty() && it.value.trim() == "/" },
                canBe(GuesserType.YEAR),
            )
        ) { matches ->
            listOf(
                createDate(
                    matches[2].first(),
                    matches[0].first(),
                    matches[4].first(),
                )
            )
        },
    )

    val PATTERNS_MAYBES = listOf(
        Pattern(
            listOf(
                canBe(GuesserType.HOURS),
                canBeNothing(true),
                canBe(GuesserType.MINUTES),
                canBeNothing(true),
                canBe(GuesserType.SECONDS),
            )
        ) { matches ->
            listOf(
                Time(
                    matches[0].first().value.toInt(),
                    matches[2].first().value.toInt(),
                    matches[4].first().value.toInt(),
                )
            )
        },
        Pattern(
            listOf(
                canBe(GuesserType.HOURS),
                canBeNothing(true),
                canBe(GuesserType.MINUTES),
            )
        ) { matches ->
            listOf(
                Time(
                    matches[0].first().value.toInt(), matches[2].first().value.toInt(), 0
                )
            )
        },
        Pattern(
            listOf(
                canBe(GuesserType.DAY),
                canBeNothing(true),
                canBe(GuesserType.MONTH),
                canBeNothing(true),
                canBe(GuesserType.YEAR),
            )
        ) { matches ->
            listOf(
                createDate(
                    matches[0].first(),
                    matches[2].first(),
                    matches[4].first(),
                )
            )
        },
        Pattern(
            listOf(
                canBe(GuesserType.YEAR),
                canBeNothing(true),
                canBe(GuesserType.MONTH),
                canBeNothing(true),
                canBe(GuesserType.DAY),
            )
        ) { matches ->
            listOf(
                createDate(
                    matches[4].first(),
                    matches[2].first(),
                    matches[0].first(),
                )
            )
        },
    )

    fun canBe(guesserType: GuesserType, canMatchMultipleTimes: Boolean = false): Matcher {
        return CanBeMatcher(canMatchMultipleTimes, guesserType)
    }

    fun canBeNothing(canMatchMultipleTimes: Boolean = false): Matcher {
        return CanBeNothingMatcher(canMatchMultipleTimes)
    }

    fun matches(
        description: String, canMatchMultipleTimes: Boolean = false, condition: (Any) -> Boolean
    ): Matcher {
        return ConditionMatcher(canMatchMultipleTimes, description, condition)
    }

    fun createDate(
        day: Any, month: Any?, year: Any?
    ): Date {
        //TODO error catching?
        return Date(
            day.value.toInt(),
            if (month != null)
                month.value.toIntOrNull()
                    ?: MonthMappings.mapMonthToInt(month.value)
                    ?: throw IllegalArgumentException("blaa")
            else
                null,
            if (year != null) fixYear(year.value.toInt()) else null
        )
    }

    private fun fixYear(year: Int): Int {
        return if (year < 70) { //we get some problems in the year 2070 with this...
            2000 + year
        } else if (year < 100) {
            1900 + year
        } else {
            year
        }
    }

    internal class Pattern(val matchers: List<Matcher>, val replacement: (List<List<Any>>) -> List<Component>) {
        override fun toString(): String {
            return "Pattern(matchers=$matchers)"
        }
    }

    internal sealed class Matcher {
        abstract val canMatchMultipleTimes: Boolean
        abstract fun matches(any: Any): Boolean
    }

    internal data class CanBeNothingMatcher(
        override val canMatchMultipleTimes: Boolean,
    ) : Matcher() {
        override fun matches(any: Any): Boolean {
            return any.possibleTypes.isEmpty()
        }
    }

    internal data class CanBeMatcher(
        override val canMatchMultipleTimes: Boolean, val guesserType: GuesserType
    ) : Matcher() {
        override fun matches(any: Any): Boolean {
            return any.possibleTypes.contains(guesserType)
        }
    }

    internal class ConditionMatcher(
        override val canMatchMultipleTimes: Boolean, val description: String, //only useful for debugging purposes
        val condition: (Any) -> Boolean
    ) : Matcher() {
        override fun matches(any: Any): Boolean {
            return condition(any)
        }

        override fun toString(): String {
            return "ConditionMatcher(canMatchMultipleTimes=$canMatchMultipleTimes, description='$description')"
        }
    }

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
                        val matches = currentMatcher.matches(guess)
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
                        val replacements = pattern.replacement(capturedMatches)
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
        val matchedRanges = mutableListOf<Pair<Int, Int>>()
        for (pattern in patterns) {
            var i = 0
            while (i < guesses.size) {
                if (i + pattern.matchers.size <= guesses.size) {
                    var patternMatches = false
                    var capturedCount = 0
                    var stepCount = 0
                    var currentStepMatched = false
                    while (i + capturedCount < guesses.size) {
                        val guess = guesses[i + capturedCount]
                        if (guess !is Any) {
                            break
                        }
                        val currentMatcher = pattern.matchers[stepCount]
                        val matches = currentMatcher.matches(guess)
                        if (!(matches || (currentMatcher.canMatchMultipleTimes && currentStepMatched))) {
                            break
                        }
                        if (matches) {
                            capturedCount++
                            currentStepMatched = true
                        }
                        if (!currentMatcher.canMatchMultipleTimes || !matches) {
                            stepCount++
                        }
                        if (stepCount >= pattern.matchers.size) {
                            patternMatches = true
                            break
                        }
                    }
                    if (patternMatches) {
                        matchedRanges.add(Pair(i, i + capturedCount - 1))
                        i += capturedCount
                        continue
                    }
                }
                i++
            }
        }
        for (i1 in matchedRanges.indices) {
            for (i2 in matchedRanges.indices) {
                if (i1 == i2) {
                    continue
                }
                val r1 = matchedRanges[i1]
                val r2 = matchedRanges[i2]
                if (r1.first <= r2.first && r1.second >= r2.second) {
                    return false
                }
                if (r1.first >= r2.first && r1.first <= r2.second) {
                    return false
                }
            }
        }
        return true
    }

}
