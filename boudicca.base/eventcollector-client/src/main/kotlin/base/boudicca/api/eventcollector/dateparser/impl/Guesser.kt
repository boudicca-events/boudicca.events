package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.HintType

internal class Guesser(private val hints: List<HintType>, private val tokens: List<Pair<TokenizerType, String>>) {
    fun guess(): List<Guess> {
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

internal sealed class Guess(val value: String) //TODO probably not right
internal class Noise(value: String) : Guess(value)
internal class Any(value: String) : Guess(value)
internal class Day(value: String) : Guess(value)
internal class Month(value: String) : Guess(value)
internal class Year(value: String) : Guess(value)
internal class Hours(value: String) : Guess(value)
internal class Minutes(value: String) : Guess(value)
internal class Seconds(value: String) : Guess(value)
