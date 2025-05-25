package base.boudicca.api.eventcollector.dateparser

import assertk.assertThat
import assertk.assertions.isEqualTo
import base.boudicca.api.eventcollector.dateparser.impl.*
import org.junit.jupiter.api.Test

class GuesserTest {

    @Test
    fun testSimpleHintMapping() {
        assertThat(
            Guesser(
                listOf(HintType.DAY, HintType.MONTH, HintType.YEAR),
                listOf(
                    Pair(TokenizerType.INT, "25"),
                    Pair(TokenizerType.SEPARATOR, "."),
                    Pair(TokenizerType.INT, "04"),
                    Pair(TokenizerType.SEPARATOR, "."),
                    Pair(TokenizerType.INT, "1992"),
                )
            ).guess()
        ).isEqualTo(
            listOf(
                Date("25", "04", "1992")
            )
        )
    }

}
