package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.DatePair
import base.boudicca.api.eventcollector.dateparser.DateParserResult
import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType

internal sealed interface Guess {
    fun solve(): DateParserResult?
    fun getAllNestedGuesses(): List<Guesses>
}

internal sealed interface Nestable : Guess {
    fun isInteresting(): Boolean
    fun getAllSolutionComponents(): List<SolutionComponent>
}

internal sealed interface CanBeMapped : Guess {
    fun mapAllNestedGuesses(mapper: (Guesses) -> Nestable): Guess
}


internal class Guesses(val guesses: List<Component>) : Nestable, CanBeMapped {

    private val solvedGuesses = if (guesses.any { !it.isSolved() } && Patterns.canApplyWithoutCollision(
            guesses, Patterns.PATTERNS_MAYBES
        )) {
        Patterns.apply(guesses, Patterns.PATTERNS_MAYBES)
    } else {
        guesses
    }

    override fun mapAllNestedGuesses(mapper: (Guesses) -> Nestable): Guess {
        return mapper(this)
    }

    override fun solve(): DateParserResult? {
        if (solvedGuesses.any { !it.isSolved() }) {
            return null
        }
        val components = getAllSolutionComponents()
        return Utils.buildSingleDateParserResultFromComponents(components)
    }

    override fun getAllSolutionComponents(): List<SolutionComponent> {
        if (solvedGuesses.any { !it.isSolved() }) {
            return emptyList()
        }
        val result = mutableListOf<SolutionComponent>()

        val anys = solvedGuesses.filterIsInstance<Any>().filter { it.possibleTypes.size == 1 }
        var i = 0
        while (i < anys.size) {
            val next = anys.get(i)
            if (next.possibleTypes == setOf(GuesserType.DAY)) {
                val month = anys.getOrNull(i + 1)
                if (month?.possibleTypes == setOf(GuesserType.MONTH)) {
                    val year = anys.getOrNull(i + 2)
                    if (year?.possibleTypes == setOf(GuesserType.YEAR)) {
                        i += 2
                        result.add(Date.create(next, month, year))
                    } else {
                        i++
                        result.add(Date.create(next, month, null))
                    }
                } else {
                    result.add(Date.create(next, null, null))
                }
            }
            if (next.possibleTypes == setOf(GuesserType.YEAR)) {
                val month = anys.getOrNull(i + 1)
                if (month?.possibleTypes == setOf(GuesserType.MONTH)) {
                    val day = anys.getOrNull(i + 2)
                    if (day?.possibleTypes == setOf(GuesserType.DAY)) {
                        i += 2
                        result.add(Date.create(day, month, next))
                    }
                }
            }
            if (next.possibleTypes == setOf(GuesserType.HOURS)) {
                val minutes = anys.getOrNull(i + 1)
                if (minutes?.possibleTypes == setOf(GuesserType.MINUTES)) {
                    val seconds = anys.getOrNull(i + 2)
                    if (seconds?.possibleTypes == setOf(GuesserType.SECONDS)) {
                        i += 2
                        result.add(Time.create(next, minutes, seconds))
                    } else {
                        i++
                        result.add(Time.create(next, minutes, null))
                    }
                } else {
                    result.add(Time.create(next, null, null))
                }
            }
            i++
        }


        return result
    }

    override fun isInteresting(): Boolean {
        return guesses.any { it is Any && it.possibleTypes.isNotEmpty() }
    }

    override fun getAllNestedGuesses(): List<Guesses> {
        return listOf(this)
    }
}

internal data class Grouping(val inputGroups: List<Guesses>) : Nestable {

