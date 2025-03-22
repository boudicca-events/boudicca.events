package base.boudicca.model.structured

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class KeyFilterTest {
    @Test
    fun testParseAndToKey() {
        val keyAsString = "*:variant1=*:variant2="

        val parsedAndToKey = KeyFilter.parse(keyAsString).toKeyString()

        assertThat(parsedAndToKey).isEqualTo(keyAsString)
    }
}
