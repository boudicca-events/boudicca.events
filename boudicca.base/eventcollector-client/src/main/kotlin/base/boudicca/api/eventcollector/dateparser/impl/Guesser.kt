package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.HintType

internal class Guesser(private val hints: List<HintType>, private val tokens: List<Pair<TokenizerType, String>>) {
    fun guess(): List<Guess> {
        val remainingHints = hints.toMutableList()
        val guesses = tokens.map {
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
        return guesses
    }

    private fun isNotNoise(it: Pair<TokenizerType, String>): Boolean {
        return it.first == TokenizerType.INT || it.first == TokenizerType.STRING && MonthMappings.mapMonthToInt(it.second) != null
    }
}

internal sealed class Guess
internal class Noise(val value: String) : Guess()
internal class Any(val value: String) : Guess()
internal class Day(val value: String) : Guess()
internal class Month(val value: String) : Guess()
internal class Year(val value: String) : Guess()
internal class Hours(val value: String) : Guess()
internal class Minutes(val value: String) : Guess()
internal class Seconds(val value: String) : Guess()
internal class Date(val day: String, val month: String, val year: String) : Guess() //TODO should those be int already?
internal class Time(
    val hours: String,
    val minutes: String, //TODO make nullable as well?
    val seconds: String?
) : Guess() //TODO should those be int already?
