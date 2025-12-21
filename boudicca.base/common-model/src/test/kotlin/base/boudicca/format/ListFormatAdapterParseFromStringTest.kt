package base.boudicca.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ListFormatAdapterParseFromStringTest {
    @Test
    fun testFromStringOrNull() {
        val result = ListFormatAdapter().fromStringOrNull(null)

        assertEquals(listOf<String>(), result)
    }

    @Test
    fun testEmptyString() {
        val result = parseFromString("")

        assertEquals(listOf<String>(), result)
    }

    @Test
    fun testSingleComma() {
        val result = parseFromString(",")

        assertEquals(listOf(""), result)
    }

    @Test
    fun testTrailingDoubleComma() {
        val result = parseFromString("value,,")

        assertEquals(listOf("value", ""), result)
    }

    @Test
    fun testSingleValue() {
        val result = parseFromString("value")

        assertEquals(listOf("value"), result)
    }

    @Test
    fun testTrailingComma() {
        val result = parseFromString("value,")

        assertEquals(listOf("value"), result)
    }

    @Test
    fun testTwoValues() {
        val result = parseFromString("value1,value2")

        assertEquals(listOf("value1", "value2"), result)
    }

    @Test
    fun testOneValueWithEscapedComma() {
        val result = parseFromString("value1\\,value2")

        assertEquals(listOf("value1,value2"), result)
    }

    @Test
    fun testOneValueWithEscapedBackslash() {
        val result = parseFromString("val\\\\ue")

        assertEquals(listOf("val\\ue"), result)
    }

    @Test
    fun testOneValueWithInvalidEscapedChar() {
        val result = parseFromString("val\\zue")

        assertEquals(listOf("value"), result)
    }

    @Test
    fun testOneValueWithEscapeAtEOF() {
        val result = parseFromString("value\\")

        assertEquals(listOf("value"), result)
    }

    @Test
    fun testWhitespaceInValue() {
        val result = parseFromString("1, 2")

        assertEquals(listOf("1", " 2"), result)
    }

    @Test
    fun testTwoEmptyValues() {
        val result = parseFromString(",,")

        assertEquals(listOf("", ""), result)
    }

    @Test
    fun testWhitespace() {
        val result = parseFromString(" ")

        assertEquals(listOf(" "), result)
    }

    @Test
    fun testComplex() {
        val result = parseFromString("1,2\\,3\\\\,4")

        assertEquals(listOf("1", "2,3\\", "4"), result)
    }

    private fun parseFromString(s: String): List<String> {
        return ListFormatAdapter().fromString(s)
    }
}
