package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.HintType

internal class Guesser(private val hints: List<HintType>, private val tokens: List<Pair<TokenizerType, String>>) {
    fun guess(): List<Guess> {
        var guesserTokens = mapAndValidateTokens(tokens)
        guesserTokens = mapHints(guesserTokens)
        guesserTokens = guessRemainingAny(guesserTokens)
        var guesses =
            mapGuesserTokensToGuesses(guesserTokens).filter { it !is Noise } //TODO probably not so good, but oh well
        guesses = groupGuesses(guesses)
        return guesses
    }

    private fun mapGuesserTokensToGuesses(guesserTokens: List<GuesserToken>): List<Guess> {
        return guesserTokens.map {
            if (it.possibleTypes.isEmpty()) {
                Noise(0, it.value)
            } else if (it.possibleTypes.size == 1) {
                val guesserType = it.possibleTypes.first()

                var parsedValue = it.value.toIntOrNull()
                if (parsedValue == null && guesserType == GuesserType.MONTH) {
                    parsedValue = MonthMappings.mapMonthToInt(it.value)
                }

                if (parsedValue != null) {
                    when (guesserType) {
                        GuesserType.DAY -> Day(0, parsedValue)
                        GuesserType.MONTH -> Month(0, parsedValue)
                        GuesserType.YEAR -> Year(0, fixYear(parsedValue))
                        GuesserType.HOURS -> Hours(0, parsedValue)
                        GuesserType.MINUTES -> Minutes(0, parsedValue)
                        GuesserType.SECONDS -> Seconds(0, parsedValue)
                    }
                } else {
                    //could not parse the int, so wtf is it?
                    Noise(0, it.value)
                }
            } else {
                Any(0, it.value)
            }
        }
    }

    private fun mapAndValidateTokens(tokens: List<Pair<TokenizerType, String>>): List<GuesserToken> {
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
            GuesserToken(it.second, possibleTypes)
        }
    }

    private fun guessRemainingAny(guesses: List<GuesserToken>): List<GuesserToken> {
        val result = mutableListOf<GuesserToken>()
        var i = 0
        while (i < guesses.size) {
            if (i + 2 < guesses.size) {
                if (guesses[i].possibleTypes.contains(GuesserType.HOURS) && guesses[i + 1].possibleTypes.isEmpty() && guesses[i + 2].possibleTypes.contains(
                        GuesserType.MINUTES
                    )
                ) {
                    if (guesses[i + 1].value.trim() == ":") {
                        result.add(GuesserToken(guesses[i].value, setOf(GuesserType.HOURS)))
                        result.add(guesses[i + 1])
                        result.add(GuesserToken(guesses[i + 2].value, setOf(GuesserType.MINUTES)))
                        i += 3
                        continue
                    }
                }
            }
            result.add(guesses[i])
            i++
        }
        return result
    }

    private fun groupGuesses(guesses: List<Guess>): List<Guess> {
        val result = mutableListOf<Guess>()
        var i = 0
        while (i < guesses.size) {
            if (i + 2 < guesses.size) {
                if (guesses[i] is Day && guesses[i + 1] is Month && guesses[i + 2] is Year) {
                    result.add(
                        Date(
                            0,
                            (guesses[i] as Day).value, (guesses[i + 1] as Month).value, (guesses[i + 2] as Year).value
                        )
                    )
                    i += 3
                    continue
                } else if (guesses[i] is Year && guesses[i + 1] is Month && guesses[i + 2] is Day) {
                    result.add(
                        Date(
                            0,
                            (guesses[i + 2] as Day).value, (guesses[i + 1] as Month).value, (guesses[i] as Year).value
                        )
                    )
                    i += 3
                    continue
                } else if (guesses[i] is Hours && guesses[i + 1] is Minutes && guesses[i + 2] is Seconds) {
                    result.add(
                        Time(
                            0,
                            (guesses[i] as Hours).value,
                            (guesses[i + 1] as Minutes).value,
                            (guesses[i + 2] as Seconds).value
                        )
                    )
                    i += 3
                    continue
                }
            }
            if (i + 1 < guesses.size) {
                if (guesses[i] is Hours && guesses[i + 1] is Minutes) {
                    result.add(
                        Time(
                            0,
                            (guesses[i] as Hours).value, (guesses[i + 1] as Minutes).value, null
                        )
                    )
                    i += 2
                    continue
                }
            }
            result.add(guesses[i])
            i++
        }
        return result
    }

    private fun mapHints(guesserTokens: List<GuesserToken>): List<GuesserToken> {
        val remainingHints = hints.toMutableList()
        return guesserTokens.map {
            val nextHint = remainingHints.firstOrNull()
            if (nextHint != null) {
                val guesserTypeFromHint = GuesserType.fromHintType(nextHint)
                if (guesserTypeFromHint != null && it.possibleTypes.contains(guesserTypeFromHint)) {
                    remainingHints.removeFirst()
                    GuesserToken(it.value, setOf(guesserTypeFromHint)) //only allow hint
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

    data class GuesserToken(val value: String, val possibleTypes: Set<GuesserType>)

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

internal data class Noise(override val confidence: Int, val value: String) : Guess()
internal data class Any(override val confidence: Int, val value: String) : Guess()
internal data class Day(override val confidence: Int, val value: Int) : Guess()
internal data class Month(override val confidence: Int, val value: Int) : Guess()
internal data class Year(override val confidence: Int, val value: Int) : Guess()
internal data class Hours(override val confidence: Int, val value: Int) : Guess()
internal data class Minutes(override val confidence: Int, val value: Int) : Guess()
internal data class Seconds(override val confidence: Int, val value: Int) : Guess()
internal data class Date(
    override val confidence: Int,
    val day: Int, val month: Int, val year: Int
) : Guess() //TODO should those be int already?

internal data class Time(
    override val confidence: Int,
    val hours: Int, val minutes: Int, //TODO make nullable as well?
    val seconds: Int?
) : Guess() //TODO should those be int already?
