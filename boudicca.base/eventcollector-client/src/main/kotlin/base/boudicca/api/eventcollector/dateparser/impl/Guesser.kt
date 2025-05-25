package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.HintType

internal class Guesser(private val hints: List<HintType>, private val tokens: List<Pair<TokenizerType, String>>) {
    fun guess(): List<Guess> {
        val guesserTokens = mapAndValidateTokens(tokens)
        var guesses = mapHints(guesserTokens)
        guesses = guessRemainingAny(guesses).filter { it !is Noise } //TODO probably not so good, but oh well
        guesses = groupGuesses(guesses)
        return guesses
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

    private fun guessRemainingAny(guesses: List<Guess>): List<Guess> {
        val result = mutableListOf<Guess>()
        var i = 0
        while (i < guesses.size) {
            if (i + 2 < guesses.size) {
                if (guesses[i] is Any && guesses[i + 1] is Noise && guesses[i + 2] is Any) {
                    if ((guesses[i + 1] as Noise).value.trim() == ":") {
                        result.add(Hours((guesses[i] as Any).value))
                        result.add(guesses[i + 1])
                        result.add(Minutes((guesses[i + 2] as Any).value))
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
                            (guesses[i] as Day).value, (guesses[i + 1] as Month).value, (guesses[i + 2] as Year).value
                        )
                    )
                    i += 3
                    continue
                } else if (guesses[i] is Year && guesses[i + 1] is Month && guesses[i + 2] is Day) {
                    result.add(
                        Date(
                            (guesses[i + 2] as Day).value, (guesses[i + 1] as Month).value, (guesses[i] as Year).value
                        )
                    )
                    i += 3
                    continue
                } else if (guesses[i] is Hours && guesses[i + 1] is Minutes && guesses[i + 2] is Seconds) {
                    result.add(
                        Time(
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

    private fun mapHints(guesserTokens: List<GuesserToken>): List<Guess> {
        val remainingHints = hints.toMutableList()
        return guesserTokens.map {
            if (it.possibleTypes.isEmpty()) {
                Noise(it.value)
            } else {
                val nextHint = remainingHints.firstOrNull()
                if (nextHint != null) {
                    val guesserType = GuesserType.fromHintType(nextHint)
                    if (guesserType != null && it.possibleTypes.contains(guesserType)) {
                        remainingHints.removeFirst()
                        if (nextHint == HintType.DAY) {
                            Day(it.value)
                        } else if (nextHint == HintType.MONTH) {
                            Month(it.value)
                        } else if (nextHint == HintType.YEAR) {
                            Year(it.value)
                        } else if (nextHint == HintType.HOURS) {
                            Hours(it.value)
                        } else if (nextHint == HintType.MINUTES) {
                            Minutes(it.value)
                        } else if (nextHint == HintType.SECONDS) {
                            Seconds(it.value)
                        } else {
                            Any(it.value)
                        }
                    } else {
                        Any(it.value)
                    }
                } else {
                    Any(it.value)
                }
            }
        }
    }

    data class GuesserToken(val value: String, val possibleTypes: MutableSet<GuesserType>) {

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

internal sealed interface Guess
internal data class Noise(val value: String) : Guess
internal data class Any(val value: String) : Guess
internal data class Day(val value: String) : Guess
internal data class Month(val value: String) : Guess
internal data class Year(val value: String) : Guess
internal data class Hours(val value: String) : Guess
internal data class Minutes(val value: String) : Guess
internal data class Seconds(val value: String) : Guess
internal data class Date(
    val day: String, val month: String, val year: String
) : Guess //TODO should those be int already?

internal data class Time(
    val hours: String, val minutes: String, //TODO make nullable as well?
    val seconds: String?
) : Guess //TODO should those be int already?
