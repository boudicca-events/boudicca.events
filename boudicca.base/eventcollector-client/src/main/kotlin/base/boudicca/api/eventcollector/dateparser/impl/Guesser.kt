package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.DatePair
import base.boudicca.api.eventcollector.dateparser.DateParserResult
import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId


internal class Guesser(private val tokenGroups: List<List<Pair<TokenizerType, String>>>) {

    fun guess(): DateParserResult {
        val guesses = tokenGroups.map { mapAndValidateTokens(it) }
            .reduce { acc, guesses -> acc + listOf(Separator(10000)) + guesses }

        val chain = buildChain()
        val result = chain.mutate(Guesses(guesses))
        if (result != null) {
            return result
        }
        throw IllegalArgumentException("could not solve, blabla") //TODO
    }

    private fun buildChain(): MutatorChain {
        return FixedPatternStep(CheckIfSolvableStep(GroupingStep(CheckIfSolvableStep(null))))
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

    class GroupingStep(val next: MutatorChain) : MutatorChain {
        override fun mutate(guess: Guess): DateParserResult? {
            var result = next.mutate(guess)
            if (result != null) {
                return result
            }
            if (guess is Guesses) {
                val joinedSeparatorsList = joinAllSeparators(guess)
                val allSeparatorValues = collectSeparatorValues(joinedSeparatorsList)
                for (separatorThreshold in allSeparatorValues) {
                    val groups = mutableListOf<Guesses>()
                    var currentGroup = mutableListOf<Component>()
                    for (component in joinedSeparatorsList) {
                        when (component) {
                            is Separator -> {
                                if (component.weight >= separatorThreshold) {
                                    if (currentGroup.isNotEmpty()) {
                                        groups.add(Guesses(currentGroup))
                                        currentGroup = mutableListOf()
                                    }
                                } else {
                                    currentGroup.add(component)
                                }
                            }

                            else -> currentGroup.add(component)
                        }
                    }
                    if (currentGroup.isNotEmpty()) {
                        groups.add(Guesses(currentGroup))
                    }
                    result = next.mutate(Grouping(groups))
                    if (result != null) {
                        return result
                    }
                }
            }
            return null
        }

        private fun joinAllSeparators(guess: Guesses): List<Component> {
            val result = mutableListOf<Component>()
            var currentSeparatorWeight = 0

            for (component in guess.guesses) {
                when (component) {
                    is Separator -> currentSeparatorWeight += component.weight
                    is Any -> {
                        if (component.possibleTypes.isEmpty()) {
                            currentSeparatorWeight += calculateSeparatorWeight(component.value)
                        } else {
                            if (currentSeparatorWeight > 0) {
                                if (result.isNotEmpty()) {
                                    result.add(Separator(currentSeparatorWeight))
                                }
                                currentSeparatorWeight = 0
                            }
                            result.add(component)
                        }
                    }
                }
            }

            return result
        }

        private fun calculateSeparatorWeight(value: String): Int {
            return value.map {
                when (it) {
                    '.', '-', '/', ':' -> 1
                    ' ' -> 2
                    else -> 3
                }
            }.sum()
        }

        private fun collectSeparatorValues(components: List<Component>): List<Int> {
            return components.asSequence()
                .filterIsInstance<Separator>()
                .map { it.weight }
                .distinct()
                .sortedDescending()
                .toList()
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

internal data class Separator(val weight: Int) : Component {
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

internal class Guesses(inputGuesses: List<Component>) : Guess {

    val guesses = if (inputGuesses.any { !it.isSolved() } && Patterns.canApplyWithoutCollision(
            inputGuesses, Patterns.PATTERNS_MAYBES
        )) {
        Patterns.apply(inputGuesses, Patterns.PATTERNS_MAYBES)
    } else {
        inputGuesses
    }

    override fun solve(): DateParserResult? {
        if (guesses.any { !it.isSolved() }) {
            return null
        }
        val components = getAllSolutionComponents()
        return buildDateParserResultFromComponents(components)
    }

    fun getAllSolutionComponents(): List<SolutionComponent> {
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

    fun isInteresting(): Boolean {
        return guesses.any { it is Any && it.possibleTypes.isNotEmpty() }
    }
}

internal data class Grouping(val inputGroups: List<Guesses>) : Grouper {
    override fun solve(): DateParserResult? {
        val groups = inputGroups.filter { it.isInteresting() }
        if (groups.size == 1) {
            return groups.single().solve()
        }
        if (groups.size != 2) {
            //dunno how to handle this
            return null
        }
        val left = groups[0]
        val right = groups[1]

        var leftComponents = left.getAllSolutionComponents().filter { it.isSolved() }
        var rightComponents = right.getAllSolutionComponents().filter { it.isSolved() }

        if (leftComponents.isEmpty() && rightComponents.isEmpty()) {
            return null
        }
        if (leftComponents.size > 1 || rightComponents.size > 1) {
            return null
        }
        if (leftComponents.isEmpty()) {
            val rightResult = rightComponents.single()
            leftComponents = retryWithContext(rightResult, left)
            if (leftComponents.size != 1 || !leftComponents.single().isSolved()) {
                return null
            }
        }
        if (rightComponents.isEmpty()) {
            val leftResult = leftComponents.single()
            rightComponents = retryWithContext(leftResult, right)
            if (rightComponents.size != 1 || !rightComponents.single().isSolved()) {
                return null
            }
        }
        return buildDateParserResultFromComponents(leftComponents + rightComponents)
    }

    private fun retryWithContext(
        solutionComponent: SolutionComponent, guessesToRetry: Guesses
    ): List<SolutionComponent> {
        return when (solutionComponent) {
            is Date -> {
                removeTypesFromGuesses(
                    guessesToRetry, setOf(GuesserType.DAY, GuesserType.MONTH, GuesserType.YEAR)
                )
            }

            is Time -> {
                removeTypesFromGuesses(
                    guessesToRetry, setOf(GuesserType.HOURS, GuesserType.MINUTES, GuesserType.SECONDS)
                )
            }
        }.getAllSolutionComponents()
    }

    private fun removeTypesFromGuesses(guesses: Guesses, types: Set<GuesserType>): Guesses {
        return Guesses(guesses.guesses.map {
            if (it is Any) {
                Any(
                    it.value, it.possibleTypes.minus(types)
                )
            } else {
                it
            }
        })
    }
}

private fun buildDateParserResultFromComponents(components: List<SolutionComponent>): DateParserResult? {
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
