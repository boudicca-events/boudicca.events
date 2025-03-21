package base.boudicca.model.structured

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KeyFilterTest {
    @Test
    fun testParseAndToKey() {
        val keyAsString = "*:variant1=*:variant2="

        val parsedAndToKey = KeyFilter.parse(keyAsString).toKeyString()

        assertThat(parsedAndToKey).isEqualTo(keyAsString)
    }
}
