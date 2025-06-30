package base.boudicca.api.eventcollector.dateparser.impl

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.Test

class PatternsTest {
    @Test
    fun testSimple() {
        val result = Patterns.apply(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY, Guesser.GuesserType.HOURS)),
                Any("04", setOf(Guesser.GuesserType.MONTH, Guesser.GuesserType.MINUTES)),
                Any("2025", setOf(Guesser.GuesserType.YEAR)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.DAY),
                        Patterns.type(Guesser.GuesserType.MONTH),
                        Patterns.type(Guesser.GuesserType.YEAR),
                    )
                )
            )
        )

        assertThat(result).isEqualTo(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY)),
                Any("04", setOf(Guesser.GuesserType.MONTH)),
                Any("2025", setOf(Guesser.GuesserType.YEAR)),
            )
        )
    }

    @Test
    fun testSimpleNotMatch() {
        val input = listOf(
            Any("12", setOf(Guesser.GuesserType.DAY)),
            Any("2025", setOf(Guesser.GuesserType.YEAR)),
        )

        val result = Patterns.apply(
            input, listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.DAY),
                        Patterns.type(Guesser.GuesserType.MONTH),
                        Patterns.type(Guesser.GuesserType.YEAR),
                    )
                )
            )
        )

        assertThat(result).isEqualTo(input)
    }

    @Test
    fun testMultipleMatchApply() {
        val result = Patterns.apply(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY, Guesser.GuesserType.HOURS)),
                Any(".", setOf()),
                Any(".", setOf()),
                Any(".", setOf()),
                Any("2025", setOf(Guesser.GuesserType.YEAR, Guesser.GuesserType.SECONDS)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.DAY),
                        Patterns.noise(true),
                        Patterns.type(Guesser.GuesserType.YEAR),
                    )
                )
            )
        )

        assertThat(result).isEqualTo(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY)),
                Any(".", setOf()),
                Any(".", setOf()),
                Any(".", setOf()),
                Any("2025", setOf(Guesser.GuesserType.YEAR)),
            )
        )
    }

    @Test
    fun testCanApplyWithoutCollisionYes() {
        val result = Patterns.canApplyWithoutCollision(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY)),
                Any("2025", setOf(Guesser.GuesserType.YEAR)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.DAY),
                    )
                ),
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.YEAR),
                    )
                )
            )
        )

        assertThat(result).isTrue()
    }

    @Test
    fun testCanApplyWithoutCollisionNo() {
        val result = Patterns.canApplyWithoutCollision(
            listOf(
                Any("12", setOf(Guesser.GuesserType.YEAR, Guesser.GuesserType.DAY)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.DAY),
                    )
                ),
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.YEAR),
                    )
                )
            )
        )

        assertThat(result).isFalse()
    }

    @Test
    fun testCanApplyWithoutCollisionNo2() {
        val result = Patterns.canApplyWithoutCollision(
            listOf(
                Any("12", setOf(Guesser.GuesserType.YEAR, Guesser.GuesserType.DAY)),
                Any("24", setOf(Guesser.GuesserType.YEAR, Guesser.GuesserType.DAY)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.DAY),
                    )
                ),
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.YEAR),
                    )
                )
            )
        )

        assertThat(result).isFalse()
    }

    @Test
    fun testCanApplyWithoutCollisionYes2() {
        val result = Patterns.canApplyWithoutCollision(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY)),
                Any("10", setOf(Guesser.GuesserType.MONTH)),
                Any("2024", setOf(Guesser.GuesserType.YEAR)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.DAY),
                        Patterns.type(Guesser.GuesserType.MONTH),
                    )
                ),
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.MONTH),
                        Patterns.type(Guesser.GuesserType.YEAR),
                    )
                )
            )
        )

        assertThat(result).isTrue()
    }

    @Test
    fun testCanApplyWithoutCollisionNo3() {
        val result = Patterns.canApplyWithoutCollision(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY, Guesser.GuesserType.HOURS)),
                Any("10", setOf(Guesser.GuesserType.MONTH, Guesser.GuesserType.MINUTES)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.DAY),
                        Patterns.type(Guesser.GuesserType.MONTH),
                    )
                ),
                Patterns.Pattern(
                    listOf(
                        Patterns.type(Guesser.GuesserType.HOURS),
                        Patterns.type(Guesser.GuesserType.MINUTES),
                    )
                )
            )
        )

        assertThat(result).isFalse()
    }
}
