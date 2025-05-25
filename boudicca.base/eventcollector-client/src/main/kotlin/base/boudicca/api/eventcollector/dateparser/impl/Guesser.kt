package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.HintType

internal class Guesser(private val hints: List<HintType>, private val tokens: List<Pair<TokenizerType, String>>) {
    fun guess(): List<Guess> {
        var guesses = mapHints()
        guesses = guessRemainingAny(guesses).filter { it !is Noise } //TODO probably not so good, but oh well
        guesses = groupGuesses(guesses)
        return guesses
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

    private fun mapHints(): List<Guess> {
        val remainingHints = hints.toMutableList()
        return tokens.map {
            if (isNotNoise(it)) {
                val hint = remainingHints.removeFirstOrNull()
                if (hint == HintType.DAY) {
                    Day(it.second)
                } else if (hint == HintType.MONTH) {
                    Month(it.second)
                } else if (hint == HintType.YEAR) {
                    Year(it.second)
                } else if (hint == HintType.HOURS) {
                    Hours(it.second)
                } else if (hint == HintType.MINUTES) {
                    Minutes(it.second)
                } else if (hint == HintType.SECONDS) {
                    Seconds(it.second)
                } else {
                    Any(it.second)
                }
            } else {
                Noise(it.second)
            }
        }
    }

    private fun isNotNoise(it: Pair<TokenizerType, String>): Boolean {
        return it.first == TokenizerType.INT || it.first == TokenizerType.STRING && MonthMappings.mapMonthToInt(it.second) != null
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
