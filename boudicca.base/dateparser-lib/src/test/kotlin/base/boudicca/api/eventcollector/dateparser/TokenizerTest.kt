package base.boudicca.api.eventcollector.dateparser

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import base.boudicca.dateparser.dateparser.impl.Tokenizer
import base.boudicca.dateparser.dateparser.impl.TokenizerType
import org.junit.jupiter.api.Test

class TokenizerTest {

    @Test
    fun testSingleTerms() {
        assertThat(Tokenizer.tokenize("")).isEmpty()
        assertThat(Tokenizer.tokenize("123")).isEqualTo(listOf(Pair(TokenizerType.INT, "123")))
        assertThat(Tokenizer.tokenize("asd")).isEqualTo(listOf(Pair(TokenizerType.STRING, "asd")))
        assertThat(Tokenizer.tokenize(" +:.")).isEqualTo(listOf(Pair(TokenizerType.SEPARATOR, " +:.")))
    }

    @Test
    fun testSplitting() {
        assertThat(Tokenizer.tokenize("123asd +:.")).isEqualTo(
            listOf(
                Pair(TokenizerType.INT, "123"),
                Pair(TokenizerType.STRING, "asd"),
                Pair(TokenizerType.SEPARATOR, " +:."),
            )
        )
        assertThat(Tokenizer.tokenize("Freitag, 25.04.1992 um 19:00")).isEqualTo(
            listOf(
                Pair(TokenizerType.STRING, "Freitag"),
                Pair(TokenizerType.SEPARATOR, ", "),
                Pair(TokenizerType.INT, "25"),
                Pair(TokenizerType.SEPARATOR, "."),
                Pair(TokenizerType.INT, "04"),
                Pair(TokenizerType.SEPARATOR, "."),
                Pair(TokenizerType.INT, "1992"),
                Pair(TokenizerType.SEPARATOR, " "),
                Pair(TokenizerType.STRING, "um"),
                Pair(TokenizerType.SEPARATOR, " "),
                Pair(TokenizerType.INT, "19"),
                Pair(TokenizerType.SEPARATOR, ":"),
                Pair(TokenizerType.INT, "00"),
            )
        )
    }
}
