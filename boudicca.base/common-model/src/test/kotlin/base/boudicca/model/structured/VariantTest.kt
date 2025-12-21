package base.boudicca.model.structured

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VariantTest {
    @Test
    fun testSorting() {
        val unsorted =
            listOf(
                Variant("name", "value2"),
                Variant("name4", "value"),
                Variant("name3", "value"),
                Variant("name", "value3"),
                Variant("name2", "value"),
                Variant("name", "value"),
            )

        val sorted = unsorted.sorted()

        assertThat(sorted).isEqualTo(
            listOf(
                Variant("name", "value"),
                Variant("name", "value2"),
                Variant("name", "value3"),
                Variant("name2", "value"),
                Variant("name3", "value"),
                Variant("name4", "value"),
            ),
        )
    }

    @Test
    fun testInvalidVariants() {
        assertThrows<IllegalArgumentException> { Variant("", "value") }
        assertThrows<IllegalArgumentException> { Variant("*", "value") }
        assertThrows<IllegalArgumentException> { Variant("asd=asd", "value") }
        assertThrows<IllegalArgumentException> { Variant("asd:asd", "value") }
        assertThrows<IllegalArgumentException> { Variant("name", "asd=asd") }
        assertThrows<IllegalArgumentException> { Variant("name", "asd:asd") }
    }
}
