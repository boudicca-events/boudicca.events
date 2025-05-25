package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.HintType
import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType

internal class Guesser(private val hints: List<HintType>, private val tokens: List<Pair<TokenizerType, String>>) {
    fun guess(): List<Guess> {
        var guesses = mapAndValidateTokens(tokens)
        guesses = mapHints(guesses)
        guesses = guessRemainingAny(guesses)
        guesses = guesses.filter { it is Any && it.possibleTypes.isNotEmpty() } //TODO probably not so good, but oh well
        guesses = groupGuesses(guesses)
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

    private fun guessRemainingAny(guesses: List<Guess>): List<Guess> {
        val result = mutableListOf<Guess>()
        var i = 0
        while (i < guesses.size) {
            val first = guesses[i]
            if (i + 2 < guesses.size) {
                val second = guesses[i + 1]
                val third = guesses[i + 2]
                if (first is Any && first.possibleTypes.contains(GuesserType.HOURS) && second is Any && second.possibleTypes.isEmpty() && third is Any && third.possibleTypes.contains(
                        GuesserType.MINUTES
                    )
                ) {
                    if (second.value.trim() == ":") {
                        result.add(Any(0, first.value, setOf(GuesserType.HOURS)))
                        result.add(second)
                        result.add(Any(0, third.value, setOf(GuesserType.MINUTES)))
                        i += 3
                        continue
                    }
                }
            }
            result.add(first)
            i++
        }
        return result
    }

    private fun groupGuesses(guesses: List<Guess>): List<Guess> {
        val result = mutableListOf<Guess>()
        var i = 0
        while (i < guesses.size) {
            val first = guesses[i]
            if (i + 2 < guesses.size) {
                val second = guesses[i + 1]
                val third = guesses[i + 2]
                if (first is Any && first.possibleTypes.contains(GuesserType.DAY) && second is Any && second.possibleTypes.contains(
                        GuesserType.MONTH
                    ) && third is Any && third.possibleTypes.contains(GuesserType.YEAR)
                ) {
                    result.add(
                        createDate(first, second, third)
                    )
                    i += 3
                    continue
                } else if (first is Any && first.possibleTypes.contains(GuesserType.YEAR) && second is Any && second.possibleTypes.contains(
                        GuesserType.MONTH
                    ) && third is Any && third.possibleTypes.contains(GuesserType.DAY)
                ) {
                    result.add(
                        createDate(third, second, first)
                    )
                    i += 3
                    continue
                } else if (first is Any && first.possibleTypes.contains(GuesserType.HOURS) && second is Any && second.possibleTypes.contains(
                        GuesserType.MINUTES
                    ) && third is Any && third.possibleTypes.contains(GuesserType.SECONDS)
                ) {
                    result.add(
                        Time(
                            0,
                            first.value.toInt(),
                            second.value.toInt(),
                            third.value.toInt(),
                        )
                    )
                    i += 3
                    continue
                }
            }
            if (i + 1 < guesses.size) {
                val second = guesses[i + 1]
                if (first is Any && first.possibleTypes.contains(GuesserType.HOURS) && second is Any && second.possibleTypes.contains(
                        GuesserType.MINUTES
                    )
                ) {
                    result.add(
                        Time(
                            0, first.value.toInt(), second.value.toInt(), null
                        )
                    )
                    i += 2
                    continue
                }
            }
            result.add(first)
            i++
        }
        return result
    }

    private fun createDate(
        day: Any, month: Any, year: Any
    ): Date {
        //TODO error catching?
        return Date(
            0,
            day.value.toInt(),
            month.value.toIntOrNull() ?: MonthMappings.mapMonthToInt(month.value)
            ?: throw IllegalArgumentException("blaa"), //TODO
            fixYear(year.value.toInt()),
        )
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

    private fun fixYear(year: Int): Int {
        return if (year < 70) { //we get some problems in the year 2070 with this...
            2000 + year
        } else if (year < 100) {
            1900 + year
        } else {
            year
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
}

internal sealed class Guess {
    abstract val confidence: Int
}

internal data class Any(override val confidence: Int, val value: String, val possibleTypes: Set<GuesserType>) : Guess()
internal data class Date(
    override val confidence: Int, val day: Int, val month: Int, val year: Int
) : Guess() //TODO should those be int already?

internal data class Time(
    override val confidence: Int, val hours: Int, val minutes: Int, //TODO make nullable as well?
    val seconds: Int?
) : Guess() //TODO should those be int already?
