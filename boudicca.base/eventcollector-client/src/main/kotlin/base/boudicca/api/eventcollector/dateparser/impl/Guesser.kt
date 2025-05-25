package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.HintType
import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType

internal class Guesser(private val hints: List<HintType>, private val tokens: List<Pair<TokenizerType, String>>) {

    fun guess(): List<Guess> {
        var guesses = mapAndValidateTokens(tokens)
        guesses = applyHints(guesses)
        guesses = applyFormulas(guesses, Formulas.FORMULAS)
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

    private fun applyFormulas(inputGuesses: List<Guess>, formulas: List<Formulas.Formula>): List<Guess> {
        var guesses = inputGuesses
        var result = mutableListOf<Guess>()
        for (formula in formulas) {
            var i = 0
            while (i < guesses.size) {
                if (i + formula.matchers.size <= guesses.size) {
                    var formulaMatches = false
                    var capturedCount = 0
                    var stepCount = 0
                    val capturedMatches = mutableListOf<List<Any>>()
                    var currentCapturedMatches = mutableListOf<Any>()
                    while (i + capturedCount < guesses.size) {
                        val guess = guesses[i + capturedCount]
                        if (guess !is Any) {
                            break
                        }
                        val currentMatcher = formula.matchers[stepCount]
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
                        if (stepCount >= formula.matchers.size) {
                            formulaMatches = true
                            break
                        }
                    }
                    if (formulaMatches) {
                        val replacements = formula.replacement(capturedMatches)
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

    private fun applyHints(guesserTokens: List<Guess>): List<Guess> {
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

    enum class GuesserType {
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
}

internal sealed class Guess {
    abstract val confidence: Int
}

internal data class Any(override val confidence: Int, val value: String, val possibleTypes: Set<GuesserType>) : Guess()
internal data class Date(
    override val confidence: Int, val day: Int, val month: Int, val year: Int
) : Guess()

internal data class Time(
    override val confidence: Int, val hours: Int, val minutes: Int, val seconds: Int?
) : Guess()
