package base.boudicca.model.structured

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class KeyBuilderTest {
    @Test
    fun `toBuilder and back to key (build) stays the same`() {
        val keysToTest =
            listOf(
                Key("Test"),
                Key("Test", emptyList()),
                Key("Test", listOf(Variant("variant1", "value1"), Variant("variant2", "value2"))),
            )

        assertAll {
            keysToTest.forEach {
                assertThat(it.toBuilder().build()).isEqualTo(it)
            }
        }
    }
}
