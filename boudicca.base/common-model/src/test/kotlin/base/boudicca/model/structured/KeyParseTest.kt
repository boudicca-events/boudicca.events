package base.boudicca.model.structured

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KeyParseTest {
    @Test
    fun testOnlyPropertyNameKeyFilter() {
        val keyFilter = callParser("description")

        Assertions.assertEquals("description", keyFilter.name)
        Assertions.assertEquals(0, keyFilter.variants.size)
    }

    @Test
    fun testWithVariant() {
        val keyFilter = callParser("description:lang=de")

        Assertions.assertEquals("description", keyFilter.name)
        Assertions.assertEquals(1, keyFilter.variants.size)
        Assertions.assertEquals("lang", keyFilter.variants[0].variantName)
        Assertions.assertEquals("de", keyFilter.variants[0].variantValue)
    }

    @Test
    fun testWithVariants() {
        val keyFilter = callParser("description:format=number:lang=de")

        Assertions.assertEquals("description", keyFilter.name)
        Assertions.assertEquals(2, keyFilter.variants.size)
        Assertions.assertEquals("format", keyFilter.variants[0].variantName)
        Assertions.assertEquals("number", keyFilter.variants[0].variantValue)
        Assertions.assertEquals("lang", keyFilter.variants[1].variantName)
        Assertions.assertEquals("de", keyFilter.variants[1].variantValue)
    }

    @Test
    fun testWithVariantsAndSort() {
        val keyFilter = callParser("description:lang=de:format=number")

        Assertions.assertEquals("description", keyFilter.name)
        Assertions.assertEquals(2, keyFilter.variants.size)
        Assertions.assertEquals("format", keyFilter.variants[0].variantName)
        Assertions.assertEquals("number", keyFilter.variants[0].variantValue)
        Assertions.assertEquals("lang", keyFilter.variants[1].variantName)
        Assertions.assertEquals("de", keyFilter.variants[1].variantValue)
    }

    @Test
    fun testWithInvalidVariants() {
        assertThrows<IllegalArgumentException> { callParser("") }
        assertThrows<IllegalArgumentException> { callParser("description:lang") }
        assertThrows<IllegalArgumentException> { callParser("description:lang==de") }
        assertThrows<IllegalArgumentException> { callParser("description:*=de") }
        assertThrows<IllegalArgumentException> { callParser("description:=de") }
    }

    private fun callParser(propertyKey: String): Key {
        return Key.parse(propertyKey)
    }
}
