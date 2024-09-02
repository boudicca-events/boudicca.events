package base.boudicca.model.structured

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KeyUtilsTest {
    @Test
    fun testEmptyMap() {
        val result = toStructuredKeyValuePairs(mapOf())

        assertEquals(emptyMap<Key, String>(), result)
    }

    @Test
    fun testSingleEntry() {
        val result = toStructuredKeyValuePairs(mapOf("key" to "value"))

        assertEquals(
            mapOf(
                Key("key", emptyList()) to "value"
            ), result
        )
    }

    @Test
    fun testTwoProperties() {
        val result = toStructuredKeyValuePairs(
            mapOf(
                "key" to "value",
                "key2" to "value2"
            )
        )

        assertEquals(
            mapOf(
                Key("key", emptyList()) to "value",
                Key("key2", emptyList()) to "value2"
            ), result
        )
    }

    @Test
    fun testOnePropertyTwoVariants() {
        val result = toStructuredKeyValuePairs(
            mapOf(
                "key" to "value",
                "key:variant=foo" to "value2"
            )
        )

        assertEquals(
            mapOf(
                Key("key", emptyList()) to "value",
                Key("key", listOf(Variant("variant", "foo"))) to "value2"
            ), result
        )
    }

    @Test
    fun testTwoPropertiesTwoVariants() {
        val result = toStructuredKeyValuePairs(
            mapOf(
                "key" to "value",
                "key:variant=foo" to "value2",
                "key2:variant=foo" to "value3",
                "key2:variant=foo:variant2=bar" to "value4",
            )
        )

        assertEquals(
            mapOf(
                Key("key", emptyList()) to "value",
                Key("key", listOf(Variant("variant", "foo"))) to "value2",
                Key("key2", listOf(Variant("variant", "foo"))) to "value3",
                Key("key2", listOf(Variant("variant", "foo"), Variant("variant2", "bar"))) to "value4",
            ), result
        )
    }

    private fun toStructuredKeyValuePairs(map: Map<String, String>): Map<Key, String> {
        return KeyUtils.toStructuredKeyValuePairs(map)
    }

    @Test
    fun testToFlatEmpty() {
        val result = toFlat(emptyMap())

        assertEquals(emptyMap<String, String>(), result)
    }

    @Test
    fun testToFlatOneProperty() {
        val result = toFlat(
            mapOf(
                Key("key", listOf()) to "value",
                Key("key", listOf(Variant("variant", "variantvalue"))) to "value2",
            )
        )

        assertEquals(
            mapOf(
                Pair("key", "value"),
                Pair("key:variant=variantvalue", "value2")
            ), result
        )
    }

    @Test
    fun testToFlatTwoProperties() {
        val result = toFlat(
            mapOf(
                Key("key", listOf()) to "value",
                Key("key", listOf(Variant("variant", "variantvalue"))) to "value2",
                Key("key2", listOf()) to "value3",
                Key(
                    "key2",
                    listOf(Variant("variant", "variantvalue"), Variant("variant2", "variantvalue2"))
                ) to "value4"

            )
        )

        assertEquals(
            mapOf(
                Pair("key", "value"),
                Pair("key:variant=variantvalue", "value2"),
                Pair("key2", "value3"),
                Pair("key2:variant=variantvalue:variant2=variantvalue2", "value4"),
            ), result
        )
    }

    private fun toFlat(data: Map<Key, String>): Map<String, String> {
        return KeyUtils.toFlatKeyValuePairs(data)
    }
}