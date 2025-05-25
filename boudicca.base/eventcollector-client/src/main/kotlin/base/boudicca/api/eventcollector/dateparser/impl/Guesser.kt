package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.HintType
import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType

internal class Guesser(private val hints: List<HintType>, private val tokens: List<Pair<TokenizerType, String>>) {


    companion object {
        private fun canBe(guesserType: GuesserType): Matcher {
            return CanBeMatcher(false, guesserType)
        }

        private fun matches(description: String, condition: (Any) -> Boolean): Matcher {
            return ConditionMatcher(false, description, condition)
        }

        private fun createDate(
            day: Any, month: Any, year: Any
        ): Date {
            //TODO error catching?
            return Date(
                0,
                day.value.toInt(),
                month.value.toIntOrNull() ?: MonthMappings.mapMonthToInt(month.value) ?: throw IllegalArgumentException(
                    "blaa"
                ), //TODO
                fixYear(year.value.toInt()),
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

        private val GROUP_REMAINING_ANY_FORMULAS = listOf(
            Formula(
                listOf(
                    canBe(GuesserType.HOURS),
                    matches("matches a ':' for \"hours:minutes\"") { it.possibleTypes.isEmpty() && it.value.trim() == ":" },
                    canBe(GuesserType.MINUTES)
                )
            ) { matches ->
                listOf(
                    Any(
                        0, matches[0].value, setOf(GuesserType.HOURS)
                    ), matches[1], Any(
                        0, matches[2].value, setOf(GuesserType.MINUTES)
                    )
                )
            })
        private val GROUP_GUESSES_FORMULAS = listOf(
            Formula(
                listOf(
                    canBe(GuesserType.HOURS),
                    canBe(GuesserType.MINUTES),
                    canBe(GuesserType.SECONDS),
                )
            ) { matches ->
                listOf(
                    Time(
                        0,
                        matches[0].value.toInt(),
                        matches[1].value.toInt(),
                        matches[2].value.toInt(),
                    )
                )
            },
            Formula(
                listOf(
                    canBe(GuesserType.HOURS),
                    canBe(GuesserType.MINUTES),
                )
            ) { matches ->
                listOf(
                    Time(
                        0, matches[0].value.toInt(), matches[1].value.toInt(), 0
                    )
                )
            },
            Formula(
                listOf(
                    canBe(GuesserType.DAY),
                    canBe(GuesserType.MONTH),
                    canBe(GuesserType.YEAR),
                )
            ) { matches ->
                listOf(
                    createDate(
                        matches[0],
                        matches[1],
                        matches[2],
                    )
                )
            },
            Formula(
                listOf(
                    canBe(GuesserType.YEAR),
                    canBe(GuesserType.MONTH),
                    canBe(GuesserType.DAY),
                )
            ) { matches ->
                listOf(
                    createDate(
                        matches[2],
                        matches[1],
                        matches[0],
                    )
                )
            },
        )
    }

    fun guess(): List<Guess> {
        var guesses = mapAndValidateTokens(tokens)
        guesses = mapHints(guesses)
        guesses = applyFormulas(guesses, GROUP_REMAINING_ANY_FORMULAS)
        guesses = guesses.filter { it is Any && it.possibleTypes.isNotEmpty() } //TODO probably not so good, but oh well
        guesses = applyFormulas(guesses, GROUP_GUESSES_FORMULAS)
        return guesses
    }

    private fun mapAndValidateTokens(tokens: List<Pair<TokenizerType, String>>): List<Guess> {
        return tokens.map {
            val possibleTypes = mutableSetOf<GuesserType>()
            if (it.first == TokenizerType.STRING) {
                if (MonthMappings.mapMonthToInt(it.second) != null) {
                    possibleTypes.add(GuesserType.MONTH)
                }
            } else if (it.first == TokenizerType.INT) {
                val num = it.second.toIntOrNull()
                if (num != null) {
                    if (0 <= num && num <= 99 || 1900 <= num && num <= 3000) { //shortform 0-99 + longform 1900-3000
                        possibleTypes.add(GuesserType.YEAR)
                    }
                    if (1 <= num && num <= 12) {
                        possibleTypes.add(GuesserType.MONTH)
                    }
                    if (1 <= num && num <= 31) {
                        possibleTypes.add(GuesserType.DAY)
                    }
                    if (0 <= num && num <= 24) {
                        possibleTypes.add(GuesserType.HOURS)
                    }
                    if (0 <= num && num <= 59) {
                        possibleTypes.add(GuesserType.MINUTES)
                        possibleTypes.add(GuesserType.SECONDS)
                    }
                }
            }
            Any(0, it.second, possibleTypes)
        }
    }

    private fun applyFormulas(inputGuesses: List<Guess>, formulas: List<Formula>): List<Guess> {
        var guesses = inputGuesses
        var result = mutableListOf<Guess>()
        for (formula in formulas) {
            var i = 0
            while (i < guesses.size) {
                if (i + formula.matchers.size <= guesses.size) {
                    var formulaMatches = true
                    val capturedMatches = mutableListOf<Any>()
                    for (step in formula.matchers.indices) {
                        val guess = guesses[i + step]
                        if (guess !is Any) {
                            formulaMatches = false
                            break
                        }
                        val matches = formula.matchers[step].matches(guess)
                        if (!matches) {
                            formulaMatches = false
                            break
                        }
                        capturedMatches.add(guess)
                    }
                    if (formulaMatches) {
                        val replacements = formula.replacement(capturedMatches)
                        result.addAll(replacements)
                        i += capturedMatches.size //TODO adapt for more captured?
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

    private fun mapHints(guesserTokens: List<Guess>): List<Guess> {
        val remainingHints = hints.toMutableList()
        return guesserTokens.map {
            val nextHint = remainingHints.firstOrNull()
            if (nextHint != null) {
                val guesserTypeFromHint = GuesserType.fromHintType(nextHint)
                if (guesserTypeFromHint != null && (it is Any) && it.possibleTypes.contains(guesserTypeFromHint)) {
                    remainingHints.removeFirst()
                    Any(0, it.value, setOf(guesserTypeFromHint)) //only allow hint
                } else {
                    it
                }
            } else {
                it
            }
        }
    }

    enum class GuesserType { //TODO same as hinttype?
        DAY, MONTH, YEAR, HOURS, MINUTES, SECONDS;

        companion object {
            fun fromHintType(hintType: HintType): GuesserType? {
                return when (hintType) {
                    HintType.ANY -> null
                    HintType.DAY -> DAY
                    HintType.MONTH -> MONTH
                    HintType.YEAR -> YEAR
                    HintType.HOURS -> HOURS
                    HintType.MINUTES -> MINUTES
                    HintType.SECONDS -> SECONDS
                }
            }
        }
    }

    private class Formula(val matchers: List<Matcher>, val replacement: (List<Any>) -> List<Guess>) {
        override fun toString(): String {
            return "Formula(matchers=$matchers)"
        }
    }

    private sealed class Matcher {
        abstract val canMatchMultipleTimes: Boolean
        abstract fun matches(any: Any): Boolean
    }

    private data class CanBeMatcher(
        override val canMatchMultipleTimes: Boolean,
        val guesserType: GuesserType
    ) : Matcher() {
        override fun matches(any: Any): Boolean {
            return any.possibleTypes.contains(guesserType)
        }
    }

    private class ConditionMatcher(
        override val canMatchMultipleTimes: Boolean,
        val description: String, //only useful for debugging purposes
        val condition: (Any) -> Boolean
    ) : Matcher() {
        override fun matches(any: Any): Boolean {
            return condition(any)
        }

        override fun toString(): String {
            return "ConditionMatcher(canMatchMultipleTimes=$canMatchMultipleTimes, description='$description')"
        }
    }


}

internal sealed class Guess {
    abstract val confidence: Int
}

internal data class Any(override val confidence: Int, val value: String, val possibleTypes: Set<GuesserType>) : Guess()
internal data class Date(
    override val confidence: Int, val day: Int, val month: Int, val year: Int
) : Guess()

internal data class Time(
    override val confidence: Int, val hours: Int, val minutes: Int, //TODO make nullable as well?
    val seconds: Int?
) : Guess()