    override fun solve(): DateParserResult? {
        val groups = inputGroups.filter { it.isInteresting() }
        if (groups.size == 1) {
            return groups.single().solve()
        }
        val solutionComponents = getAllSolutionComponents()

        return Utils.buildSingleDateParserResultFromComponents(solutionComponents)
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


    override fun getAllSolutionComponents(): List<SolutionComponent> {
        val groups = inputGroups.filter { it.isInteresting() }
        if (groups.size == 1) {
            return groups.single().getAllSolutionComponents()
        }
        if (groups.size != 2) {
            //dunno how to handle this
            return emptyList()
        }
        val left = groups[0]
        val right = groups[1]

        var leftComponents = left.getAllSolutionComponents()
        var rightComponents = right.getAllSolutionComponents()

        if (leftComponents.isEmpty() && rightComponents.isEmpty()) {
            return emptyList()
        }
        if (leftComponents.size > 1 || rightComponents.size > 1) {
            return emptyList()
        }
        if ((leftComponents + rightComponents).any { !it.isSolved() }) {
            return emptyList()
        }

        if (leftComponents.isEmpty()) {
            val rightResult = rightComponents.single()
            leftComponents = retryWithContext(rightResult, left)
            if (leftComponents.size != 1 || !leftComponents.single().isSolved()) {
                return emptyList()
            }
        }
        if (rightComponents.isEmpty()) {
            val leftResult = leftComponents.single()
            rightComponents = retryWithContext(leftResult, right)
            if (rightComponents.size != 1 || !rightComponents.single().isSolved()) {
                return emptyList()
            }
        }

        return leftComponents + rightComponents
    }

    override fun isInteresting(): Boolean {
        return inputGroups.any { it.isInteresting() }
    }

    override fun getAllNestedGuesses(): List<Guesses> {
        return inputGroups
    }
}

internal data class Until(val inputGroups: List<Nestable>) : CanBeMapped {

    override fun getAllNestedGuesses(): List<Guesses> {
        return inputGroups.flatMap { it.getAllNestedGuesses() }
    }

    override fun mapAllNestedGuesses(mapper: (Guesses) -> Nestable): CanBeMapped {
        return Until(inputGroups.map {
            if (it is Guesses) {
                mapper(it)
            } else {
                it
            }
        })
    }

    override fun solve(): DateParserResult? {
        val groups = inputGroups.filter { it.isInteresting() }
        if (groups.size == 1) {
            return groups.single().solve()
        }
        if (groups.size != 2) {
            //dunno how to handle this
            return null
        }
        val leftGroup = groups[0]
        val rightGroup = groups[1]

        val leftComponents = leftGroup.getAllSolutionComponents()
        val rightComponents = rightGroup.getAllSolutionComponents()

        val (left, right) = shareDataBetweenComponents(listOf(leftComponents, rightComponents))
        val (leftFull, rightFull) = fillMissingComponentsBetweenLists(listOf(left, right))


        val leftDate = Utils.buildOffsetDateTimeFromComponents(leftFull)
        val rightDate = Utils.buildOffsetDateTimeFromComponents(rightFull)

        if (leftDate != null && rightDate != null) {
            return DateParserResult(listOf(DatePair(leftDate, rightDate)))
        }

        return null
    }

    private fun shareDataBetweenComponents(componentLists: List<List<SolutionComponent>>): List<List<SolutionComponent>> {
        val allComponents = componentLists.flatten()
        return componentLists.map { list ->
            list.map { component ->
                if (component.isSolved()) {
                    component
                } else {
                    when (component) {
                        is Date -> {
                            var newDate: Date = component
                            val dates = allComponents.filterIsInstance<Date>()

                            for (date in dates) {
                                if (newDate.day == null && date.day != null) {
                                    newDate = Date(date.day, newDate.month, newDate.year)
                                }
                                if (newDate.month == null && date.month != null) {
                                    newDate = Date(newDate.day, date.month, newDate.year)
                                }
                                if (newDate.year == null && date.year != null) {
                                    newDate = Date(newDate.day, newDate.month, date.year)
                                }
                            }

                            newDate
                        }

                        is Time -> {
                            var newTime: Time = component
                            val times = allComponents.filterIsInstance<Time>()

                            for (time in times) {
                                if (newTime.hours == null && time.hours != null) {
                                    newTime = Time(time.hours, newTime.minutes, newTime.seconds)
                                }
                                if (newTime.minutes == null && time.minutes != null) {
                                    newTime = Time(newTime.hours, time.minutes, newTime.seconds)
                                }
                                if (newTime.seconds == null && time.seconds != null) {
                                    newTime = Time(newTime.hours, newTime.minutes, time.seconds)
                                }
                            }

                            newTime
                        }
                    }
                }
            }
        }
    }

    private fun fillMissingComponentsBetweenLists(componentLists: List<List<SolutionComponent>>): List<List<SolutionComponent>> {
        val allComponents = componentLists.flatten()
        return componentLists.map { list ->
            //if we have exactly one date/time we filter allComponents for the other and if we find one we take it
            if (list.size == 1) {
                val fillerComponents = allComponents.filter { list.single()::class != it::class }
                if (fillerComponents.size == 1) {
                    list + fillerComponents
                } else {
                    list
                }
            } else {
                list
            }
        }
    }
}
