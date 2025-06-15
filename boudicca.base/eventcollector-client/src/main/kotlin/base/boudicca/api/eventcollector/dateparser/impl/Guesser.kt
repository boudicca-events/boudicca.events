package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.DatePair
import base.boudicca.api.eventcollector.dateparser.DateParserResult
import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId


internal class Guesser(private val tokenGroups: List<List<Pair<TokenizerType, String>>>) {

    fun guess(): DateParserResult {
        val guesses =
            tokenGroups.map { mapAndValidateTokens(it) }.reduce { acc, guesses -> acc + listOf(Separator) + guesses }

        val chain = buildChain()
        val result = chain.mutate(Guesses(guesses))
        if (result != null) {
            return result
        }
        throw IllegalArgumentException("could not solve, blabla") //TODO
    }

    private fun buildChain(): MutatorChain {
        return FixedPatternStep(CheckIfSolvableStep(null))
    }

    class CheckIfSolvableStep(val next: MutatorChain?) : MutatorChain {
        override fun mutate(guess: Guess): DateParserResult? {
            val result = guess.solve()
            if (result != null) {
                return result
            }
            if (next != null) {
                return next.mutate(guess)
            }
            return null
        }
    }

    class FixedPatternStep(val next: MutatorChain) : MutatorChain {
        override fun mutate(guess: Guess): DateParserResult? {
            if (guess is Guesses) {
                val applied = Patterns.apply(guess.guesses, Patterns.PATTERNS_FIXED)
                val result = next.mutate(Guesses(applied))
                if (result != null) {
                    return result
                }
            }
            return next.mutate(guess)
        }
    }

    interface MutatorChain {
        fun mutate(guess: Guess): DateParserResult?
    }

    private fun mapAndValidateTokens(tokens: List<Pair<TokenizerType, String>>): List<Component> {
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
            Any(it.second, possibleTypes)
        }
    }

    enum class GuesserType {
        DAY, MONTH, YEAR, HOURS, MINUTES, SECONDS;
    }

}

internal sealed interface Guess {
    fun solve(): DateParserResult? {
        return null
    }
}

internal sealed interface Grouper : Guess {

}

internal sealed interface Component {
    fun isSolved(): Boolean
}

internal data object Separator : Component {
    override fun isSolved(): Boolean {
        return true
    }
}

internal data class Any(val value: String, val possibleTypes: Set<GuesserType>) : Component {
    override fun isSolved(): Boolean {
        return possibleTypes.size <= 1
    }
}

internal sealed interface SolutionComponent {
    fun isSolved(): Boolean {
        return false
    }
}

internal data class Date(
    val day: Int?, val month: Int?, val year: Int?
) : SolutionComponent {
    override fun isSolved(): Boolean {
        return day != null && month != null && year != null
    }

    fun toLocalDate(): LocalDate {
        return LocalDate.of(year!!, month!!, day!!)
    }

    companion object {
        fun create(
            day: Any, month: Any?, year: Any?
        ): Date {
            //TODO error catching?
            return Date(
                day.value.toInt(),
                if (month != null) month.value.toIntOrNull() ?: MonthMappings.mapMonthToInt(month.value)
                ?: throw IllegalArgumentException("blaa")
                else null,
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
    }
}

internal data class Time(
    val hours: Int?, val minutes: Int?, val seconds: Int?
) : SolutionComponent {
    override fun isSolved(): Boolean {
        return hours != null
    }

    fun toLocalTime(): LocalTime {
        return LocalTime.of(hours!!, minutes ?: 0, seconds ?: 0)
    }

    companion object {
        fun create(
            hours: Any, minutes: Any?, seconds: Any?
        ): Time {
            //TODO error catching?
            return Time(
                hours.value.toInt(),
                minutes?.value?.toInt(),
                seconds?.value?.toInt(),
            )
        }
    }
}

internal data class Guesses(val guesses: List<Component>) : Guess {

    override fun solve(): DateParserResult? {
        var guesses = guesses
        if (guesses.any { !it.isSolved() }) {
            if (Patterns.canApplyWithoutCollision(guesses, Patterns.PATTERNS_MAYBES)) {
                guesses = Patterns.apply(guesses, Patterns.PATTERNS_MAYBES)
                if (guesses.any { !it.isSolved() }) {
                    return null
                }
            } else {
                return null
            }
        }
        val components = getAllSolutionComponents(guesses)
        if (components.any { !it.isSolved() }) {
            return null
        }
        if (components.size == 1) {
            val component = components[0]
            if (component is Date) {
                val localDate = component.toLocalDate()
                val date = localDate.atStartOfDay().atZone(ZoneId.of("Europe/Vienna")) //TODO make configurable
                    .toOffsetDateTime()
                return DateParserResult(listOf(DatePair(date)))
            }
        }
        if (components.size == 2) {
            val dates = components.filterIsInstance<Date>()
            val times = components.filterIsInstance<Time>()
            if (dates.size == 1 && times.size == 1) {
                val localDate = dates[0].toLocalDate()
                val localTime = times[0].toLocalTime()
                val date = localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")) //TODO make configurable
                    .toOffsetDateTime()
                return DateParserResult(listOf(DatePair(date)))
            }
        }
        return null
    }

    private fun getAllSolutionComponents(guesses: List<Component>): List<SolutionComponent> {
        val result = mutableListOf<SolutionComponent>()

        val anys = guesses.filterIsInstance<Any>().filter { it.possibleTypes.size == 1 }.toMutableList()
        while (anys.isNotEmpty()) {
            val next = anys.removeFirst()
            if (next.possibleTypes == setOf(GuesserType.DAY)) {
                val month = anys.removeFirstOrNull()
                if (month?.possibleTypes == setOf(GuesserType.MONTH)) {
                    val year = anys.removeFirstOrNull()
                    if (year?.possibleTypes == setOf(GuesserType.YEAR)) {
                        result.add(Date.create(next, month, year))
                    } else {
                        result.add(Date.create(next, month, null))
                    }
                } else {
                    result.add(Date.create(next, null, null))
                }
            }
            if (next.possibleTypes == setOf(GuesserType.YEAR)) {
                val month = anys.removeFirstOrNull()
                if (month?.possibleTypes == setOf(GuesserType.MONTH)) {
                    val day = anys.removeFirstOrNull()
                    if (day?.possibleTypes == setOf(GuesserType.DAY)) {
                        result.add(Date.create(day, month, next))
                    }
                }
            }
            if (next.possibleTypes == setOf(GuesserType.HOURS)) {
                val minutes = anys.removeFirstOrNull()
                if (minutes?.possibleTypes == setOf(GuesserType.MINUTES)) {
                    val seconds = anys.removeFirstOrNull()
                    if (seconds?.possibleTypes == setOf(GuesserType.SECONDS)) {
                        result.add(Time.create(next, minutes, seconds))
                    } else {
                        result.add(Time.create(next, minutes, null))
                    }
                } else {
                    result.add(Time.create(next, null, null))
                }
            }
        }


        return result
    }
}

internal data class Grouping(val groups: List<Guesses>) : Grouper {

}
