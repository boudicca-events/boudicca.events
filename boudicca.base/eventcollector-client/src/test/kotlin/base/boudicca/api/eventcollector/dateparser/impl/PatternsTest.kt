package base.boudicca.api.eventcollector.dateparser.impl

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.Test

class PatternsTest {
    @Test
    fun testSimple() {
        val result = Patterns.apply(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY)),
                Any("04", setOf(Guesser.GuesserType.MONTH)),
                Any("2025", setOf(Guesser.GuesserType.YEAR)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.canBe(Guesser.GuesserType.DAY),
                        Patterns.canBe(Guesser.GuesserType.MONTH),
                        Patterns.canBe(Guesser.GuesserType.YEAR),
                    )
                ) { matches ->
                    listOf(
                        Patterns.createDate(
                            matches[0].first(),
                            matches[1].first(),
                            matches[2].first()
                        )
                    )
                })
        )

        assertThat(result).size().isEqualTo(1)
        assertThat(result).first().isInstanceOf(Date::class)
    }

    @Test
    fun testSimpleNotMatch() {
        val result = Patterns.apply(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY)),
                Any("2025", setOf(Guesser.GuesserType.YEAR)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.canBe(Guesser.GuesserType.DAY),
                        Patterns.canBe(Guesser.GuesserType.MONTH),
                        Patterns.canBe(Guesser.GuesserType.YEAR),
                    )
                ) { matches ->
                    listOf(
                        Patterns.createDate(
                            matches[0].first(),
                            matches[1].first(),
                            matches[2].first()
                        )
                    )
                })
        )

        assertThat(result).size().isEqualTo(2)
        assertThat(result).first().isInstanceOf(Any::class)
        assertThat(result).transform { it[1] }.isInstanceOf(Any::class)
    }

    @Test
    fun testMultipleMatchApply() {
        val result = Patterns.apply(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY)),
                Any(".", setOf()),
                Any(".", setOf()),
                Any(".", setOf()),
                Any("2025", setOf(Guesser.GuesserType.YEAR)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.canBe(Guesser.GuesserType.DAY),
                        Patterns.canBeNothing(true),
                        Patterns.canBe(Guesser.GuesserType.YEAR),
                    )
                ) { matches ->
                    listOf(
                        Patterns.createDate(
                            matches[0].first(),
                            Any("10", setOf()),
                            matches[2].first()
                        )
                    )
                })
        )

        assertThat(result).size().isEqualTo(1)
        assertThat(result).first().isInstanceOf(Date::class)
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
                        Patterns.canBe(Guesser.GuesserType.DAY),
                    )
                ) { _ -> listOf() },
                Patterns.Pattern(
                    listOf(
                        Patterns.canBe(Guesser.GuesserType.YEAR),
                    )
                ) { _ -> listOf() })
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
                        Patterns.canBe(Guesser.GuesserType.DAY),
                    )
                ) { _ -> listOf() },
                Patterns.Pattern(
                    listOf(
                        Patterns.canBe(Guesser.GuesserType.YEAR),
                    )
                ) { _ -> listOf() })
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
                        Patterns.canBe(Guesser.GuesserType.DAY),
                    )
                ) { _ -> listOf() },
                Patterns.Pattern(
                    listOf(
                        Patterns.canBe(Guesser.GuesserType.YEAR),
                    )
                ) { _ -> listOf() })
        )

        assertThat(result).isFalse()
    }

    @Test
    fun testCanApplyWithoutCollisionNo3() {
        val result = Patterns.canApplyWithoutCollision(
            listOf(
                Any("12", setOf(Guesser.GuesserType.DAY)),
                Any("10", setOf(Guesser.GuesserType.MONTH)),
                Any("2024", setOf(Guesser.GuesserType.YEAR)),
            ), listOf(
                Patterns.Pattern(
                    listOf(
                        Patterns.canBe(Guesser.GuesserType.DAY),
                        Patterns.canBe(Guesser.GuesserType.MONTH),
                    )
                ) { _ -> listOf() },
                Patterns.Pattern(
                    listOf(
                        Patterns.canBe(Guesser.GuesserType.MONTH),
                        Patterns.canBe(Guesser.GuesserType.YEAR),
                    )
                ) { _ -> listOf() })
        )

        assertThat(result).isFalse()
    }
}
