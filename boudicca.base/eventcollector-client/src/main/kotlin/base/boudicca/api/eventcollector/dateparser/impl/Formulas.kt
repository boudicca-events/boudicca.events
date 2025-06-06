package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType

internal object Formulas {
    val FORMULAS = listOf(
        Formula(
            listOf(
                canBe(GuesserType.HOURS),
                matches("matches a ':' for \"hours:minutes:seconds\"") { it.possibleTypes.isEmpty() && it.value.trim() == ":" },
                canBe(GuesserType.MINUTES),
                matches("matches a ':' for \"hours:minutes:seconds\"") { it.possibleTypes.isEmpty() && it.value.trim() == ":" },
                canBe(GuesserType.SECONDS)
            )
        ) { matches ->
            listOf(
                Time(
                    0,
                    matches[0].first().value.toInt(),
                    matches[2].first().value.toInt(),
                    matches[4].first().value.toInt(),
                )
            )
        },
        Formula(
            listOf(
                canBe(GuesserType.HOURS),
                matches("matches a ':' for \"hours:minutes\"") { it.possibleTypes.isEmpty() && it.value.trim() == ":" },
                canBe(GuesserType.MINUTES)
            )
        ) { matches ->
            listOf(
                Time(
                    0, matches[0].first().value.toInt(), matches[2].first().value.toInt(), null
                )
            )
        },
        Formula(
            listOf(
                canBe(GuesserType.DAY),
                matches("matches a '.' for \"day:month:year\"") { it.possibleTypes.isEmpty() && it.value.trim() == "." },
                canBe(GuesserType.MONTH),
                matches("matches a '.' for \"day:month:year\"") { it.possibleTypes.isEmpty() && it.value.trim() == "." },
                canBe(GuesserType.YEAR),
            )
        ) { matches ->
            listOf(
                createDate(
                    matches[0].first(),
                    matches[2].first(),
                    matches[4].first(),
                )
            )
        },
        Formula(
            listOf(
                canBe(GuesserType.YEAR),
                matches("matches a '.' for \"day:month:year\"") { it.possibleTypes.isEmpty() && it.value.trim() == "." },
                canBe(GuesserType.MONTH),
                matches("matches a '.' for \"day:month:year\"") { it.possibleTypes.isEmpty() && it.value.trim() == "." },
                canBe(GuesserType.DAY),
            )
        ) { matches ->
            listOf(
                createDate(
                    matches[4].first(),
                    matches[2].first(),
                    matches[0].first(),
                )
            )
        },
        Formula(
            listOf(
                canBe(GuesserType.HOURS),
                canBeNothing(true),
                canBe(GuesserType.MINUTES),
                canBeNothing(true),
                canBe(GuesserType.SECONDS),
            )
        ) { matches ->
            listOf(
                Time(
                    0,
                    matches[0].first().value.toInt(),
                    matches[2].first().value.toInt(),
                    matches[4].first().value.toInt(),
                )
            )
        },
        Formula(
            listOf(
                canBe(GuesserType.HOURS),
                canBeNothing(true),
                canBe(GuesserType.MINUTES),
            )
        ) { matches ->
            listOf(
                Time(
                    0,
                    matches[0].first().value.toInt(),
                    matches[2].first().value.toInt(),
                    0
                )
            )
        },
        Formula(
            listOf(
                canBe(GuesserType.DAY),
                canBeNothing(true),
                canBe(GuesserType.MONTH),
                canBeNothing(true),
                canBe(GuesserType.YEAR),
            )
        ) { matches ->
            listOf(
                createDate(
                    matches[0].first(),
                    matches[2].first(),
                    matches[4].first(),
                )
            )
        },
        Formula(
            listOf(
                canBe(GuesserType.YEAR),
                canBeNothing(true),
                canBe(GuesserType.MONTH),
                canBeNothing(true),
                canBe(GuesserType.DAY),
            )
        ) { matches ->
            listOf(
                createDate(
                    matches[4].first(),
                    matches[2].first(),
                    matches[0].first(),
                )
            )
        },
    )

    private fun canBe(guesserType: GuesserType, canMatchMultipleTimes: Boolean = false): Matcher {
        return CanBeMatcher(canMatchMultipleTimes, guesserType)
    }

    private fun canBeNothing(canMatchMultipleTimes: Boolean = false): Matcher {
        return CanBeNothingMatcher(canMatchMultipleTimes)
    }

    private fun matches(
        description: String, canMatchMultipleTimes: Boolean = false, condition: (Any) -> Boolean
    ): Matcher {
        return ConditionMatcher(canMatchMultipleTimes, description, condition)
    }

    private fun createDate(
        day: Any, month: Any, year: Any
    ): Date {
        //TODO error catching?
        return Date(
            0,
            day.value.toInt(),
            month.value.toIntOrNull() ?: MonthMappings.mapMonthToInt(month.value) ?: throw IllegalArgumentException(
                "blaa"
            ), //TODO
            fixYear(year.value.toInt()),
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

    internal class Formula(val matchers: List<Matcher>, val replacement: (List<List<Any>>) -> List<Guess>) {
        override fun toString(): String {
            return "Formula(matchers=$matchers)"
        }
    }

    internal sealed class Matcher {
        abstract val canMatchMultipleTimes: Boolean
        abstract fun matches(any: Any): Boolean
    }

    internal data class CanBeNothingMatcher(
        override val canMatchMultipleTimes: Boolean,
    ) : Matcher() {
        override fun matches(any: Any): Boolean {
            return any.possibleTypes.isEmpty()
        }
    }

    internal data class CanBeMatcher(
        override val canMatchMultipleTimes: Boolean, val guesserType: GuesserType
    ) : Matcher() {
        override fun matches(any: Any): Boolean {
            return any.possibleTypes.contains(guesserType)
        }
    }

    internal class ConditionMatcher(
        override val canMatchMultipleTimes: Boolean, val description: String, //only useful for debugging purposes
        val condition: (Any) -> Boolean
    ) : Matcher() {
        override fun matches(any: Any): Boolean {
            return condition(any)
        }

        override fun toString(): String {
            return "ConditionMatcher(canMatchMultipleTimes=$canMatchMultipleTimes, description='$description')"
        }
    }

}
