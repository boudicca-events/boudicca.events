package base.boudicca.keyfilters

import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import base.boudicca.SemanticKeys
import base.boudicca.model.structured.StructuredEntry
import base.boudicca.model.structured.VariantConstants
import base.boudicca.model.structured.key
import base.boudicca.model.structured.toEvent
import base.boudicca.model.toStructuredEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KeySelectorTest {
    @Test
    fun testWithOnlyPropertyName() {
        val result = KeySelector
            .builder("description")
            .build()
            .selectSingle(testEntry())

        assertThat(result).isNotNull()
        assertEquals("description", result!!.first.toKeyString())
    }

    @Test
    fun testWithOneVariant() {
        val result = KeySelector
            .builder("name")
            .thenVariant("format", listOf("markdown", ""))
            .build()
            .selectSingle(testEntry())

        assertThat(result).isNotNull()
        assertEquals("name", result!!.first.toKeyString())
    }

    @Test
    fun testWithWrongPropertyName() {
        val result = KeySelector
            .builder("notavailable")
            .build()
            .selectSingle(testEntry())

        assertThat(result).isNull()
    }

    @Test
    fun testWithMultipleVariants1() {
        val result = KeySelector
            .builder("description")
            .thenVariant("lang", listOf("de", "", "*"))
            .thenVariant("format", listOf("markdown", ""))
            .build()
            .selectSingle(testEntry())

        assertThat(result).isNotNull()
        assertEquals("description:lang=de", result!!.first.toKeyString())
    }

    @Test
    fun testWithMultipleVariants2() {
        val result = KeySelector
            .builder("description")
            .thenVariant("lang", listOf("en", "", "*"))
            .thenVariant("format", listOf("markdown", ""))
            .build()
            .selectSingle(testEntry())

        assertThat(result).isNotNull()
        assertEquals("description:format=markdown:lang=en", result!!.first.toKeyString())
    }

    @Test
    fun testWithMultipleVariants3() {
        val result = KeySelector
            .builder("description")
            .thenVariant("lang", listOf("fr", "", "*"))
            .thenVariant("format", listOf("markdown", ""))
            .build()
            .selectSingle(testEntry())

        assertThat(result).isNotNull()
        assertEquals("description", result!!.first.toKeyString())
    }

    @Test
    fun testWithMultipleVariantsNoResult() {
        val result = KeySelector
            .builder("description")
            .thenVariant("lang", listOf("fr", "cz"))
            .thenVariant("format", listOf("markdown", ""))
            .build()
            .selectSingle(testEntry())

        assertThat(result).isNull()
    }

    @Test
    fun testWith3Variants() {
        val result = KeySelector
            .builder("description")
            .thenVariant("lang", listOf("de", "en"))
            .thenVariant("format", listOf("whatever", "markdown"))
            .thenVariant("anothervariant", listOf("val1", "val2", ""))
            .build()
            .selectSingle(testEntry())

        assertThat(result).isNotNull()
        assertEquals("description:format=markdown:lang=en", result!!.first.toKeyString())
    }

    @Test
    fun testWithEvent() {
        val result = KeySelector
            .builder("name")
            .build()
            .selectSingle(testEntry().toEvent()!!)

        assertThat(result).isNotNull()
        assertEquals("name", result!!.first.toKeyString())
    }

    @Test
    fun testDsl() {
        val keySelector = keySelector(SemanticKeys.DESCRIPTION) {
            thenVariant(
                VariantConstants.LANGUAGE_VARIANT_NAME,
                "de", "en"
            )
            thenVariant(
                VariantConstants.FORMAT_VARIANT_NAME,
                VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME,
                VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME
            )
        }

        val result = keySelector.selectSingle(testEntry())

        assertThat(result).isNotNull()
        assertEquals("description:lang=de", result!!.first.toKeyString())
    }

    private fun testEntry(): StructuredEntry {
        return mapOf(
            "name" to "My Event",
            "startDate" to "2024-04-27T23:59:00+02:00",
            "startDate:format=date" to "2024-04-27T23:59:00+02:00",
            "pictureUrl" to "https://i.insider.com/2b37544bfe5eb549a8378b00?width=1024",
            "description" to "my default lang description",
            "description:lang=de" to "meine deutsche beschreibung",
            "description:lang=en" to "my english description",
            "description:format=markdown:lang=en" to "#my english markdown description"
        ).toStructuredEntry()
    }
}
