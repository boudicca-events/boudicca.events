package base.boudicca.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ListFormatParseToStringTest {

    @Test
    fun testEmptyList() {
        val result = parseFromString(listOf())

        assertNull(result)
    }

    @Test
    fun testListWithEmptyString() {
        val result = parseFromString(listOf(""))

        assertEquals("", result)
    }

    @Test
    fun testListWithOneValue() {
        val result = parseFromString(listOf("value"))

        assertEquals("value", result)
    }

    @Test
    fun testListWithTwoValues() {
        val result = parseFromString(listOf("value1", "value2"))

        assertEquals("value1,value2", result)
    }

    @Test
    fun testListWithValueWithComma() {
        val result = parseFromString(listOf("val,ue"))

        assertEquals("val\\,ue", result)
    }

    @Test
    fun testListWithValueWithBackslash() {
        val result = parseFromString(listOf("val\\ue"))

        assertEquals("val\\\\ue", result)
    }

    @Test
    fun testComplex() {
        val result = parseFromString(listOf("1,", ",2", "\\3", "4\\"))

        assertEquals("1\\,,\\,2,\\\\3,4\\\\", result)
    }

    private fun parseFromString(value: List<String>): String? {
        return ListFormat.parseToString(value)
    }
}