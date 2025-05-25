package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.HintType

internal class Guesser(private val hints: List<HintType>, private val tokens: List<Pair<TokenizerType, String>>) {
    fun guess(): List<Guess> {
        var guesses = mapHints()
        guesses = groupGuesses(guesses)
        return guesses
    }

    private fun groupGuesses(guesses: List<Guess>): List<Guess> {
        val result = mutableListOf<Guess>()
        var i = 0
        while (i < guesses.size) {
            if (i + 4 < guesses.size) {
                if (guesses[i] is Day && guesses[i + 1] is Noise && guesses[i + 2] is Month && guesses[i + 3] is Noise && guesses[i + 4] is Year) {
                    result.add(
                        Date(
                            (guesses[i] as Day).value, (guesses[i + 2] as Month).value, (guesses[i + 4] as Year).value
                        )
                    )
                    i += 5
                    continue
                } else if (guesses[i] is Year && guesses[i + 1] is Noise && guesses[i + 2] is Month && guesses[i + 3] is Noise && guesses[i + 4] is Day) {
                    result.add(
                        Date(
                            (guesses[i + 4] as Day).value, (guesses[i + 2] as Month).value, (guesses[i] as Year).value
                        )
                    )
                    i += 5
                    continue
                } else if (guesses[i] is Hours && guesses[i + 1] is Noise && guesses[i + 2] is Minutes && guesses[i + 3] is Noise && guesses[i + 4] is Seconds) {
                    result.add(
                        Time(
                            (guesses[i] as Hours).value,
                            (guesses[i + 2] as Minutes).value,
                            (guesses[i + 4] as Seconds).value
                        )
                    )
                    i += 5
                    continue
                }
            }
            if (i + 2 < guesses.size) {
                if (guesses[i] is Hours && guesses[i + 1] is Noise && guesses[i + 2] is Minutes) {
                    result.add(
                        Time(
                            (guesses[i] as Hours).value, (guesses[i + 2] as Minutes).value, null
                        )
                    )
                    i += 3
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
                val hint = remainingHints.removeFirst() //TODO guard against that
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
